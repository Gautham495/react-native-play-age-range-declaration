import { type DeclaredAgeRangeResult } from './providers/AppleDeclaredAgeRange';
import {
  type AmazonGetUserAgeDataResult,
  AmazonGetUserAgeDataUserStatus,
  AmazonGetUserAgeDataResponseStatus,
} from './providers/AmazonGetUserAgeData';
import {
  type PlayAgeSignalsResult,
  PlayAgeSignalsUserStatus,
} from './providers/GooglePlayAgeSignals';
import {
  type SamsungGetAgeSignalsResult,
  SamsungGetAgeSignalsUserStatus,
} from './providers/SamsungGetAgeSignals';

const getIsConsideredOlderThaniOS = async (
  ageData: DeclaredAgeRangeResult,
  age: number
): Promise<boolean> => {
  // The user is not in an applicable region, we should consider them older than the age.
  if (!ageData.isEligible) return true;

  return Boolean(ageData.lowerBound && ageData.lowerBound >= age);
};

const getIsConsideredOlderThanAmazon = async (
  ageData: AmazonGetUserAgeDataResult,
  age: number
): Promise<boolean> => {
  // The request failed.
  // TODO Add retry logic for INTERNAL_TRANSIENT_ERROR
  if (ageData.responseStatus !== AmazonGetUserAgeDataResponseStatus.SUCCESS)
    return true;

  // User is in a region where age verification law isn't applicable
  if (ageData.responseStatus === undefined) return true;

  switch (ageData.userStatus) {
    /**
     * For a user who is 18+ years old in a region where the age verification
     * law is applicable, indicates the user's age is verified.
     */
    case AmazonGetUserAgeDataUserStatus.VERIFIED:
      return true;

    /**
     * For a user who is under 18 years old in a region where the age
     * verification law is applicable, indicates a parent or guardian
     * provided consent.
     */
    case AmazonGetUserAgeDataUserStatus.SUPERVISED:
      return Boolean(ageData.ageLower && ageData.ageLower >= age);

    /**
     * In a region where the age verification law is applicable, indicates one of the following:
     * - The parent or guardian revoked their consent.
     * - There is a significant change in effect, and the re-consent of the parent or guardian is pending.
     * - There is a significant change in effect, and the parent or guardian denied the re-consent request.
     */
    case AmazonGetUserAgeDataUserStatus.CONSENT_NOT_GRANTED:
      return false;

    /**
     * In jurisdictions or regions where age verification law is applicable,
     * the user's age and consent status is not verified. The age range of
     * the user is unconfirmed.
     */
    case AmazonGetUserAgeDataUserStatus.UNKNOWN:
      return false;

    default:
      return false;
  }
};

const getIsConsideredOlderThanGooglePlay = async (
  ageData: PlayAgeSignalsResult,
  age: number
): Promise<boolean> => {
  // The user is not in an applicable region, we should consider them older than the age. This is the empty state for userStatus.
  if (ageData.userStatus === undefined) return true;

  switch (ageData.userStatus) {
    // The user is over 18. Google verified the user's age using a commercially reasonable method such as a government-issued ID, credit card, or facial age estimation.
    case PlayAgeSignalsUserStatus.VERIFIED:
      return true;

    // The user has a supervised Google Account managed by a parent who sets their age. Use ageLower and ageUpper to determine the user's age range.
    case PlayAgeSignalsUserStatus.SUPERVISED:
      return Boolean(ageData.ageLower && ageData.ageLower >= age);

    // The user has a supervised Google Account, and their supervising parent has not yet approved one or more pending significant changes. Use ageLower and ageUpper to determine the user's age range. Use mostRecentApprovalDate to determine the last significant change that was approved.
    // TODO: We should check the mostRecentApprovalDate to determine the last significant change that was approved. We currently do not support notification of a significant change that would require the user to be re-verified
    case PlayAgeSignalsUserStatus.SUPERVISED_APPROVAL_PENDING:
      return Boolean(ageData.ageLower && ageData.ageLower >= age);

    // The user has a supervised Google Account, and their supervising parent denied approval for one or more significant changes. Use ageLower and ageUpper to determine the user's age range. Use mostRecentApprovalDate to determine the last significant change that was approved.
    // TODO: We should check the mostRecentApprovalDate to determine the last significant change that was approved. We currently do not support notification of a significant change that would require the user to be re-verified
    case PlayAgeSignalsUserStatus.SUPERVISED_APPROVAL_DENIED:
      return Boolean(ageData.ageLower && ageData.ageLower >= age);

    // The user is not verified or supervised in applicable jurisdictions and regions. These users could be over or under 18. To obtain an age signal from Google Play, ask the user to visit the Play Store to resolve their status.
    case PlayAgeSignalsUserStatus.UNKNOWN:
      return false;

    default:
      return false;
  }
};

const getIsConsideredOlderThanSamsungGalaxy = async (
  ageData: SamsungGetAgeSignalsResult,
  age: number
): Promise<boolean> => {
  // The user is not in an applicable region, we should consider them older than the age. This is the empty state for userStatus.
  if (ageData.userStatus === undefined) return true;

  switch (ageData.userStatus) {
    // The user is 18 years of age or older.
    case SamsungGetAgeSignalsUserStatus.VERIFIED:
      return true;

    /**
     * The user has a supervised Samsung account that is managed by a parent who
     * sets their age.
     */
    case SamsungGetAgeSignalsUserStatus.SUPERVISED:
      return Boolean(ageData.ageLower && ageData.ageLower >= age);

    /**
     * The user has a supervised Samsung account and the supervising parent has
     * not yet approved one or more pending significant changes.
     */
    // TODO: We should check the mostRecentApprovalDate to determine the last significant change that was approved. We currently do not support notification of a significant change that would require the user to be re-verified
    case SamsungGetAgeSignalsUserStatus.SUPERVISED_APPROVAL_PENDING:
      return Boolean(ageData.ageLower && ageData.ageLower >= age);

    /**
     * The user has a supervised Samsung account that is managed by a parent who
     * sets their age and the supervising parent has denied approval for one or
     * more pending significant changes.
     */
    // TODO: We should check the mostRecentApprovalDate to determine the last significant change that was approved. We currently do not support notification of a significant change that would require the user to be re-verified
    case SamsungGetAgeSignalsUserStatus.SUPERVISED_APPROVAL_DENIED:
      return Boolean(ageData.ageLower && ageData.ageLower >= age);

    /**
     * There could be one or more reasons for this value:
     * - The user has not agreed to the Galaxy Store's terms and conditions
     * - The installer information of the currently installed app is not Galaxy Store.
     * - The user is a minor who is not a member of a Samsung account family group.
     * - The user is a minor but the Samsung parental controls are disabled.
     */
    case SamsungGetAgeSignalsUserStatus.UNKNOWN:
      return false;

    default:
      return false;
  }
};

export {
  getIsConsideredOlderThaniOS,
  getIsConsideredOlderThanAmazon,
  getIsConsideredOlderThanGooglePlay,
  getIsConsideredOlderThanSamsungGalaxy,
};
