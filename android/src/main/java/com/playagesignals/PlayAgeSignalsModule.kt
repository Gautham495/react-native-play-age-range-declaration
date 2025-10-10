package com.gautham.playagesignals

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.NativeModule
import com.facebook.react.turbomodule.core.interfaces.TurboModule
import com.facebook.react.module.annotations.ReactModule
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest

@ReactModule(name = PlayAgeSignalsModule.NAME)
class PlayAgeSignalsModule(
  private val reactContext: ReactApplicationContext
) : NativeModule, TurboModule {

  companion object {
    const val NAME = "PlayAgeSignals"
  }

  override fun getName(): String = NAME
  override fun initialize() {}
  override fun invalidate() {}

  @ReactMethod
  fun getPlayAgeSignals(promise: Promise) {
    try {
      val manager = AgeSignalsManagerFactory.create(reactContext.applicationContext)
      val request = AgeSignalsRequest.builder().build()

      manager.checkAgeSignals(request)
        .addOnSuccessListener { result ->
          val map = Arguments.createMap().apply {
            putString("installId", result.installId())
            putString("userStatus", result.userStatus()?.toString() ?: "UNKNOWN")
          }
          promise.resolve(map)
        }
        .addOnFailureListener { e ->
          val msg = e.message ?: "Unknown error"
          Log.e("PlayAgeSignals", "Failed to fetch Age Signals: $msg", e)

          // Reject so JS sees the *real* native exception
          promise.reject(
            "AGE_SIGNALS_ERROR",
            "Failed to fetch Age Signals: $msg",
            e
          )
        }

    } catch (e: Exception) {
      Log.e("PlayAgeSignals", "Initialization error", e)
      promise.reject("AGE_SIGNALS_INIT_ERROR", e.message, e)
    }
  }
}
