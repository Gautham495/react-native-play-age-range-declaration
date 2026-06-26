// https://developer.samsung.com/galaxy-store/galaxy-store-content-provider-api/get-age-signals.html
export enum SamsungGetAgeSignalsUserStatus {
  VERIFIED = 0,
  SUPERVISED = 1,
  SUPERVISED_APPROVAL_PENDING = 2,
  SUPERVISED_APPROVAL_DENIED = 3,
  UNKNOWN = 4,
}

export const SamsungGetAgeSignalsUserStatusString: Record<
  SamsungGetAgeSignalsUserStatus,
  string
> = {
  [SamsungGetAgeSignalsUserStatus.VERIFIED]: 'VERIFIED',
  [SamsungGetAgeSignalsUserStatus.SUPERVISED]: 'SUPERVISED',
  [SamsungGetAgeSignalsUserStatus.SUPERVISED_APPROVAL_PENDING]:
    'SUPERVISED_APPROVAL_PENDING',
  [SamsungGetAgeSignalsUserStatus.SUPERVISED_APPROVAL_DENIED]:
    'SUPERVISED_APPROVAL_DENIED',
  [SamsungGetAgeSignalsUserStatus.UNKNOWN]: 'UNKNOWN',
} as const;

export interface SamsungGetAgeSignalsResult {
  // 0 success, 1 call failed
  result_code?: number;
  /**
   * If the result_code is 1, the reason the call failed.
   * - It is not a supported method: The method name is not correct or the
   *   function is disabled by the server. Even if the user is not subject to
   *   the current laws, this value is returned.
   * - The device is not registered SA: The device does not have a registered
   *   Samsung account.
   */
  result_message?: string;
  // Galaxy Store-generated alphanumeric ID. Null if userStatus is VERIFIED or UNKNOWN.
  installID?: string;
  userStatus?: SamsungGetAgeSignalsUserStatus;
  /**
   * The (inclusive) lower bound of the age range of a supervised user.
   * For example, if this is set to 5, the user is at least 5 years old.
   */
  ageLower?: number;
  /**
   * The (inclusive) upper bound of the age range of a supervised user.
   * For example, if this is set to 12, the user is at most 12 years old.
   */
  ageUpper?: number;
  /**
   * The effective date when the most recent significant change was approved.
   * - YYYY-MM-DD: The effective date from which the most recent significant change
   *   was approved by the supervising parent. When an app is installed, this is the
   *   date of the most recent significant change prior to the installation of the app.
   * - Null: Either the userStatus is SUPERVISED and no significant change has been
   *   submitted or userStatus is VERIFIED or UNKNOWN.
   */
  mostRecentApprovalDate?: string; // ISO 8601 format (YYYY-MM-DD)
}
