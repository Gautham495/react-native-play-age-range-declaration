package com.margelo.nitro.playagerangedeclaration

import android.app.Activity
import android.content.Context
import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.google.android.play.agesignals.AgeSignalsAccessRequest
import com.google.android.play.agesignals.AgeSignalsException
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.AgeSignalsResult
import com.google.android.play.agesignals.model.AgeRangeSource
import com.google.android.play.agesignals.model.AgeSignalsStatus
import com.google.android.play.agesignals.model.SignificantChangeStatus
import com.google.android.play.agesignals.testing.FakeAgeSignalsManager
import com.margelo.nitro.NitroModules
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume

// https://developer.android.com/google/play/age-signals/use-age-signals-api
// Uses age-signals 0.0.4 API:
//   - Two-step flow: requestAgeSignalsAccess() then checkAgeSignals() if SHARED
//   - userStatus() replaced by ageRangeSource() + significantChangeStatus()
//   - mostRecentApprovalDate() renamed to significantChangeApprovalDate()
// The JS-facing PlayAgeSignalsUserStatus keeps the old shape; we derive it
// from the new split fields.
object GooglePlayAgeSignalsProvider : StoreAgeSignalsProvider {
  private const val TAG = "PlayAgeRangeDeclaration"
  private const val PLAYSTORE = "com.android.vending"

  override val store = AppStore.GOOGLE_PLAY

  override fun isAvailable(context: Context): Boolean {
    if (PlayAgeRangeDeclaration.googlePlayMockUser != null) return true
    return getInstallerPackageName(context) == PLAYSTORE
  }

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

  // Derive legacy JS-facing userStatus from the new 0.0.4 split fields.
  //   - PENDING/DECLINED significant change → SUPERVISED_APPROVAL_*
  //   - Any age range present → SUPERVISED
  //   - TIER_A → VERIFIED (Google's strongest verification)
  //   - TIER_C → DECLARED (self-declared)
  //   - Otherwise → UNKNOWN
  private fun deriveUserStatus(
    ageRangeSource: Int?,
    significantChangeStatus: Int?,
    ageLower: Int?
  ): PlayAgeSignalsUserStatus {
    return when {
      significantChangeStatus == SignificantChangeStatus.PENDING ->
        PlayAgeSignalsUserStatus.SUPERVISED_APPROVAL_PENDING
      significantChangeStatus == SignificantChangeStatus.DECLINED ->
        PlayAgeSignalsUserStatus.SUPERVISED_APPROVAL_DENIED
      ageRangeSource == AgeRangeSource.TIER_A ->
        PlayAgeSignalsUserStatus.VERIFIED
      ageRangeSource == AgeRangeSource.TIER_C ->
        PlayAgeSignalsUserStatus.DECLARED
      ageLower != null ->
        PlayAgeSignalsUserStatus.SUPERVISED
      else ->
        PlayAgeSignalsUserStatus.UNKNOWN
    }
  }

  private fun mapToResult(r: AgeSignalsResult): PlayAgeSignalsResult {
    val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val approvalDate = r.significantChangeApprovalDate()?.let { isoDateFormat.format(it) }
    val ageLowerInt = r.ageLower()
    val userStatus = deriveUserStatus(
      ageRangeSource = r.ageRangeSource(),
      significantChangeStatus = r.significantChangeStatus(),
      ageLower = ageLowerInt
    )
    return PlayAgeSignalsResult(
      installId = r.installId(),
      ageLower = ageLowerInt?.toDouble(),
      ageUpper = r.ageUpper()?.toDouble(),
      mostRecentApprovalDate = approvalDate,
      userStatus = userStatus,
      error = null,
    )
  }

  private val currentActivity: Activity?
    get() = (NitroModules.applicationContext as? ReactApplicationContext)?.currentActivity

  suspend fun getAgeSignals(context: Context): PlayAgeSignalsResult {
    return try {
      val manager = getManager(context)

      // Mock path: skip the access step and go straight to checkAgeSignals,
      // since FakeAgeSignalsManager returns whatever we seeded.
      if (PlayAgeRangeDeclaration.googlePlayMockUser != null) {
        return checkSignals(manager)
      }

      // Real path: two-step flow.
      val activity = currentActivity
        ?: return emptyResult(error = "AGE_SIGNALS_NO_ACTIVITY: No current Activity available")

      val accessResult = suspendCancellableCoroutine<com.google.android.play.agesignals.AgeSignalsAccessResult?> { cont ->
        val accessRequest = AgeSignalsAccessRequest.builder()
          .setActivity(activity)
          .build()
        manager.requestAgeSignalsAccess(accessRequest)
          .addOnSuccessListener { r -> cont.resume(r) }
          .addOnFailureListener { e ->
            Log.e(TAG, "requestAgeSignalsAccess failed", e)
            cont.resume(null)
          }
      }

      if (accessResult == null) {
        return emptyResult(error = "AGE_SIGNALS_ACCESS_ERROR: Failed to request age signals access")
      }

      val status = accessResult.ageSignalsStatus()
      if (status != AgeSignalsStatus.SHARED) {
        // NOT_SHARED / VERIFICATION_REQUIRED / UNSPECIFIED — not eligible.
        return emptyResult()
      }

      checkSignals(manager)
    } catch (e: Exception) {
      Log.e(TAG, "Age Signals initialization error", e)
      emptyResult(error = "AGE_SIGNALS_INIT_ERROR: ${e.message}")
    }
  }

  private suspend fun checkSignals(manager: AgeSignalsManager): PlayAgeSignalsResult {
    return suspendCancellableCoroutine { cont ->
      val request = AgeSignalsRequest.builder().build()
      manager.checkAgeSignals(request)
        .addOnSuccessListener { r ->
          cont.resume(mapToResult(r))
        }
        .addOnFailureListener { e ->
          val code = (e as? AgeSignalsException)?.errorCode
          val msg = e.message ?: "Unknown error"
          cont.resume(emptyResult(error = if (code != null) "[$code] $msg" else msg))
        }
    }
  }
}