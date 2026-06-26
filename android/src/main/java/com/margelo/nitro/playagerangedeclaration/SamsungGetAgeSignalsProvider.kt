package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings


// https://developer.samsung.com/galaxy-store/galaxy-store-content-provider-api/get-age-signals.html
object SamsungAgeSignalsProvider {
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

  fun isAvailable(context: Context): Boolean {
    val installer = getInstallerPackageName(context)

    if (installer != GALAXYSTORE) return false

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

  private fun emptyResult(error: String? = null, resultCode: Double? = null, resultMessage: String? = null) = SamsungGetAgeSignalsResult(
    result_code = resultCode,
    result_message = resultMessage,
    installID = null,
    userStatus = null,
    ageLower = null,
    ageUpper = null,
    mostRecentApprovalDate = null,
  )

  fun getAgeSignals(context: Context): SamsungGetAgeSignalsResult {
    val asaaEnabled = Settings.Secure.getString(context.contentResolver, KEY_ASAA_ENABLED)
    if (asaaEnabled != "1") return emptyResult()

    val bundle = try {
      context.contentResolver.call(Uri.parse(URI_ASAA_SETTINGS), METHOD_GET_AGE_SIGNAL_RESULT, null, null)
    } catch (e: Exception) {
      return emptyResult(error = e.message)
    }

    if (bundle == null) {
      return emptyResult()
    }

    val resultCode = bundle.getInt(KEY_RESULT_CODE, 1)
    if (resultCode != 0) {
      val message = bundle.getString(KEY_RESULT_MESSAGE) ?: "Unknown Samsung error"
      return emptyResult(resultCode = resultCode.toDouble(), resultMessage = message)
    }

    val userStatus = mapUserStatus(bundle.getString(KEY_RESULT_USER_STATUS))

    return SamsungGetAgeSignalsResult(
      result_code = resultCode.toDouble(),
      result_message = null,
      installID = bundle.getString(KEY_RESULT_INSTALLID),
      userStatus = userStatus,
      ageLower = bundle.getString(KEY_RESULT_AGE_LOWER)?.toDoubleOrNull(),
      ageUpper = bundle.getString(KEY_RESULT_AGE_UPPER)?.toDoubleOrNull(),
      mostRecentApprovalDate = bundle.getString(KEY_RESULT_APPROVAL_DATE),
    )
  }
}
