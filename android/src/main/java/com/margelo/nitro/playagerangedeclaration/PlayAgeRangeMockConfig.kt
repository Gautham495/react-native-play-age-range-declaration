package com.margelo.nitro.playagerangedeclaration

import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus
import java.util.Date

data class PlayAgeRangeMockConfig(
  // Required fields
  @AgeSignalsVerificationStatus val userStatus: Int,

  // Optional fields
  val ageLower: Int? = null,
  val ageUpper: Int? = null,
  val installId: String? = null,
  val mostRecentApprovalDate: Date? = null,
)
