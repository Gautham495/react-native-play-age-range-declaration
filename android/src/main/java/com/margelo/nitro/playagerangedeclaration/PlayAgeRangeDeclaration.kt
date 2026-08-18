package com.margelo.nitro.playagerangedeclaration

import android.app.Activity
import android.content.Context
import android.util.Log
import com.facebook.proguard.annotations.DoNotStrip
import com.facebook.react.bridge.ReactApplicationContext
import com.google.android.play.agesignals.AgeSignalsAccessRequest
import com.google.android.play.agesignals.AgeSignalsAccessResult
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.AgeSignalsResult
import com.google.android.play.agesignals.model.AgeRangeSource
import com.google.android.play.agesignals.model.AgeSignalsStatus
import com.google.android.play.agesignals.model.SignificantChangeStatus
import com.google.android.play.agesignals.testing.FakeAgeSignalsManager
import com.margelo.nitro.NitroModules
import com.margelo.nitro.core.Promise
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume

// https://developer.android.com/google/play/age-signals/use-age-signals-api
// Migrated from age-signals 0.0.3 → 0.0.4:
//   - Two-step flow: requestAgeSignalsAccess() then checkAgeSignals() if SHARED
//   - userStatus() replaced by ageRangeSource() + significantChangeStatus()
//   - mostRecentApprovalDate() renamed to significantChangeApprovalDate()
// The JS-facing API (PlayAgeRangeDeclarationResult with userStatus, mostRecentApprovalDate)
// is preserved by mapping the new fields back to the old shape.

@DoNotStrip
class PlayAgeRangeDeclaration : HybridPlayAgeRangeDeclarationSpec() {

  private val appContext: Context
    get() = NitroModules.applicationContext
      ?: throw IllegalStateException("Application context not available")

  private val currentActivity: Activity?
    get() = (NitroModules.applicationContext as? ReactApplicationContext)?.currentActivity

  override fun getPlayAgeRangeDeclaration(): Promise<PlayAgeRangeDeclarationResult> {
    return Promise.async {
      try {
        val manager = getManager(appContext)
        val activity = currentActivity
          ?: return@async errorResult("AGE_SIGNALS_NO_ACTIVITY: No current Activity available")

        // Step 1: request access
        val accessResult = suspendCancellableCoroutine<AgeSignalsAccessResult?> { cont ->
          val accessRequest = AgeSignalsAccessRequest.builder()
            .setActivity(activity)
            .build()
          manager.requestAgeSignalsAccess(accessRequest)
            .addOnSuccessListener { r -> cont.resume(r) }
            .addOnFailureListener { e ->
              Log.e("PlayAgeRangeDeclaration", "requestAgeSignalsAccess failed", e)
              cont.resume(null)
            }
        }

        if (accessResult == null) {
          return@async errorResult("AGE_SIGNALS_ACCESS_ERROR: Failed to request age signals access")
        }

        val status = accessResult.ageSignalsStatus()
        // Only SHARED means we can proceed to fetch signals.
        // NOT_SHARED / VERIFICATION_REQUIRED / UNSPECIFIED → not eligible for us.
        if (status != AgeSignalsStatus.SHARED) {
          return@async PlayAgeRangeDeclarationResult(
            isEligible = false,
            installId = null,
            userStatus = PlayAgeRangeDeclarationUserStatus.UNKNOWN,
            error = null,
            ageLower = null,
            ageUpper = null,
            mostRecentApprovalDate = null
          )
        }

        // Step 2: fetch age signals
        val signalsResult = suspendCancellableCoroutine<PlayAgeRangeDeclarationResult> { cont ->
          val request = AgeSignalsRequest.builder().build()
          manager.checkAgeSignals(request)
            .addOnSuccessListener { r ->
              cont.resume(mapToLegacyResult(r))
            }
            .addOnFailureListener { e ->
              val msg = e.message ?: "Unknown error"
              cont.resume(errorResult(msg))
            }
        }

        signalsResult
      } catch (e: Exception) {
        Log.e("PlayAgeRangeDeclaration", "Initialization error", e)
        errorResult("AGE_SIGNALS_INIT_ERROR: ${e.message}")
      }
    }
  }

