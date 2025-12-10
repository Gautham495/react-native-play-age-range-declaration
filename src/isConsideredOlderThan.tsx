import {
  PlayAgeRangeDeclarationUserStatus,
  type DeclaredAgeRangeResult,
  type PlayAgeRangeDeclarationResult,
} from './PlayAgeRangeDeclaration.nitro';

const getIsConsideredOlderThaniOS = async (
  ageData: DeclaredAgeRangeResult,
  age: number
): Promise<boolean> => {
  // The user is not in an applicable region, we should consider them older than the age.
  if (!ageData.isEligible) return true;

  return Boolean(ageData.lowerBound && ageData.lowerBound >= age);
};

const getIsConsideredOlderThanAndroid = async (
  ageData: PlayAgeRangeDeclarationResult,
  age: number
): Promise<boolean> => {
  // The user is not in an applicable region, we should consider them older than the age. This is the empty state for userStatus.
  if (!ageData.isEligible) return true;

  switch (ageData.userStatus) {
    // The user is over 18. Google verified the user's age using a commercially reasonable method such as a government-issued ID, credit card, or facial age estimation.
    case PlayAgeRangeDeclarationUserStatus.VERIFIED:
      return true;

    // The user has a supervised Google Account managed by a parent who sets their age. Use ageLower and ageUpper to determine the user's age range.
    case PlayAgeRangeDeclarationUserStatus.SUPERVISED:
      return Boolean(ageData.ageLower && ageData.ageLower >= age);

    // The user has a supervised Google Account, and their supervising parent has not yet approved one or more pending significant changes. Use ageLower and ageUpper to determine the user's age range. Use mostRecentApprovalDate to determine the last significant change that was approved.
    // TODO: We should check the mostRecentApprovalDate to determine the last significant change that was approved. We currently do not support notification of a significant change that would require the user to be re-verified
    case PlayAgeRangeDeclarationUserStatus.SUPERVISED_APPROVAL_PENDING:
      return Boolean(ageData.ageLower && ageData.ageLower >= age);

    // The user has a supervised Google Account, and their supervising parent denied approval for one or more significant changes. Use ageLower and ageUpper to determine the user's age range. Use mostRecentApprovalDate to determine the last significant change that was approved.
    // TODO: We should check the mostRecentApprovalDate to determine the last significant change that was approved. We currently do not support notification of a significant change that would require the user to be re-verified
    case PlayAgeRangeDeclarationUserStatus.SUPERVISED_APPROVAL_DENIED:
      return Boolean(ageData.ageLower && ageData.ageLower >= age);

    // The user is not verified or supervised in applicable jurisdictions and regions. These users could be over or under 18. To obtain an age signal from Google Play, ask the user to visit the Play Store to resolve their status.
    case PlayAgeRangeDeclarationUserStatus.UNKNOWN:
      return false;

    default:
      return false;
  }
};

export { getIsConsideredOlderThaniOS, getIsConsideredOlderThanAndroid };
