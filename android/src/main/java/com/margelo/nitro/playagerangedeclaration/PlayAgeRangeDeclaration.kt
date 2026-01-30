package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import android.util.Log
import com.facebook.proguard.annotations.DoNotStrip
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.AgeSignalsResult
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus
import com.google.android.play.agesignals.testing.FakeAgeSignalsManager
import com.margelo.nitro.core.Promise
import com.margelo.nitro.NitroModules
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

// https://developer.android.com/google/play/age-signals/test-age-signals-api

@DoNotStrip
class PlayAgeRangeDeclaration : HybridPlayAgeRangeDeclarationSpec() {

  private val appContext: Context
        get() = NitroModules.applicationContext 
            ?: throw IllegalStateException("Application context not available")

  override fun getPlayAgeRangeDeclaration(): Promise<PlayAgeRangeDeclarationResult> {
    return Promise.async {
      try {
        // Retrieve the AgeSignalsManager (NOTE: Mocked scenarios will be handled by getManager)
        val manager = getManager(appContext)
        val request = AgeSignalsRequest.builder().build()

        val result = suspendCancellableCoroutine<PlayAgeRangeDeclarationResult> { cont ->
          manager.checkAgeSignals(request)
            .addOnSuccessListener { r ->
              val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
              val approvalDate = r.mostRecentApprovalDate()?.let { isoDateFormat.format(it) }
              val userStatusString = r.userStatus()?.toString()
              // userStatus will be empty if the user is not in a location where are legally required to show show the age verification prompt
              // This is different from UNKNOWN where the user is not verified, but is in an applicable region
              // https://developer.android.com/google/play/age-signals/use-age-signals-api#age-signals-responses
              val isEligible = !userStatusString.isNullOrEmpty()

              cont.resume(
                PlayAgeRangeDeclarationResult(
                  isEligible = isEligible,
                  installId = r.installId(),
                  ageLower = r.ageLower()?.toDouble(),
                  ageUpper = r.ageUpper()?.toDouble(),
                  mostRecentApprovalDate = approvalDate,
                  userStatus = userStatusString,
                  error = null
                )
              )
            }
            .addOnFailureListener { e ->
              val msg = e.message ?: "Unknown error"
              cont.resume(
                PlayAgeRangeDeclarationResult(
                  isEligible = false,
                  installId = null,
                  userStatus = null,
                  error = "$msg",
                  ageLower = null,
                  ageUpper = null,
                  mostRecentApprovalDate = null,
                )
              )
            }
        }

        result
      } catch (e: Exception) {
        Log.e("PlayAgeRangeDeclaration", "Initialization error", e)
        PlayAgeRangeDeclarationResult(
          isEligible = false,
          installId = null,
          userStatus = null,
          error = "AGE_SIGNALS_INIT_ERROR: ${e.message}",
          ageLower = null,
          ageUpper = null,
          mostRecentApprovalDate = null,
        )
      }
    }
  }

  override fun requestDeclaredAgeRange(firstThresholdAge: Double, secondThresholdAge: Double?, thirdThresholdAge: Double?): Promise<DeclaredAgeRangeResult> {
    return Promise.async {
      DeclaredAgeRangeResult(
        isEligible = false,
        status = null,
        lowerBound = null,
        upperBound = null,
        parentControls = null
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