  override fun requestDeclaredAgeRange(
    firstThresholdAge: Double,
    secondThresholdAge: Double?,
    thirdThresholdAge: Double?
  ): Promise<DeclaredAgeRangeResult> {
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

  // Map new 0.0.4 AgeSignalsResult fields into the legacy JS-facing shape.
  private fun mapToLegacyResult(r: AgeSignalsResult): PlayAgeRangeDeclarationResult {
    val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val approvalDate = r.significantChangeApprovalDate()?.let { isoDateFormat.format(it) }
    val ageLower = r.ageLower()?.toDouble()
    val ageUpper = r.ageUpper()?.toDouble()
    val userStatus = deriveLegacyUserStatus(
      ageRangeSource = r.ageRangeSource(),
      significantChangeStatus = r.significantChangeStatus(),
      ageLower = r.ageLower()
    )

    return PlayAgeRangeDeclarationResult(
      isEligible = true,
      installId = r.installId(),
      ageLower = ageLower,
      ageUpper = ageUpper,
      mostRecentApprovalDate = approvalDate,
      userStatus = userStatus,
      error = null
    )
  }

  // Derive legacy userStatus from the new split fields.
  // Mapping rationale:
  //   - PENDING/DECLINED significant change → surface as the old SUPERVISED_APPROVAL_* values
  //     so the existing JS switch statements keep working.
  //   - Any age range present (ageLower/ageUpper) means supervised age band → SUPERVISED.
  //   - TIER_A source is Google's strongest verification (gov ID / equivalent) → VERIFIED.
  //   - Everything else falls back to UNKNOWN (safest, matches old behavior for empty state).
  private fun deriveLegacyUserStatus(
    ageRangeSource: Int?,
    significantChangeStatus: Int?,
    ageLower: Int?
  ): PlayAgeRangeDeclarationUserStatus {
    return when {
      significantChangeStatus == SignificantChangeStatus.PENDING ->
        PlayAgeRangeDeclarationUserStatus.SUPERVISED_APPROVAL_PENDING
      significantChangeStatus == SignificantChangeStatus.DECLINED ->
        PlayAgeRangeDeclarationUserStatus.SUPERVISED_APPROVAL_DENIED
      ageLower != null ->
        PlayAgeRangeDeclarationUserStatus.SUPERVISED
      ageRangeSource == AgeRangeSource.TIER_A ->
        PlayAgeRangeDeclarationUserStatus.VERIFIED
      else ->
        PlayAgeRangeDeclarationUserStatus.UNKNOWN
    }
  }

  private fun errorResult(message: String): PlayAgeRangeDeclarationResult {
    return PlayAgeRangeDeclarationResult(
      isEligible = false,
      installId = null,
      userStatus = null,
      error = message,
      ageLower = null,
      ageUpper = null,
      mostRecentApprovalDate = null
    )
  }

  // MOCK: Use setMockUser for testing.
  // https://developer.android.com/google/play/age-signals/test-age-signals-api
  companion object {
    var mockUser: AgeSignalsResult? = null

    fun getManager(context: Context): AgeSignalsManager {
      return mockUser?.let {
        FakeAgeSignalsManager().apply { setNextAgeSignalsResult(it) }
      } ?: AgeSignalsManagerFactory.create(context)
    }

    fun setMockUser(config: PlayAgeRangeMockConfig?) {
      if (config == null) {
        mockUser = null
        return
      }

      val user = AgeSignalsResult.builder().setInstallId("fake_install_id_12345")

      // Map old-style userStatus onto the new split fields.
      // We keep PlayAgeRangeMockConfig.userStatus as the input for backward compat,
      // then translate into ageRangeSource + significantChangeStatus so the fake
      // manager returns a shape that mapToLegacyResult() will convert back correctly.
      when (config.userStatus) {
        // VERIFIED (legacy) — user is verified adult → TIER_A, no significant change
        // AgeSignalsVerificationStatus.VERIFIED == 0 per the old enum
        0 -> {
          user.setAgeRangeSource(AgeRangeSource.TIER_A)
          user.setSignificantChangeStatus(SignificantChangeStatus.UNSPECIFIED)
        }
        // SUPERVISED == 1
        1 -> {
          user.setAgeRangeSource(AgeRangeSource.TIER_B)
          user.setSignificantChangeStatus(SignificantChangeStatus.APPROVED)
        }
        // SUPERVISED_APPROVAL_PENDING == 2
        2 -> {
          user.setAgeRangeSource(AgeRangeSource.TIER_B)
          user.setSignificantChangeStatus(SignificantChangeStatus.PENDING)
        }
        // SUPERVISED_APPROVAL_DENIED == 3
        3 -> {
          user.setAgeRangeSource(AgeRangeSource.TIER_B)
          user.setSignificantChangeStatus(SignificantChangeStatus.DECLINED)
        }
        // UNKNOWN == 4
        else -> {
          user.setAgeRangeSource(AgeRangeSource.UNSPECIFIED)
          user.setSignificantChangeStatus(SignificantChangeStatus.UNSPECIFIED)
        }
      }

      config.ageLower?.let { user.setAgeLower(it) }
      config.ageUpper?.let { user.setAgeUpper(it) }
      config.installId?.let { user.setInstallId(it) }
      config.mostRecentApprovalDate?.let { user.setSignificantChangeApprovalDate(it) }

      mockUser = user.build()
    }
  }
}