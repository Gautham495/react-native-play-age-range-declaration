package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import android.util.Log
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

  override fun setGooglePlayMockUser(config: GooglePlayMockConfig?) {
    if (config == null) {
      mockUser = null
      return
    }

    val userStatus = when (config.userStatus) {
      PlayAgeSignalsUserStatus.VERIFIED -> AgeSignalsVerificationStatus.VERIFIED
      PlayAgeSignalsUserStatus.SUPERVISED -> AgeSignalsVerificationStatus.SUPERVISED
      PlayAgeSignalsUserStatus.SUPERVISED_APPROVAL_PENDING -> AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_PENDING
      PlayAgeSignalsUserStatus.SUPERVISED_APPROVAL_DENIED -> AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_DENIED
      PlayAgeSignalsUserStatus.UNKNOWN -> AgeSignalsVerificationStatus.UNKNOWN
    }

    val user = AgeSignalsResult.builder().setInstallId(config.installId ?: "fake_install_id_12345")
    user.setUserStatus(userStatus)
    config.ageLower?.let { user.setAgeLower(it.toInt()) }
    config.ageUpper?.let { user.setAgeUpper(it.toInt()) }
    config.mostRecentApprovalDate?.let {
      val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it)
      if (date != null) user.setMostRecentApprovalDate(date)
    }

    mockUser = user.build()
  }

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
