package com.margelo.nitro.playagerangedeclaration

import android.content.Context

/**
 * A store-specific source of age signals.
 *
 * Detection precedence is defined by the order of
 * [PlayAgeRangeDeclaration.providers].
 */
interface StoreAgeSignalsProvider {
  /** The store whose age-signals API this provider reads. */
  val store: AppStore

  /** Whether this store's age-signals API can serve the current install. */
  fun isAvailable(context: Context): Boolean
}
