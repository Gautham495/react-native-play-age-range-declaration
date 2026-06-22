package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import android.net.Uri
import android.provider.Settings

// https://developer.samsung.com/galaxy-store/galaxy-store-content-provider-api/get-age-signals.html
object SamsungAgeSignalsProvider {
  private const val URI = "content://com.sec.android.app.samsungapps.provider.ASAA/settings"
  private const val METHOD = "getAgeSignalResult"

  // Authority uses applicationId to avoid ContentProvider conflicts when multiple apps on the same
  // device use this library. Must match android:authorities="${applicationId}.samsung_test_provider"
  // in the app's AndroidManifest.xml.
  private fun testUri(context: Context) =
    "content://${context.packageName}.samsung_test_provider/settings"

  fun getAgeSignals(context: Context, testOption: Int? = null): PlayAgeRangeDeclarationResult {
    if (testOption == null) {
      // Samsung's documented ASAA feature gate. On non-Samsung devices this setting is absent (returns
      // null), which falls through to isEligible = false with no error — correct behaviour.
      val asaaEnabled = Settings.Secure.getString(context.contentResolver, "gs_asaa_enable")
      if (asaaEnabled != "1") {
        return PlayAgeRangeDeclarationResult(
          isEligible = false,
          installId = null,
          userStatus = null,
          error = null,
          ageLower = null,
          ageUpper = null,
          mostRecentApprovalDate = null
        )
      }
    }

    val uri = if (testOption != null) Uri.parse(testUri(context)) else Uri.parse(URI)
    val bundle = try {
      context.contentResolver.call(uri, METHOD, testOption?.toString(), null)
    } catch (e: Exception) {
      return PlayAgeRangeDeclarationResult(
        isEligible = false,
        installId = null,
        userStatus = null,
        error = "SAMSUNG_PROVIDER_ERROR: ${e.message}",
        ageLower = null,
        ageUpper = null,
        mostRecentApprovalDate = null
      )
    }

    if (bundle == null) {
      return PlayAgeRangeDeclarationResult(
        isEligible = false,
        installId = null,
        userStatus = null,
        error = "SAMSUNG_PROVIDER_NULL_RESPONSE",
        ageLower = null,
        ageUpper = null,
        mostRecentApprovalDate = null
      )
    }

    val resultCode = bundle.getInt("result_code", 1)
    if (resultCode != 0) {
      val message = bundle.getString("result_message") ?: "Unknown Samsung error"
      return PlayAgeRangeDeclarationResult(
        isEligible = false,
        installId = null,
        userStatus = null,
        error = "SAMSUNG_API_ERROR: $message",
        ageLower = null,
        ageUpper = null,
        mostRecentApprovalDate = null
      )
    }

    val userStatusString = bundle.getString("userStatus")
    val userStatus = mapUserStatus(userStatusString)
    val isEligible = userStatus != null

    return PlayAgeRangeDeclarationResult(
      isEligible = isEligible,
      installId = bundle.getString("installID"),
      userStatus = userStatus,
      error = null,
      ageLower = bundle.getString("ageLower")?.toDoubleOrNull(),
      ageUpper = bundle.getString("ageUpper")?.toDoubleOrNull(),
      mostRecentApprovalDate = bundle.getString("mostRecentApprovalDate")
    )
  }

  private fun mapUserStatus(status: String?): PlayAgeRangeDeclarationUserStatus? = when (status) {
    "VERIFIED" -> PlayAgeRangeDeclarationUserStatus.VERIFIED
    "SUPERVISED" -> PlayAgeRangeDeclarationUserStatus.SUPERVISED
    "SUPERVISED_APPROVAL_PENDING" -> PlayAgeRangeDeclarationUserStatus.SUPERVISED_APPROVAL_PENDING
    "SUPERVISED_APPROVAL_DENIED" -> PlayAgeRangeDeclarationUserStatus.SUPERVISED_APPROVAL_DENIED
    "UNKNOWN" -> PlayAgeRangeDeclarationUserStatus.UNKNOWN
    else -> null
  }
}
