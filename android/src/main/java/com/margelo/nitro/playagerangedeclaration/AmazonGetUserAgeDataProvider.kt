package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import android.net.Uri
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus

// https://developer.amazon.com/docs/app-submission/user-age-verification.html
object AmazonGetUserAgeDataProvider {
  private const val AMAZONSTORE: String = "com.amazon.venezia";
  private const val AUTHORITY = "amzn_appstore"
  private const val PATH_GET_USER_AGE_DATA = "getUserAgeData"
  private const val COLUMN_RESPONSE_STATUS: String = "responseStatus"
  private const val COLUMN_USER_STATUS: String = "userStatus"
  private const val COLUMN_USER_ID: String = "userId"
  private const val COLUMN_MOST_RECENT_APPROVAL_DATE: String = "mostRecentApprovalDate"
  private const val COLUMN_AGE_LOWER: String = "ageLower"
  private const val COLUMN_AGE_UPPER: String = "ageUpper"
  private const val URI = "content://$AUTHORITY/$PATH_GET_USER_AGE_DATA"

  fun isAvailable(context: Context): Boolean {
    if (PlayAgeRangeDeclaration.amazonTestOption != null) return true
    val installer = getInstallerPackageName(context)
    return installer == AMAZONSTORE
  }

  private fun mapUserStatus(userStatus: String?): AmazonGetUserAgeDataUserStatus? {
    return when (userStatus) {
      "VERIFIED" -> AmazonGetUserAgeDataUserStatus.VERIFIED
      "SUPERVISED" -> AmazonGetUserAgeDataUserStatus.SUPERVISED
      "UNKNOWN" -> AmazonGetUserAgeDataUserStatus.UNKNOWN
      "CONSENT_NOT_GRANTED" -> AmazonGetUserAgeDataUserStatus.CONSENT_NOT_GRANTED
      else -> null
    }
  }

  private fun mapResponseStatus(userStatus: String?): AmazonGetUserAgeDataResponseStatus? {
    return when (userStatus) {
      "SUCCESS" -> AmazonGetUserAgeDataResponseStatus.SUCCESS
      "APP_NOT_OWNED" -> AmazonGetUserAgeDataResponseStatus.APP_NOT_OWNED
      "INTERNAL_TRANSIENT_ERROR" -> AmazonGetUserAgeDataResponseStatus.INTERNAL_TRANSIENT_ERROR
      "INTERNAL_ERROR" -> AmazonGetUserAgeDataResponseStatus.INTERNAL_ERROR
      "FEATURE_NOT_SUPPORTED" -> AmazonGetUserAgeDataResponseStatus.FEATURE_NOT_SUPPORTED
      else -> null
    }
  }

  private fun emptyResult(error: String? = null, responseStatus: AmazonGetUserAgeDataResponseStatus? = null) = AmazonGetUserAgeDataResult(
    responseStatus = responseStatus,
    userId = null,
    userStatus = null,
    ageLower = null,
    ageUpper = null,
    mostRecentApprovalDate = null,
  )

  fun getAgeSignals(context: Context): AmazonGetUserAgeDataResult {
    val testOption = PlayAgeRangeDeclaration.amazonTestOption
    val queryUri = if (testOption != null) {
      Uri.parse("content://${context.packageName}.amzn_test_appstore/$PATH_GET_USER_AGE_DATA?testOption=$testOption")
    } else {
      Uri.parse(URI)
    }

    val cursor = try {
      context.contentResolver.query(queryUri, null, null, null, null)
    } catch (e: Exception) {
      return emptyResult()
    }

    if (cursor == null) {
      return emptyResult()
    }

    return cursor.use { c ->
      if (!c.moveToFirst()) {
        return@use emptyResult()
      }

      try {
        val responseStatusIdx = c.getColumnIndexOrThrow(COLUMN_RESPONSE_STATUS)
        val responseStatus = c.getString(responseStatusIdx)
        val mappedResponseStatus = mapResponseStatus(responseStatus)
        if (mappedResponseStatus != AmazonGetUserAgeDataResponseStatus.SUCCESS) {
          return@use emptyResult(responseStatus = mappedResponseStatus)
        }

        val userStatusIdx = c.getColumnIndexOrThrow(COLUMN_USER_STATUS)
        val userStatus = c.getString(userStatusIdx)
        val mappedUserStatus = mapUserStatus(userStatus)

        val ageLowerIdx = c.getColumnIndexOrThrow(COLUMN_AGE_LOWER)
        val ageLower = if (!c.isNull(ageLowerIdx)) c.getInt(ageLowerIdx).toDouble() else null

        val ageUpperIdx = c.getColumnIndexOrThrow(COLUMN_AGE_UPPER)
        val ageUpper = if (!c.isNull(ageUpperIdx)) c.getInt(ageUpperIdx).toDouble() else null

        val userIdx = c.getColumnIndexOrThrow(COLUMN_USER_ID)
        val userId = c.getString(userIdx)

        val approvalDateIdx = c.getColumnIndexOrThrow(COLUMN_MOST_RECENT_APPROVAL_DATE)
        val mostRecentApprovalDate = c.getString(approvalDateIdx)

        return@use AmazonGetUserAgeDataResult(
          responseStatus = mappedResponseStatus,
          userStatus = mappedUserStatus,
          ageLower = ageLower,
          ageUpper = ageUpper,
          mostRecentApprovalDate = mostRecentApprovalDate,
          userId = userId,
        )
      } catch (e: Exception) {
        return@use emptyResult(error = e.message)
      }
    }
  }
}
