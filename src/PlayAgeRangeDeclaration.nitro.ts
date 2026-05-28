import type { HybridObject } from 'react-native-nitro-modules';

export enum PlayAgeRangeDeclarationUserStatus {
  VERIFIED = 0,
  SUPERVISED = 1,
  SUPERVISED_APPROVAL_PENDING = 2,
  SUPERVISED_APPROVAL_DENIED = 3,
  UNKNOWN = 4,
}

// https://developer.android.com/google/play/age-signals/use-age-signals-api#age-signals-responses
export const PlayAgeRangeDeclarationUserStatusString: Record<
  PlayAgeRangeDeclarationUserStatus,
  string
> = {
  [PlayAgeRangeDeclarationUserStatus.VERIFIED]: 'VERIFIED',
  [PlayAgeRangeDeclarationUserStatus.SUPERVISED]: 'SUPERVISED',
  [PlayAgeRangeDeclarationUserStatus.SUPERVISED_APPROVAL_PENDING]:
    'SUPERVISED_APPROVAL_PENDING',
  [PlayAgeRangeDeclarationUserStatus.SUPERVISED_APPROVAL_DENIED]:
    'SUPERVISED_APPROVAL_DENIED',
  [PlayAgeRangeDeclarationUserStatus.UNKNOWN]: 'UNKNOWN',
};

export interface PlayAgeRangeDeclarationResult {
  isEligible: boolean;
  installId?: string;
  userStatus?: PlayAgeRangeDeclarationUserStatus;
  error?: string;
  ageLower?: number;
  ageUpper?: number;
  mostRecentApprovalDate?: string; // ISO 8601 format (YYYY-MM-DD)
}

// https://developer.apple.com/documentation/declaredagerange/agerangeservice/agerangedeclaration#Determining-the-age-set-method
export const AppleAgeRangeDeclarationUserStatus = {
  checkedByOtherMethod: 'checkedByOtherMethod',
  governmentIDChecked: 'governmentIDChecked',
  guardianCheckedByOtherMethod: 'guardianCheckedByOtherMethod',
  guardianDeclared: 'guardianDeclared',
  guardianGovernmentIDChecked: 'guardianGovernmentIDChecked',
  guardianPaymentChecked: 'guardianPaymentChecked',
  paymentChecked: 'paymentChecked',
  selfDeclared: 'selfDeclared',

  // Library defined statuses
  declined: 'declined', // Declined sharing age range
  unknown: 'unknown', // Fallback value
} as const;

export type AppleAgeRangeDeclarationUserStatusValues =
  (typeof AppleAgeRangeDeclarationUserStatus)[keyof typeof AppleAgeRangeDeclarationUserStatus];

export interface DeclaredAgeRangeResult {
  isEligible: boolean;
  status?: AppleAgeRangeDeclarationUserStatusValues;
  parentControls?: string;
  lowerBound?: number;
  upperBound?: number;
}

export interface PlayAgeRangeDeclaration
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  getPlayAgeRangeDeclaration(): Promise<PlayAgeRangeDeclarationResult>;
  requestDeclaredAgeRange(
    firstThresholdAge: number,
    secondThresholdAge?: number,
    thirdThresholdAge?: number
  ): Promise<DeclaredAgeRangeResult>;
}
