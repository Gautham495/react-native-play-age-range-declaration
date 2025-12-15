package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import android.util.Log
import com.facebook.proguard.annotations.DoNotStrip
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
        // MOCK: Using FakeAgeSignalsManager for testing (Commented out for production)
        // https://developer.android.com/google/play/age-signals/test-age-signals-api
        // 
        // To test different scenarios, change the AgeSignalsVerificationStatus:
        // - AgeSignalsVerificationStatus.VERIFIED - User is 18+ (verified adult)
        // - AgeSignalsVerificationStatus.SUPERVISED - User is supervised (13-17 with parental controls)
        // - AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_PENDING - Pending approval
        // - AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_DENIED - Approval denied
        // - AgeSignalsVerificationStatus.UNKNOWN - User status unknown

        // val fakeVerifiedUser = AgeSignalsResult.builder()
        //   .setUserStatus(AgeSignalsVerificationStatus.SUPERVISED)
        //   .setAgeLower(13)
        //   .setAgeUpper(17)
        //   .setMostRecentApprovalDate(
        //     Date.from(LocalDate.of(2025, 2, 1).atStartOfDay(ZoneOffset.UTC).toInstant())
        //   )
        //   .setInstallId("fake_install_id_12345")
        //   .build()
        
        // val manager = FakeAgeSignalsManager()
        // manager.setNextAgeSignalsResult(fakeVerifiedUser)

        // Comment the line below to use the FakeAgeSignalsManager instead of the real API
        val manager = AgeSignalsManagerFactory.create(appContext)

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
}
