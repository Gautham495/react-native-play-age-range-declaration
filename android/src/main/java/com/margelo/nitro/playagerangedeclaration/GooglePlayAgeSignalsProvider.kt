package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import android.util.Log
import com.google.android.play.agesignals.AgeSignalsException
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus
import com.google.android.play.agesignals.testing.FakeAgeSignalsManager
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume

// https://developer.android.com/google/play/age-signals/use-age-signals-api
object GooglePlayAgeSignalsProvider : StoreAgeSignalsProvider {
  private const val TAG = "PlayAgeRangeDeclaration"
  private const val PLAYSTORE = "com.android.vending"

  override val store = AppStore.GOOGLE_PLAY

  override fun isAvailable(context: Context): Boolean {
    if (PlayAgeRangeDeclaration.googlePlayMockUser != null) return true

    return getInstallerPackageName(context) == PLAYSTORE
  }

  // Returns the real AgeSignalsManager, or a FakeAgeSignalsManager when a mock user is set.
  private fun getManager(context: Context): AgeSignalsManager {
    return PlayAgeRangeDeclaration.googlePlayMockUser?.let {
      FakeAgeSignalsManager().apply { setNextAgeSignalsResult(it) }
    } ?: AgeSignalsManagerFactory.create(context)
  }

  private fun emptyResult(error: String? = null) = PlayAgeSignalsResult(
    installId = null,
    userStatus = null,
    ageLower = null,
    ageUpper = null,
    error = error,
    mostRecentApprovalDate = null,
  )

  private fun mapUserStatus(userStatus: Int?): PlayAgeSignalsUserStatus? {
    return when (userStatus) {
      AgeSignalsVerificationStatus.VERIFIED -> PlayAgeSignalsUserStatus.VERIFIED
      AgeSignalsVerificationStatus.DECLARED -> PlayAgeSignalsUserStatus.DECLARED
      AgeSignalsVerificationStatus.SUPERVISED -> PlayAgeSignalsUserStatus.SUPERVISED
      AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_PENDING -> PlayAgeSignalsUserStatus.SUPERVISED_APPROVAL_PENDING
      AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_DENIED -> PlayAgeSignalsUserStatus.SUPERVISED_APPROVAL_DENIED
      AgeSignalsVerificationStatus.UNKNOWN -> PlayAgeSignalsUserStatus.UNKNOWN
      else -> null
    }
  }

  suspend fun getAgeSignals(context: Context): PlayAgeSignalsResult {
    return try {
      val manager = getManager(context)
      val request = AgeSignalsRequest.builder().build()

      suspendCancellableCoroutine { cont ->
        manager.checkAgeSignals(request)
          .addOnSuccessListener { r ->
            val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val approvalDate = r.mostRecentApprovalDate()?.let { isoDateFormat.format(it) }
            // userStatus is empty when the user is not in a region where age
            // verification is legally required. This is different from UNKNOWN,
            // where the user is in an applicable region but not verified.
            // https://developer.android.com/google/play/age-signals/use-age-signals-api#age-signals-responses
            val userStatus = mapUserStatus(r.userStatus())

            cont.resume(
              PlayAgeSignalsResult(
                installId = r.installId(),
                ageLower = r.ageLower()?.toDouble(),
                ageUpper = r.ageUpper()?.toDouble(),
                mostRecentApprovalDate = approvalDate,
                userStatus = userStatus,
                error = null,
              )
            )
          }
          .addOnFailureListener { e ->
            // Prefix the AgeSignalsException error code so callers can tell
            // retryable from non-retryable failures (see "Handle API errors"
            // in the Age Signals docs).
            val code = (e as? AgeSignalsException)?.errorCode
            val msg = e.message ?: "Unknown error"
            cont.resume(
              emptyResult(error = if (code != null) "[$code] $msg" else msg)
            )
          }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Age Signals initialization error", e)
      emptyResult(error = "AGE_SIGNALS_INIT_ERROR: ${e.message}")
    }
  }
}
