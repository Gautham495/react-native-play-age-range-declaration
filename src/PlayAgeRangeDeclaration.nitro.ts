import type { HybridObject } from 'react-native-nitro-modules';

export const PlayAgeRangeDeclarationUserStatus = {
  VERIFIED: '0',
  SUPERVISED: '1',
  SUPERVISED_APPROVAL_PENDING: '2',
  SUPERVISED_APPROVAL_DENIED: '3',
  UNKNOWN: '4',
} as const;

export const PlayAgeRangeDeclarationUserStatusString: Record<string, string> = {
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
  userStatus?: string;
  error?: string;
  ageLower?: number;
  ageUpper?: number;
  mostRecentApprovalDate?: string; // ISO 8601 format (YYYY-MM-DD)
}

export interface DeclaredAgeRangeResult {
  isEligible: boolean;
  status?: string;
  parentControls?: string;
  lowerBound?: number;
  upperBound?: number;
}

export interface PlayAgeRangeDeclaration
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  getPlayAgeRangeDeclaration(): Promise<PlayAgeRangeDeclarationResult>;
  requestDeclaredAgeRange(
    firstThresholdAge: number,
    secondThresholdAge: number,
    thirdThresholdAge: number
  ): Promise<DeclaredAgeRangeResult>;
}
