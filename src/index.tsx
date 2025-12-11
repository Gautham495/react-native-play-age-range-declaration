import { NitroModules } from 'react-native-nitro-modules';
import { Platform } from 'react-native';

import type {
  DeclaredAgeRangeResult,
  PlayAgeRangeDeclaration,
  PlayAgeRangeDeclarationResult,
} from './PlayAgeRangeDeclaration.nitro';

import { ageRangeThresholdManager } from './AgeRangeThresholdManager';

import {
  getIsConsideredOlderThaniOS,
  getIsConsideredOlderThanAndroid,
} from './isConsideredOlderThan';
const PlayAgeRangeDeclarationHybridObject =
  NitroModules.createHybridObject<PlayAgeRangeDeclaration>(
    'PlayAgeRangeDeclaration'
  );

export async function getAppleDeclaredAgeRangeStatus(): Promise<DeclaredAgeRangeResult> {
  const thresholds = ageRangeThresholdManager.getThresholds();

  return await PlayAgeRangeDeclarationHybridObject.requestDeclaredAgeRange(
    thresholds[0],
    thresholds[1],
    thresholds[2]
  );
}

export async function getAndroidPlayAgeRangeStatus(): Promise<PlayAgeRangeDeclarationResult> {
  return await PlayAgeRangeDeclarationHybridObject.getPlayAgeRangeDeclaration();
}

export const setAgeRangeThresholds = (
  thresholds: [number, number?, number?]
): void => {
  ageRangeThresholdManager.setAgeRangeThresholds(thresholds);
};

export type { DeclaredAgeRangeResult, PlayAgeRangeDeclarationResult };

export const getIsConsideredOlderThan = async (
  age: number
): Promise<boolean> => {
  if (Platform.OS === 'ios') {
    const ageData = await getAppleDeclaredAgeRangeStatus();
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
