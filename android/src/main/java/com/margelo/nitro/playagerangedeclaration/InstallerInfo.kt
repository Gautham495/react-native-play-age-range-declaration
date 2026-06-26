package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import android.os.Build

fun getInstallerPackageName(context: Context): String? {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    runCatching {
      val info = context.packageManager.getInstallSourceInfo(context.packageName)
      info.installingPackageName ?: info.initiatingPackageName
    }.getOrNull()
  } else {
    @Suppress("DEPRECATION")
    context.packageManager.getInstallerPackageName(context.packageName)
  }
}
