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
              val userStatusString = r.userStatus()?.toString()
              // userStatus will be empty if the user is not in a location where are legally required to show show the age verification prompt
              // This is different from UNKNOWN where the user is not verified, but is in an applicable region
              // https://developer.android.com/google/play/age-signals/use-age-signals-api#age-signals-responses
              val isEligible = !userStatusString.isNullOrEmpty()

              cont.resume(
                PlayAgeRangeDeclarationResult(
                  isEligible = isEligible,
                  installId = r.installId(),
                  userStatus = userStatusString,
                  error = null
                )
              )
            }
            .addOnFailureListener { e ->
              val msg = e.message ?: "Unknown error"
              cont.resume(
                PlayAgeRangeDeclarationResult(
                  isEligible = false,
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
          isEligible = false,
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
        isEligible = true,
        status = null,
        lowerBound = null,
        upperBound = null,
        parentControls = null
      )
    }
  }
}
