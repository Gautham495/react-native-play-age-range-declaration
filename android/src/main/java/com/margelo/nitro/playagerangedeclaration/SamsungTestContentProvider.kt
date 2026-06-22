package com.margelo.nitro.playagerangedeclaration

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle

// https://developer.samsung.com/galaxy-store/galaxy-store-content-provider-api/get-age-signals.html
//
// Implements Samsung ASAA test scenarios. Register this provider in your app's AndroidManifest.xml
// to enable testOption-based mock responses:
//
//   <provider
//       android:name="com.margelo.nitro.playagerangedeclaration.SamsungTestContentProvider"
//       android:authorities="${applicationId}.samsung_test_provider"
//       android:exported="false" />
//
// Then activate a scenario in native code (e.g. MainActivity.kt):
//   PlayAgeRangeDeclaration.setSamsungTestOption(1)  // 18+ verified user
//   PlayAgeRangeDeclaration.setSamsungTestOption(null)  // disable, use real API
//
// testOption values:
//   1  - VERIFIED (18+)
//   2  - UNKNOWN (indeterminate age)
//   3  - SUPERVISED (0-12)
//   4  - SUPERVISED (13-15)
//   5  - SUPERVISED (16-17)
//   6  - SUPERVISED_APPROVAL_PENDING
//   7  - SUPERVISED_APPROVAL_DENIED
//   8  - Unsupported method error (result_code = 1)
//   9 - Device not registered error (result_code = 1)
class SamsungTestContentProvider : ContentProvider() {

  companion object {
    private const val TEST_INSTALL_ID = "samsung_test_install_id"
    private const val TEST_APPROVAL_DATE = "2026-01-01"
  }

  override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
    val testOption = arg?.toIntOrNull() ?: return null
    val bundle = Bundle()

    when (testOption) {
      1 -> {
        bundle.putInt("result_code", 0)
        bundle.putString("userStatus", "VERIFIED")
        bundle.putString("ageLower", "18")
      }
      2 -> {
        bundle.putInt("result_code", 0)
        bundle.putString("userStatus", "UNKNOWN")
      }
      3 -> {
        bundle.putInt("result_code", 0)
        bundle.putString("userStatus", "SUPERVISED")
        bundle.putString("ageLower", "0")
        bundle.putString("ageUpper", "12")
        bundle.putString("installID", TEST_INSTALL_ID)
        bundle.putString("mostRecentApprovalDate", TEST_APPROVAL_DATE)
      }
      4 -> {
        bundle.putInt("result_code", 0)
        bundle.putString("userStatus", "SUPERVISED")
        bundle.putString("ageLower", "13")
        bundle.putString("ageUpper", "15")
        bundle.putString("installID", TEST_INSTALL_ID)
        bundle.putString("mostRecentApprovalDate", TEST_APPROVAL_DATE)
      }
      5 -> {
        bundle.putInt("result_code", 0)
        bundle.putString("userStatus", "SUPERVISED")
        bundle.putString("ageLower", "16")
        bundle.putString("ageUpper", "17")
        bundle.putString("installID", TEST_INSTALL_ID)
        bundle.putString("mostRecentApprovalDate", TEST_APPROVAL_DATE)
      }
      6 -> {
        bundle.putInt("result_code", 0)
        bundle.putString("userStatus", "SUPERVISED_APPROVAL_PENDING")
        bundle.putString("ageLower", "13")
        bundle.putString("ageUpper", "15")
        bundle.putString("installID", TEST_INSTALL_ID)
      }
      7 -> {
        bundle.putInt("result_code", 0)
        bundle.putString("userStatus", "SUPERVISED_APPROVAL_DENIED")
        bundle.putString("ageLower", "13")
        bundle.putString("ageUpper", "15")
        bundle.putString("installID", TEST_INSTALL_ID)
      }
      8 -> {
        bundle.putInt("result_code", 1)
        bundle.putString("result_message", "It is not a supported method")
      }
      9 -> {
        bundle.putInt("result_code", 1)
        bundle.putString("result_message", "The device is not registered SA")
      }
      else -> return null
    }

    return bundle
  }

  override fun onCreate() = true
  override fun getType(uri: Uri): String? = null
  override fun insert(uri: Uri, values: ContentValues?): Uri? = null
  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?) = 0
  override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?) = 0
  override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? = null
}
