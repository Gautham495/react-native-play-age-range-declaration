import type { HybridObject } from 'react-native-nitro-modules';

export interface PlayAgeRangeDeclarationResult {
  installId?: string | null;
  userStatus?: string | null;
  error?: string | null;
}

export interface DeclaredAgeRangeResult {
  status?: string | null;
  lowerBound?: number | null;
  upperBound?: number | null;
  error?: string | null;
}

export interface PlayAgeRangeDeclaration
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  getPlayAgeRangeDeclaration(): Promise<PlayAgeRangeDeclarationResult>;
  requestDeclaredAgeRange(ageGate: number): Promise<DeclaredAgeRangeResult>;
}
