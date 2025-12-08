import { NitroModules } from 'react-native-nitro-modules';
import { Platform } from 'react-native';

import type {
  DeclaredAgeRangeResult,
  PlayAgeRangeDeclaration,
  PlayAgeRangeDeclarationResult,
  CommonAgeRangeResult,
} from './PlayAgeRangeDeclaration.nitro';

import { ageRangeThresholdManager } from './AgeRangeThresholdManager';

const PlayAgeRangeDeclarationHybridObject =
  NitroModules.createHybridObject<PlayAgeRangeDeclaration>(
    'PlayAgeRangeDeclaration'
  );

export function setAgeRangeThresholds(thresholds: number[]): void {
  ageRangeThresholdManager.setAgeRangeThresholds(thresholds);
}

export async function getAppleDeclaredAgeRangeStatus(): Promise<DeclaredAgeRangeResult> {
  const thresholds = ageRangeThresholdManager.getThresholds();
  
  // Pad to 3 values (the API expects 3 parameters)
  const firstThresholdAge = thresholds[0] ?? 1;
  const secondThresholdAge = thresholds[1] ?? thresholds[0] ?? 1;
  const lastThreshold = thresholds[thresholds.length - 1] ?? 1;
  const thirdThresholdAge = thresholds[2] ?? lastThreshold;

  return await PlayAgeRangeDeclarationHybridObject.requestDeclaredAgeRange(
    firstThresholdAge,
    secondThresholdAge,
    thirdThresholdAge
  );
}

export async function getAndroidPlayAgeRangeStatus(): Promise<PlayAgeRangeDeclarationResult> {
  return await PlayAgeRangeDeclarationHybridObject.getPlayAgeRangeDeclaration();
}

export async function getAgeRangeStatus(): Promise<CommonAgeRangeResult> {
  if (Platform.OS === 'android') {
    const result = await getAndroidPlayAgeRangeStatus();
    return {
      isEligible: result.isEligible,
      lowerAgeBound: result.lowerAgeBound,
      upperAgeBound: result.upperAgeBound,
    };
  } else {
    const result = await getAppleDeclaredAgeRangeStatus();
    return {
      isEligible: result.status === 'eligible',
      lowerAgeBound: result.lowerBound,
      upperAgeBound: result.upperBound,
    };
  }
  //... convert to CommonAgeRangeResult
}

export async function getIsOlderThan(age: number): Promise<boolean> {
  return true
}