package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import com.facebook.proguard.annotations.DoNotStrip
import com.google.android.play.agesignals.AgeSignalsResult
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus
import com.margelo.nitro.core.Promise
import com.margelo.nitro.NitroModules
import java.text.SimpleDateFormat
import java.util.Locale

// https://developer.android.com/google/play/age-signals/test-age-signals-api

@DoNotStrip
class PlayAgeRangeDeclaration : HybridPlayAgeRangeDeclarationSpec() {

  private val appContext: Context
        get() = NitroModules.applicationContext
            ?: throw IllegalStateException("Application context not available")

  override fun detectStore(): AppStore =
    providers.firstOrNull { it.isAvailable(appContext) }?.store ?: AppStore.UNKNOWN

  override fun getGooglePlayAgeSignals(): Promise<PlayAgeSignalsResult> {
    return Promise.async { GooglePlayAgeSignalsProvider.getAgeSignals(appContext) }
  }

  override fun getAmazonUserAgeData(): Promise<AmazonGetUserAgeDataResult> {
    return Promise.async { AmazonGetUserAgeDataProvider.getAgeSignals(appContext) }
  }

  override fun getSamsungAgeSignals(): Promise<SamsungGetAgeSignalsResult> {
    return Promise.async { SamsungGetAgeSignalsProvider.getAgeSignals(appContext) }
  }

  // Apple's Declared Age Range API is iOS-only; report not-eligible on Android.
  override fun requestDeclaredAgeRange(firstThresholdAge: Double, secondThresholdAge: Double?, thirdThresholdAge: Double?): Promise<DeclaredAgeRangeResult> {
    return Promise.async {
      DeclaredAgeRangeResult(
        isEligible = false,
        status = null,
        lowerBound = null,
        upperBound = null,
        parentControls = null,
      )
    }
  }

  override fun setGooglePlayMockUser(config: PlayAgeSignalsMockConfig?) {
    if (config == null) {
      googlePlayMockUser = null
      return
    }

    val userStatus = when (config.userStatus) {
      PlayAgeSignalsUserStatus.VERIFIED -> AgeSignalsVerificationStatus.VERIFIED
      PlayAgeSignalsUserStatus.DECLARED -> AgeSignalsVerificationStatus.DECLARED
      PlayAgeSignalsUserStatus.SUPERVISED -> AgeSignalsVerificationStatus.SUPERVISED
      PlayAgeSignalsUserStatus.SUPERVISED_APPROVAL_PENDING -> AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_PENDING
      PlayAgeSignalsUserStatus.SUPERVISED_APPROVAL_DENIED -> AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_DENIED
      PlayAgeSignalsUserStatus.UNKNOWN -> AgeSignalsVerificationStatus.UNKNOWN
    }

    val user = AgeSignalsResult.builder()
      .setInstallId(config.installId ?: "fake_install_id_12345")
      .setUserStatus(userStatus)
    config.ageLower?.let { user.setAgeLower(it.toInt()) }
    config.ageUpper?.let { user.setAgeUpper(it.toInt()) }
    config.mostRecentApprovalDate?.let {
      // Ignore unparseable dates rather than crash the JS caller.
      runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it) }
        .getOrNull()
        ?.let { date -> user.setMostRecentApprovalDate(date) }
    }

    googlePlayMockUser = user.build()
  }

  override fun setAmazonMockScenario(scenario: Double?) {
    amazonMockScenario = scenario?.toInt()
  }

  override fun setSamsungMockScenario(scenario: Double?) {
    samsungMockScenario = scenario?.toInt()
  }

  companion object {
    /**
     * Store detection precedence: most specific stores first. Google Play is
     * last because it is also the JS-side fallback when no store matches.
     */
    val providers = listOf(
      AmazonGetUserAgeDataProvider,
      SamsungGetAgeSignalsProvider,
      GooglePlayAgeSignalsProvider,
    )

    // Mock state, set from JS via setGooglePlayMockUser / set*MockScenario.
    var googlePlayMockUser: AgeSignalsResult? = null
    var amazonMockScenario: Int? = null
    var samsungMockScenario: Int? = null
  }
}
