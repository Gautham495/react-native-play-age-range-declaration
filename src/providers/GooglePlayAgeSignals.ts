// https://developer.android.com/google/play/age-signals/use-age-signals-api#age-signals-responses
export enum PlayAgeSignalsUserStatus {
  VERIFIED = 0,
  SUPERVISED = 1,
  SUPERVISED_APPROVAL_PENDING = 2,
  SUPERVISED_APPROVAL_DENIED = 3,
  UNKNOWN = 4,
}

export const PlayAgeSignalsUserStatusString: Record<
  PlayAgeSignalsUserStatus,
  string
> = {
  [PlayAgeSignalsUserStatus.VERIFIED]: 'VERIFIED',
  [PlayAgeSignalsUserStatus.SUPERVISED]: 'SUPERVISED',
  [PlayAgeSignalsUserStatus.SUPERVISED_APPROVAL_PENDING]:
    'SUPERVISED_APPROVAL_PENDING',
  [PlayAgeSignalsUserStatus.SUPERVISED_APPROVAL_DENIED]:
    'SUPERVISED_APPROVAL_DENIED',
  [PlayAgeSignalsUserStatus.UNKNOWN]: 'UNKNOWN',
};

export interface PlayAgeSignalsResult {
  installId?: string;
  userStatus?: PlayAgeSignalsUserStatus;
  error?: string;
  ageLower?: number;
  ageUpper?: number;
  mostRecentApprovalDate?: string; // ISO 8601 format (YYYY-MM-DD)
}

export interface PlayAgeSignalsMockConfig {
  userStatus: PlayAgeSignalsUserStatus;
  ageLower?: number;
  ageUpper?: number;
  installId?: string;
  mostRecentApprovalDate?: string; // ISO 8601 format (YYYY-MM-DD)
}
