package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import android.net.Uri

// https://developer.amazon.com/docs/app-submission/user-age-verification.html
object AmazonAgeSignalsProvider {
  private const val PRODUCTION_URI = "content://amzn_appstore/getUserAgeData"

  // Authority uses applicationId to avoid ContentProvider conflicts when multiple apps on the same
  // device use this library. Must match android:authorities="${applicationId}.amzn_test_appstore"
  // in the app's AndroidManifest.xml.
  private fun testUri(context: Context, testOption: Int) =
    "content://${context.packageName}.amzn_test_appstore/getUserAgeData?testOption=$testOption"

  fun getAgeSignals(context: Context, testOption: Int? = null): PlayAgeRangeDeclarationResult {
    val uri = if (testOption != null) Uri.parse(testUri(context, testOption)) else Uri.parse(PRODUCTION_URI)
    val cursor = try {
      context.contentResolver.query(uri, null, null, null, null)
    } catch (e: Exception) {
      return PlayAgeRangeDeclarationResult(
        isEligible = false,
        installId = null,
        userStatus = null,
        error = "AMAZON_PROVIDER_ERROR: ${e.message}",
        ageLower = null,
        ageUpper = null,
        mostRecentApprovalDate = null
      )
    }

    if (cursor == null) {
      return PlayAgeRangeDeclarationResult(
        isEligible = false,
        installId = null,
        userStatus = null,
        error = "AMAZON_PROVIDER_NULL_RESPONSE",
        ageLower = null,
        ageUpper = null,
        mostRecentApprovalDate = null
      )
    }

    return cursor.use { c ->
      if (!c.moveToFirst()) {
        return@use PlayAgeRangeDeclarationResult(
          isEligible = false,
          installId = null,
          userStatus = null,
          error = "AMAZON_PROVIDER_EMPTY_RESPONSE",
          ageLower = null,
          ageUpper = null,
          mostRecentApprovalDate = null
        )
      }

      val responseStatus = c.getString(c.getColumnIndexOrThrow("responseStatus"))
      if (responseStatus != "SUCCESS") {
        return@use PlayAgeRangeDeclarationResult(
          isEligible = false,
          installId = null,
          userStatus = null,
          error = "AMAZON_API_ERROR: $responseStatus",
          ageLower = null,
          ageUpper = null,
          mostRecentApprovalDate = null
        )
      }

      val userStatusString = c.getString(c.getColumnIndexOrThrow("userStatus"))
      // Empty string means user is not in an applicable region
      val userStatus = mapUserStatus(userStatusString)
      val isEligible = userStatus != null

      val ageLowerIdx = c.getColumnIndex("ageLower")
      val ageUpperIdx = c.getColumnIndex("ageUpper")
      val userIdIdx = c.getColumnIndex("userId")
      val approvalDateIdx = c.getColumnIndex("mostRecentApprovalDate")
      val rawApprovalDate =
        if (approvalDateIdx >= 0 && !c.isNull(approvalDateIdx)) c.getString(approvalDateIdx) else null
      val approvalDate = rawApprovalDate?.substringBefore('T')

      PlayAgeRangeDeclarationResult(
        isEligible = isEligible,
        installId = if (userIdIdx >= 0 && !c.isNull(userIdIdx)) c.getString(userIdIdx) else null,
        userStatus = userStatus,
        error = null,
        ageLower = if (ageLowerIdx >= 0 && !c.isNull(ageLowerIdx)) c.getInt(ageLowerIdx).toDouble() else null,
        ageUpper = if (ageUpperIdx >= 0 && !c.isNull(ageUpperIdx)) c.getInt(ageUpperIdx).toDouble() else null,
        mostRecentApprovalDate = approvalDate
      )
    }
  }

  private fun mapUserStatus(status: String?): PlayAgeRangeDeclarationUserStatus? = when (status) {
    "VERIFIED" -> PlayAgeRangeDeclarationUserStatus.VERIFIED
    "SUPERVISED" -> PlayAgeRangeDeclarationUserStatus.SUPERVISED
    // We can't differentiate and map to SUPERVISED_APPROVAL_PENDING or SUPERVISED_APPROVAL_DENIED
    "CONSENT_NOT_GRANTED" -> PlayAgeRangeDeclarationUserStatus.UNKNOWN
    "UNKNOWN" -> PlayAgeRangeDeclarationUserStatus.UNKNOWN
    else -> null // "" means not in applicable region; any other unrecognised value also maps to null
  }
}
