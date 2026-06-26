package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import android.util.Log
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus
import com.margelo.nitro.playagerangedeclaration.PlayAgeRangeDeclaration.Companion.getManager
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume

object GooglePlayAgeSignalsProvider {
  private const val PLAYSTORE = "com.android.vending"

  fun isAvailable(context: Context): Boolean {
    val installer = getInstallerPackageName(context)

    return installer == PLAYSTORE
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
            // userStatus will be empty if the user is not in a location where are legally required to show show the age verification prompt
            // This is different from UNKNOWN where the user is not verified, but is in an applicable region
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
            val msg = e.message ?: "Unknown error"
            cont.resume(
              emptyResult(error = msg)
            )
          }
      }
    } catch (e: Exception) {
      Log.e("PlayAgeRangeDeclaration", "Initialization error", e)
      emptyResult(error = "AGE_SIGNALS_INIT_ERROR: ${e.message}")
    }
  }
}
