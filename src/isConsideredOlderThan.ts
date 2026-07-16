import { AgeSignalsUserStatus, type AgeSignalsResult } from './AgeSignals';
import type { DeclaredAgeRangeResult } from './providers/AppleDeclaredAgeRange';

/**
 * Whether a normalized Android age-signals result should be treated as
 * "older than `age`".
 *
 * Deliberately fails open: when the store call failed, or the user is not in
 * a region where an age-verification law applies, the user is considered
 * older so the app keeps working for the majority of users the laws don't
 * cover.
 */
const getIsConsideredOlderThanAgeSignals = (
  ageData: AgeSignalsResult<unknown>,
  age: number
): boolean => {
  // The store call failed — fail open rather than lock the user out.
  if (ageData.error != null) return true;

  // No signal: the user is not in a region where an age-verification law
  // applies (or, on Google Play, did not consent to share their age).
  if (ageData.userStatus == null) return true;

  switch (ageData.userStatus) {
    // The store verified the user is 18+.
    case AgeSignalsUserStatus.VERIFIED:
      return true;

    // An age range is known (declared by the user or set by a parent) — gate
    // on its lower bound.
    // TODO: For SUPERVISED_APPROVAL_PENDING / DENIED we should also check
    // mostRecentApprovalDate; we do not yet support notification of a
    // significant change that would require the user to be re-verified.
    case AgeSignalsUserStatus.DECLARED:
    case AgeSignalsUserStatus.SUPERVISED:
    case AgeSignalsUserStatus.SUPERVISED_APPROVAL_PENDING:
    case AgeSignalsUserStatus.SUPERVISED_APPROVAL_DENIED:
      return ageData.ageLower != null && ageData.ageLower >= age;

    // Consent was revoked/denied, or the user is in an applicable region but
    // their age could not be determined — treat as not older.
    case AgeSignalsUserStatus.CONSENT_NOT_GRANTED:
    case AgeSignalsUserStatus.UNKNOWN:
    default:
      return false;
  }
};

const getIsConsideredOlderThaniOS = (
  ageData: DeclaredAgeRangeResult,
  age: number
): boolean => {
  // The user is not in an applicable region — consider them older than the age.
  if (!ageData.isEligible) return true;

  return ageData.lowerBound != null && ageData.lowerBound >= age;
};

export { getIsConsideredOlderThaniOS, getIsConsideredOlderThanAgeSignals };
