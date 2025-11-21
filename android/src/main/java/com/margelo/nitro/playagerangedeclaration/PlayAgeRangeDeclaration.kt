package com.margelo.nitro.playagerangedeclaration

import android.content.Context
import android.util.Log
import com.facebook.proguard.annotations.DoNotStrip
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import com.margelo.nitro.core.Promise
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@DoNotStrip
class PlayAgeRangeDeclaration(private val appContext: Context) : HybridPlayAgeRangeDeclarationSpec() {

  override fun getPlayAgeRangeDeclaration(): Promise<PlayAgeRangeDeclarationResult> {
    return Promise.async {
      try {
        val manager = AgeSignalsManagerFactory.create(appContext)
        val request = AgeSignalsRequest.builder().build()
        
        val result = suspendCancellableCoroutine<PlayAgeRangeDeclarationResult> { cont ->
          manager.checkAgeSignals(request)
            .addOnSuccessListener { r ->
              cont.resume(
                PlayAgeRangeDeclarationResult(
                  installId = r.installId(),
                  userStatus = r.userStatus()?.toString() ?: "UNKNOWN",
                  error = null
                )
              )
            }
            .addOnFailureListener { e ->
              val msg = e.message ?: "Unknown error"
              cont.resume(
                PlayAgeRangeDeclarationResult(
                  installId = null,
                  userStatus = null,
                  error = "$msg"
                )
              )
            }
        }

        result
      } catch (e: Exception) {
        Log.e("PlayAgeRangeDeclaration", "Initialization error", e)
        PlayAgeRangeDeclarationResult(
          installId = null,
          userStatus = null,
          error = "AGE_SIGNALS_INIT_ERROR: ${e.message}"
        )
      }
    }
  }

  override fun requestDeclaredAgeRange(firstThresholdAge: Double, secondThresholdAge: Double, thirdThresholdAge: Double): Promise<DeclaredAgeRangeResult> {
    return Promise.async {
      DeclaredAgeRangeResult(
        status = null,
        lowerBound = null,
        upperBound = null,
        parentControls = null
      )
    }
  }
}
