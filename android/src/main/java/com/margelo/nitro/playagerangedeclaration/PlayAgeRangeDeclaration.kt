package com.margelo.nitro.playagerangedeclaration

import android.util.Log
import com.facebook.proguard.annotations.DoNotStrip
import com.margelo.nitro.NitroModules
import com.margelo.nitro.core.Promise

// https://developer.android.com/google/play/age-signals/use-age-signals-api
// This class is the JS-facing HybridObject. It owns store detection and
// mock state, and delegates each store's age-signals fetch to the
// corresponding StoreAgeSignalsProvider.

@DoNotStrip
class PlayAgeRangeDeclaration : HybridPlayAgeRangeDeclarationSpec() {
  companion object {
    private const val TAG = "PlayAgeRangeDeclaration"

    // Mock state read by the individual providers to route to their
    // app-local test ContentProviders / FakeAgeSignalsManager.
    @Volatile var googlePlayMockUser: com.google.android.play.agesignals.AgeSignalsResult? = null
    @Volatile var amazonMockScenario: Double? = null
    @Volatile var samsungMockScenario: Double? = null

    // Store detection precedence: most specific stores first. Google Play is
    // last because it is also the fallback when no store matches.
    val providers = listOf(
      AmazonGetUserAgeDataProvider,
      SamsungGetAgeSignalsProvider,
      GooglePlayAgeSignalsProvider,
    )
  }

  private val appContext
    get() = NitroModules.applicationContext
      ?: throw IllegalStateException("Application context not available")

  override fun detectStore(): AppStore {
    val ctx = appContext
    return providers.firstOrNull { it.isAvailable(ctx) }?.store ?: AppStore.GOOGLE_PLAY
  }

  override fun getGooglePlayAgeSignals(): Promise<PlayAgeSignalsResult> {
    return Promise.async {
      GooglePlayAgeSignalsProvider.getAgeSignals(appContext)
    }
  }

  override fun getAmazonUserAgeData(): Promise<AmazonGetUserAgeDataResult> {
    return Promise.async {
      AmazonGetUserAgeDataProvider.getAgeSignals(appContext)
    }
  }

  override fun getSamsungAgeSignals(): Promise<SamsungGetAgeSignalsResult> {
    return Promise.async {
      SamsungGetAgeSignalsProvider.getAgeSignals(appContext)
    }
  }

  override fun setGooglePlayMockUser(config: PlayAgeSignalsMockConfig?) {
    if (config == null) {
      googlePlayMockUser = null
      return
    }

    val builder = com.google.android.play.agesignals.AgeSignalsResult.builder()
      .setInstallId(config.installId ?: "fake_install_id_12345")

    // Map JS-facing userStatus onto the new 0.0.4 split fields
    // (ageRangeSource + significantChangeStatus). The provider's mapUserStatus
    // reads back from these to produce PlayAgeSignalsUserStatus for JS.
    when (config.userStatus) {
      PlayAgeSignalsUserStatus.VERIFIED -> {
        builder.setAgeRangeSource(com.google.android.play.agesignals.model.AgeRangeSource.TIER_A)
        builder.setSignificantChangeStatus(com.google.android.play.agesignals.model.SignificantChangeStatus.UNSPECIFIED)
      }
      PlayAgeSignalsUserStatus.DECLARED -> {
        builder.setAgeRangeSource(com.google.android.play.agesignals.model.AgeRangeSource.TIER_C)
        builder.setSignificantChangeStatus(com.google.android.play.agesignals.model.SignificantChangeStatus.UNSPECIFIED)
      }
      PlayAgeSignalsUserStatus.SUPERVISED -> {
        builder.setAgeRangeSource(com.google.android.play.agesignals.model.AgeRangeSource.TIER_B)
        builder.setSignificantChangeStatus(com.google.android.play.agesignals.model.SignificantChangeStatus.APPROVED)
      }
      PlayAgeSignalsUserStatus.SUPERVISED_APPROVAL_PENDING -> {
        builder.setAgeRangeSource(com.google.android.play.agesignals.model.AgeRangeSource.TIER_B)
        builder.setSignificantChangeStatus(com.google.android.play.agesignals.model.SignificantChangeStatus.PENDING)
      }
      PlayAgeSignalsUserStatus.SUPERVISED_APPROVAL_DENIED -> {
        builder.setAgeRangeSource(com.google.android.play.agesignals.model.AgeRangeSource.TIER_B)
        builder.setSignificantChangeStatus(com.google.android.play.agesignals.model.SignificantChangeStatus.DECLINED)
      }
      PlayAgeSignalsUserStatus.UNKNOWN, null -> {
        builder.setAgeRangeSource(com.google.android.play.agesignals.model.AgeRangeSource.UNSPECIFIED)
        builder.setSignificantChangeStatus(com.google.android.play.agesignals.model.SignificantChangeStatus.UNSPECIFIED)
      }
    }

    config.ageLower?.toInt()?.let { builder.setAgeLower(it) }
    config.ageUpper?.toInt()?.let { builder.setAgeUpper(it) }
    config.mostRecentApprovalDate?.let {
      // If PlayAgeSignalsMockConfig.mostRecentApprovalDate is a String (ISO date),
      // parse it. If it's a Date, pass directly. Adjust based on your spec.
      // Assuming String for now — parse to Date.
      runCatching {
        val parsed = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(it)
        if (parsed != null) builder.setSignificantChangeApprovalDate(parsed)
      }.onFailure { e ->
        Log.w(TAG, "Failed to parse mock mostRecentApprovalDate: $it", e)
      }
    }

    googlePlayMockUser = builder.build()
  }

  override fun setAmazonMockScenario(scenario: Double?) {
    amazonMockScenario = scenario
  }

  override fun setSamsungMockScenario(scenario: Double?) {
    samsungMockScenario = scenario
  }

  // Apple Declared Age Range API is iOS-only. On Android we return an
  // ineligible result so the JS layer's iOS branch is the only caller that
  // sees a real value.
  override fun requestDeclaredAgeRange(
    firstThresholdAge: Double,
    secondThresholdAge: Double?,
    thirdThresholdAge: Double?
  ): Promise<DeclaredAgeRangeResult> {
    return Promise.async {
      DeclaredAgeRangeResult(
        isEligible = false,
        status = null,
        parentControls = null,
        lowerBound = null,
        upperBound = null,
      )
    }
  }

  // Apple's eligibility check is iOS-only; Android is never eligible.
  override fun isEligibleForAgeFeatures(): Promise<Boolean> {
    return Promise.async { false }
  }
}