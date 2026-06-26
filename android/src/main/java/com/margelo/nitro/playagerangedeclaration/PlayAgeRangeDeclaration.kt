package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import android.util.Log
import com.facebook.proguard.annotations.DoNotStrip
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsResult
import com.google.android.play.agesignals.testing.FakeAgeSignalsManager
import com.margelo.nitro.core.Promise
import com.margelo.nitro.NitroModules

// https://developer.android.com/google/play/age-signals/test-age-signals-api

@DoNotStrip
class PlayAgeRangeDeclaration : HybridPlayAgeRangeDeclarationSpec() {

  private val appContext: Context
        get() = NitroModules.applicationContext
            ?: throw IllegalStateException("Application context not available")

  override fun detectStore(): AppStore {
    if (AmazonGetUserAgeDataProvider.isAvailable(appContext)) {
      return AppStore.AMAZON_APPSTORE
    } else if (SamsungAgeSignalsProvider.isAvailable(appContext)) {
      return AppStore.SAMSUNG_GALAXY_STORE
    } else if (GooglePlayAgeSignalsProvider.isAvailable(appContext)) {
      return AppStore.GOOGLE_PLAY
    }

    return AppStore.UNKNOWN
  }

  override fun getPlayAgeRangeDeclaration(): Promise<PlayAgeSignalsResult> {
    return Promise.async { GooglePlayAgeSignalsProvider.getAgeSignals(appContext) }
  }

  override fun getAmazonAgeRangeDeclaration(): Promise<AmazonGetUserAgeDataResult> {
    return Promise.async { AmazonGetUserAgeDataProvider.getAgeSignals(appContext) }
  }

  override fun getGalaxyAgeRangeDeclaration(): Promise<SamsungGetAgeSignalsResult> {
    return Promise.async { SamsungAgeSignalsProvider.getAgeSignals(appContext) }
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

  // MOCK: Use setMockUser for testing
  // https://developer.android.com/google/play/age-signals/test-age-signals-api
  //
  // To test different scenarios, you can override the userStatus with one of the following values:
  // - AgeSignalsVerificationStatus.VERIFIED - User is 18+ (verified adult)
  // - AgeSignalsVerificationStatus.SUPERVISED - User is supervised (13-17 with parental controls)
  // - AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_PENDING - Pending approval
  // - AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_DENIED - Approval denied
  // - AgeSignalsVerificationStatus.UNKNOWN - User status unknown
  //
  // Configure it in any of your native app files (f.ex MainActivity.kt):
  // TODO: Expose the setMockUser functionality to the native bridge
  //
  // import com.margelo.nitro.playagerangedeclaration.PlayAgeRangeDeclaration
  // import com.margelo.nitro.playagerangedeclaration.PlayAgeRangeMockConfig
  //
  // override fun onCreate() {
  //   PlayAgeRangeDeclaration.setMockUser(
  //     PlayAgeRangeMockConfig(
  //       userStatus = AgeSignalsVerificationStatus.SUPERVISED,
  //       ageLower = 13,
  //       ageUpper = 17,
  //       mostRecentApprovalDate = Date.from(LocalDate.of(2025, 2, 1).atStartOfDay(ZoneOffset.UTC).toInstant()),
  //       installId = "fake_install_id_12345"
  //     )
  //   )
  // }

  // Companion object for managing age signal mocking
  companion object {
    var mockUser: AgeSignalsResult? = null

    // Returns AgeSignalManager or the FakeAgeSignalsManager when a mock user is set
    fun getManager(context: Context): AgeSignalsManager {
      return mockUser?.let {
        FakeAgeSignalsManager().apply { setNextAgeSignalsResult(it) }
      } ?: AgeSignalsManagerFactory.create(context)
    }

    // Configure a mocked user
    fun setMockUser(config: PlayAgeRangeMockConfig?) {
      // Reset mock if no configuration is provided
      if(config == null) {
        mockUser = null
        return
      }

      // Initialize the mocked user builder
      val user = AgeSignalsResult.builder().setInstallId("fake_install_id_12345")

      // Apply configuration properties to the builder
      config.userStatus.let { user.setUserStatus(it) }
      config.ageLower?.let { user.setAgeLower(it) }
      config.ageUpper?.let { user.setAgeUpper(it) }
      config.installId?.let { user.setInstallId(it) }
      config.mostRecentApprovalDate?.let { user.setMostRecentApprovalDate(it) }

      // Finalize and store the mocked user instance
      mockUser = user.build()
    }
  }
}
