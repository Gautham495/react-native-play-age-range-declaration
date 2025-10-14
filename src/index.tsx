import { Platform } from 'react-native';

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

export async function getAgeData(
  ageGate: number
): Promise<PlayAgeRangeDeclarationResult | DeclaredAgeRangeResult> {
  if (Platform.OS === 'ios') {
    return await PlayAgeRangeDeclarationHybridObject.requestDeclaredAgeRange(
      ageGate
    );
  } else {
    return await PlayAgeRangeDeclarationHybridObject.getPlayAgeRangeDeclaration();
  }
}