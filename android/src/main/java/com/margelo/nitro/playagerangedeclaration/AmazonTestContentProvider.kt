package com.margelo.nitro.playagerangedeclaration

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

// https://developer.amazon.com/docs/app-submission/test-getuseragedata-api.html
//
// Implements the 11 Amazon test scenarios. Register this provider in your app's AndroidManifest.xml
// to enable testOption-based mock responses:
//
//   <provider
//       android:name="com.margelo.nitro.playagerangedeclaration.AmazonTestContentProvider"
//       android:authorities="${applicationId}.amzn_test_appstore"
//       android:exported="false" />
//
// Then activate a scenario in native code (e.g. MainActivity.kt):
//   PlayAgeRangeDeclaration.setAmazonTestOption(1)  // 18+ verified user
//   PlayAgeRangeDeclaration.setAmazonTestOption(null) // disable, use real API
//
// testOption values:
//   1  - VERIFIED (18+)
//   2  - UNKNOWN (indeterminate age)
//   3  - SUPERVISED (0-12)
//   4  - SUPERVISED (13-15)
//   5  - SUPERVISED (16-17)
//   6  - CONSENT_NOT_GRANTED
//   7  - Law not applicable (empty userStatus, isEligible = false)
//   8  - APP_NOT_OWNED error
//   9  - INTERNAL_TRANSIENT_ERROR
//   10 - INTERNAL_ERROR
//   11 - FEATURE_NOT_SUPPORTED
class AmazonTestContentProvider : ContentProvider() {

  companion object {
    private val COLUMNS = arrayOf(
      "responseStatus", "userStatus", "ageLower", "ageUpper", "userId", "mostRecentApprovalDate"
    )
    private const val TEST_USER_ID = "amazon_test_user_id"
    private const val TEST_APPROVAL_DATE = "2026-01-01T12:00:00Z"
  }

  override fun query(
    uri: Uri,
    projection: Array<String>?,
    selection: String?,
    selectionArgs: Array<String>?,
    sortOrder: String?
  ): Cursor? {
    val testOption = uri.getQueryParameter("testOption")?.toIntOrNull() ?: return null
    val cursor = MatrixCursor(COLUMNS)

    when (testOption) {
      1 -> cursor.addRow(arrayOf("SUCCESS", "VERIFIED", 18, null, null, null))
      2 -> cursor.addRow(arrayOf("SUCCESS", "UNKNOWN", null, null, null, null))
      3 -> cursor.addRow(arrayOf("SUCCESS", "SUPERVISED", 0, 12, TEST_USER_ID, TEST_APPROVAL_DATE))
      4 -> cursor.addRow(arrayOf("SUCCESS", "SUPERVISED", 13, 15, TEST_USER_ID, TEST_APPROVAL_DATE))
      5 -> cursor.addRow(arrayOf("SUCCESS", "SUPERVISED", 16, 17, TEST_USER_ID, TEST_APPROVAL_DATE))
      6 -> cursor.addRow(arrayOf("SUCCESS", "CONSENT_NOT_GRANTED", null, null, null, null))
      7 -> cursor.addRow(arrayOf("SUCCESS", "", null, null, null, null))
      8 -> cursor.addRow(arrayOf("APP_NOT_OWNED", null, null, null, null, null))
      9 -> cursor.addRow(arrayOf("INTERNAL_TRANSIENT_ERROR", null, null, null, null, null))
      10 -> cursor.addRow(arrayOf("INTERNAL_ERROR", null, null, null, null, null))
      11 -> cursor.addRow(arrayOf("FEATURE_NOT_SUPPORTED", null, null, null, null, null))
      else -> return null
    }

    return cursor
  }

  override fun onCreate() = true
  override fun getType(uri: Uri): String? = null
  override fun insert(uri: Uri, values: ContentValues?): Uri? = null
  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?) = 0
  override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?) = 0
}
