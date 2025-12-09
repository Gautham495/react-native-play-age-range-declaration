import { NitroModules } from 'react-native-nitro-modules';
import { Platform } from 'react-native';

import type {
  DeclaredAgeRangeResult,
  PlayAgeRangeDeclaration,
  PlayAgeRangeDeclarationResult,
} from './PlayAgeRangeDeclaration.nitro';

import {
  getIsConsideredOlderThaniOS,
  getIsConsideredOlderThanAndroid,
} from './isConsideredOlderThan';
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

export const getIsConsideredOlderThan = async (
  age: number
): Promise<boolean> => {
  if (Platform.OS === 'ios') {
    const ageData = await getAppleDeclaredAgeRangeStatus(10, 13, 16);
    return getIsConsideredOlderThaniOS(ageData, age);
  } else {
    const ageData = await getAndroidPlayAgeRangeStatus();
    return getIsConsideredOlderThanAndroid(ageData, age);
  }
};

export {
  PlayAgeRangeDeclarationUserStatus,
  PlayAgeRangeDeclarationUserStatusString,
} from './PlayAgeRangeDeclaration.nitro';
