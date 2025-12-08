import { NitroModules } from 'react-native-nitro-modules';

import type {
  DeclaredAgeRangeResult,
  PlayAgeRangeDeclaration,
  PlayAgeRangeDeclarationResult,
} from './PlayAgeRangeDeclaration.nitro';

const PlayAgeRangeDeclarationHybridObject =
  NitroModules.createHybridObject<PlayAgeRangeDeclaration>(
    'PlayAgeRangeDeclaration'
  );

export async function getAppleDeclaredAgeRangeStatus(
  firstThresholdAge: number,
  secondThresholdAge: number,
  thirdThresholdAge: number
): Promise<DeclaredAgeRangeResult> {
  return await PlayAgeRangeDeclarationHybridObject.requestDeclaredAgeRange(
    firstThresholdAge,
    secondThresholdAge,
    thirdThresholdAge
  );
}

export async function getAndroidPlayAgeRangeStatus(): Promise<PlayAgeRangeDeclarationResult> {
  return await PlayAgeRangeDeclarationHybridObject.getPlayAgeRangeDeclaration();
}

// Export types for consumers
export type { DeclaredAgeRangeResult, PlayAgeRangeDeclarationResult };

export {
  PlayAgeRangeDeclarationUserStatus,
  PlayAgeRangeDeclarationUserStatusString,
} from './PlayAgeRangeDeclaration.nitro';
