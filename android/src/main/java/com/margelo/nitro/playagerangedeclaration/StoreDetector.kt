package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import android.os.Build

object StoreDetector {
  fun detectStore(context: Context): AppStore {
    val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      runCatching {
        val info = context.packageManager.getInstallSourceInfo(context.packageName)
        info.installingPackageName ?: info.initiatingPackageName
      }.getOrNull()
    } else {
      @Suppress("DEPRECATION")
      context.packageManager.getInstallerPackageName(context.packageName)
    }
    return when (installer) {
      "com.sec.android.app.samsungapps" -> AppStore.SAMSUNG_GALAXY_STORE
      "com.amazon.venezia" -> AppStore.AMAZON_APPSTORE
      else -> AppStore.GOOGLE_PLAY
    }
  }
}
