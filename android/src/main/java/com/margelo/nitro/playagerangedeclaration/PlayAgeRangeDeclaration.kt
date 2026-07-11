package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import com.facebook.proguard.annotations.DoNotStrip
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsResult
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus
import com.google.android.play.agesignals.testing.FakeAgeSignalsManager
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

  override fun getPlayAgeRangeDeclaration(): Promise<PlayAgeSignalsResult> {
    return Promise.async { GooglePlayAgeSignalsProvider.getAgeSignals(appContext) }
  }

  override fun getAmazonAgeRangeDeclaration(): Promise<AmazonGetUserAgeDataResult> {
    return Promise.async { AmazonGetUserAgeDataProvider.getAgeSignals(appContext) }
  }

  override fun getGalaxyAgeRangeDeclaration(): Promise<SamsungGetAgeSignalsResult> {
    return Promise.async { SamsungGetAgeSignalsProvider.getAgeSignals(appContext) }
  }

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
      mockUser = null
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

    mockUser = user.build()
  }

  override fun setAmazonMockScenario(scenario: Double?) {
    amazonTestOption = scenario?.toInt()
  }

  override fun setSamsungMockScenario(scenario: Double?) {
    samsungTestOption = scenario?.toInt()
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
    var mockUser: AgeSignalsResult? = null
    var amazonTestOption: Int? = null
    var samsungTestOption: Int? = null

    // Returns the real AgeSignalsManager, or a FakeAgeSignalsManager when a mock user is set.
    fun getManager(context: Context): AgeSignalsManager {
      return mockUser?.let {
        FakeAgeSignalsManager().apply { setNextAgeSignalsResult(it) }
      } ?: AgeSignalsManagerFactory.create(context)
    }
  }
}
