package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log

// https://developer.samsung.com/galaxy-store/galaxy-store-content-provider-api/get-age-signals.html
object SamsungGetAgeSignalsProvider : StoreAgeSignalsProvider {
  private const val TAG = "PlayAgeRangeDeclaration"
  private const val GALAXYSTORE = "com.sec.android.app.samsungapps"
  private const val ASAA_META = "$GALAXYSTORE.AccountabilityActProvider.version"
  private const val ASAA_AUTHORITY = "$GALAXYSTORE.provider.ASAA"
  private const val QUERY_SETTINGS = "settings"
  private const val URI_ASAA_SETTINGS = "content://$ASAA_AUTHORITY/$QUERY_SETTINGS"
  private const val METHOD_GET_AGE_SIGNAL_RESULT = "getAgeSignalResult"
  private const val KEY_RESULT_CODE = "result_code"
  private const val KEY_RESULT_MESSAGE = "result_message"
  private const val KEY_RESULT_USER_STATUS = "userStatus"
  private const val KEY_RESULT_AGE_LOWER = "ageLower"
  private const val KEY_RESULT_AGE_UPPER = "ageUpper"
  private const val KEY_RESULT_APPROVAL_DATE = "mostRecentApprovalDate"
  private const val KEY_RESULT_INSTALLID = "installID"
  private const val KEY_ASAA_ENABLED = "gs_asaa_enable"

  override val store = AppStore.SAMSUNG_GALAXY_STORE

  override fun isAvailable(context: Context): Boolean {
    if (PlayAgeRangeDeclaration.samsungMockScenario != null) return true

    if (getInstallerPackageName(context) != GALAXYSTORE) return false

    // The Galaxy Store must ship an ASAA provider of version >= 1.0
    // (Galaxy Store 4.6.03.1 or higher).
    val version = runCatching {
      context.packageManager
        .getApplicationInfo(GALAXYSTORE, PackageManager.GET_META_DATA)
        .metaData?.getFloat(ASAA_META, 0f) ?: 0f
    }.getOrDefault(0f)

    return version >= 1.0f
  }

  private fun mapUserStatus(userStatus: String?): SamsungGetAgeSignalsUserStatus? {
    return when (userStatus) {
      "VERIFIED" -> SamsungGetAgeSignalsUserStatus.VERIFIED
      "SUPERVISED" -> SamsungGetAgeSignalsUserStatus.SUPERVISED
      "SUPERVISED_APPROVAL_PENDING" -> SamsungGetAgeSignalsUserStatus.SUPERVISED_APPROVAL_PENDING
      "SUPERVISED_APPROVAL_DENIED" -> SamsungGetAgeSignalsUserStatus.SUPERVISED_APPROVAL_DENIED
      "UNKNOWN" -> SamsungGetAgeSignalsUserStatus.UNKNOWN
      else -> null
    }
  }

  private fun emptyResult(resultCode: Double? = null, resultMessage: String? = null) = SamsungGetAgeSignalsResult(
    result_code = resultCode,
    result_message = resultMessage,
    installID = null,
    userStatus = null,
    ageLower = null,
    ageUpper = null,
    mostRecentApprovalDate = null,
  )

  fun getAgeSignals(context: Context): SamsungGetAgeSignalsResult {
    val mockScenario = PlayAgeRangeDeclaration.samsungMockScenario
    val callUri: Uri
    val callArg: String?

    if (mockScenario != null) {
      // Route to the app-local SamsungTestContentProvider (see that class for scenarios).
      callUri = Uri.parse("content://${context.packageName}.samsung_test_provider/$QUERY_SETTINGS")
      callArg = mockScenario.toString()
    } else {
      // ASAA only serves age signals when Samsung's regulatory-compliance flag
      // is enabled for this device.
      val asaaEnabled = Settings.Secure.getString(context.contentResolver, KEY_ASAA_ENABLED)
      if (asaaEnabled != "1") return emptyResult()
      callUri = Uri.parse(URI_ASAA_SETTINGS)
      callArg = null
    }

    val bundle = try {
      context.contentResolver.call(callUri, METHOD_GET_AGE_SIGNAL_RESULT, callArg, null)
    } catch (e: Exception) {
      Log.e(TAG, "Samsung getAgeSignalResult call failed", e)
      return emptyResult()
    } ?: return emptyResult()

    val resultCode = bundle.getInt(KEY_RESULT_CODE, 1)
    if (resultCode != 0) {
      val message = bundle.getString(KEY_RESULT_MESSAGE) ?: "Unknown Samsung error"
      return emptyResult(resultCode = resultCode.toDouble(), resultMessage = message)
    }

    return SamsungGetAgeSignalsResult(
      result_code = resultCode.toDouble(),
      result_message = null,
      installID = bundle.getString(KEY_RESULT_INSTALLID),
      userStatus = mapUserStatus(bundle.getString(KEY_RESULT_USER_STATUS)),
      ageLower = bundle.getString(KEY_RESULT_AGE_LOWER)?.toDoubleOrNull(),
      ageUpper = bundle.getString(KEY_RESULT_AGE_UPPER)?.toDoubleOrNull(),
      mostRecentApprovalDate = bundle.getString(KEY_RESULT_APPROVAL_DATE),
    )
  }
}
