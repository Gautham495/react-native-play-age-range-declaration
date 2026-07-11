package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import android.net.Uri
import android.util.Log

// https://developer.amazon.com/docs/app-submission/user-age-verification.html
object AmazonGetUserAgeDataProvider : StoreAgeSignalsProvider {
  private const val TAG = "PlayAgeRangeDeclaration"
  private const val AMAZONSTORE = "com.amazon.venezia"
  private const val AUTHORITY = "amzn_appstore"
  private const val PATH_GET_USER_AGE_DATA = "getUserAgeData"
  private const val COLUMN_RESPONSE_STATUS = "responseStatus"
  private const val COLUMN_USER_STATUS = "userStatus"
  private const val COLUMN_USER_ID = "userId"
  private const val COLUMN_MOST_RECENT_APPROVAL_DATE = "mostRecentApprovalDate"
  private const val COLUMN_AGE_LOWER = "ageLower"
  private const val COLUMN_AGE_UPPER = "ageUpper"
  private const val URI = "content://$AUTHORITY/$PATH_GET_USER_AGE_DATA"

  override val store = AppStore.AMAZON_APPSTORE

  override fun isAvailable(context: Context): Boolean {
    if (PlayAgeRangeDeclaration.amazonTestOption != null) return true

    return getInstallerPackageName(context) == AMAZONSTORE
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

  private fun mapResponseStatus(responseStatus: String?): AmazonGetUserAgeDataResponseStatus? {
    return when (responseStatus) {
      "SUCCESS" -> AmazonGetUserAgeDataResponseStatus.SUCCESS
      "APP_NOT_OWNED" -> AmazonGetUserAgeDataResponseStatus.APP_NOT_OWNED
      "INTERNAL_TRANSIENT_ERROR" -> AmazonGetUserAgeDataResponseStatus.INTERNAL_TRANSIENT_ERROR
      "INTERNAL_ERROR" -> AmazonGetUserAgeDataResponseStatus.INTERNAL_ERROR
      "FEATURE_NOT_SUPPORTED" -> AmazonGetUserAgeDataResponseStatus.FEATURE_NOT_SUPPORTED
      else -> null
    }
  }

  private fun emptyResult(responseStatus: AmazonGetUserAgeDataResponseStatus? = null) = AmazonGetUserAgeDataResult(
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
      // Route to the app-local AmazonTestContentProvider (see that class for scenarios).
      Uri.parse("content://${context.packageName}.amzn_test_appstore/$PATH_GET_USER_AGE_DATA?testOption=$testOption")
    } else {
      Uri.parse(URI)
    }

    val cursor = try {
      context.contentResolver.query(queryUri, null, null, null, null)
    } catch (e: Exception) {
      Log.e(TAG, "Amazon GetUserAgeData query failed", e)
      return emptyResult()
    } ?: return emptyResult()

    return cursor.use { c ->
      if (!c.moveToFirst()) return@use emptyResult()

      try {
        val responseStatus = mapResponseStatus(c.getString(c.getColumnIndexOrThrow(COLUMN_RESPONSE_STATUS)))
        // Any non-SUCCESS response carries no user data; all other columns are null.
        if (responseStatus != AmazonGetUserAgeDataResponseStatus.SUCCESS) {
          return@use emptyResult(responseStatus = responseStatus)
        }

        val ageLowerIdx = c.getColumnIndexOrThrow(COLUMN_AGE_LOWER)
        val ageUpperIdx = c.getColumnIndexOrThrow(COLUMN_AGE_UPPER)

        AmazonGetUserAgeDataResult(
          responseStatus = responseStatus,
          userStatus = mapUserStatus(c.getString(c.getColumnIndexOrThrow(COLUMN_USER_STATUS))),
          ageLower = if (c.isNull(ageLowerIdx)) null else c.getInt(ageLowerIdx).toDouble(),
          ageUpper = if (c.isNull(ageUpperIdx)) null else c.getInt(ageUpperIdx).toDouble(),
          userId = c.getString(c.getColumnIndexOrThrow(COLUMN_USER_ID)),
          mostRecentApprovalDate = c.getString(c.getColumnIndexOrThrow(COLUMN_MOST_RECENT_APPROVAL_DATE)),
        )
      } catch (e: Exception) {
        Log.e(TAG, "Amazon GetUserAgeData response parsing failed", e)
        emptyResult()
      }
    }
  }
}
