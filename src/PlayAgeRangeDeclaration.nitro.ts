import type { HybridObject } from 'react-native-nitro-modules';

export interface PlayAgeRangeDeclarationResult {
  isEligible: boolean;
  installId?: string;
  userStatus?: string;
  error?: string;
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
