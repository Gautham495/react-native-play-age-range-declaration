import type { HybridObject } from 'react-native-nitro-modules';

export interface PlayAgeRangeDeclarationResult {
  installId?: string;
  userStatus?: string;
  error?: string;
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
