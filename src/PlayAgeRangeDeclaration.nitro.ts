import type { HybridObject } from 'react-native-nitro-modules';

export enum PlayAgeRangeDeclarationUserStatus {
  VERIFIED = '0',
  SUPERVISED = '1',
  SUPERVISED_APPROVAL_PENDING = '2',
  SUPERVISED_APPROVAL_DENIED = '3',
  UNKNOWN = '4',
}

export const PlayAgeRangeDeclarationUserStatusString = {
  [PlayAgeRangeDeclarationUserStatus.VERIFIED]: 'VERIFIED',
  [PlayAgeRangeDeclarationUserStatus.SUPERVISED]: 'SUPERVISED',
  [PlayAgeRangeDeclarationUserStatus.SUPERVISED_APPROVAL_PENDING]:
    'SUPERVISED_APPROVAL_PENDING',
  [PlayAgeRangeDeclarationUserStatus.SUPERVISED_APPROVAL_DENIED]:
    'SUPERVISED_APPROVAL_DENIED',
  [PlayAgeRangeDeclarationUserStatus.UNKNOWN]: 'UNKNOWN',
} as const;

export interface PlayAgeRangeDeclarationResult {
  installId?: string;
  userStatus?: string;
  error?: string;
  ageLower?: number;
  ageUpper?: number;
  mostRecentApprovalDate?: string; // ISO 8601 format (YYYY-MM-DD)
}

export interface DeclaredAgeRangeResult {
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
