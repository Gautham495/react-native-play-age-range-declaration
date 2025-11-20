import type { HybridObject } from 'react-native-nitro-modules';

export interface PlayAgeRangeDeclarationResult {
  installId?: string;
  userStatus?: string;
  error?: string;
}

export interface DeclaredAgeRangeResult {
  status?: string;
  lowerBound?: number;
  upperBound?: number;
  error?: string;
}

export interface PlayAgeRangeDeclaration
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  getPlayAgeRangeDeclaration(): Promise<PlayAgeRangeDeclarationResult>;
  requestDeclaredAgeRange(ageGate: number): Promise<DeclaredAgeRangeResult>;
}
