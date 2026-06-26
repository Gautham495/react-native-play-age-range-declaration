import { NitroModules } from 'react-native-nitro-modules';
import { Platform } from 'react-native';

import {
  type PlayAgeRangeDeclaration,
  AppStore,
} from './PlayAgeRangeDeclaration.nitro';
import type {
  AmazonGetUserAgeDataResponseStatus,
  AmazonGetUserAgeDataResponseStatusString,
  AmazonGetUserAgeDataResult,
  AmazonGetUserAgeDataUserStatus,
  AmazonGetUserAgeDataUserStatusString,
} from './providers/AmazonGetUserAgeData';
import type { DeclaredAgeRangeResult } from './providers/AppleDeclaredAgeRange';
import type {
  PlayAgeSignalsResult,
  PlayAgeSignalsUserStatus,
  PlayAgeSignalsUserStatusString,
} from './providers/GooglePlayAgeSignals';
import type {
  SamsungGetAgeSignalsResult,
  SamsungGetAgeSignalsUserStatus,
  SamsungGetAgeSignalsUserStatusString,
} from './providers/SamsungGetAgeSignals';

import { ageRangeThresholdManager } from './AgeRangeThresholdManager';

import {
  getIsConsideredOlderThaniOS,
  getIsConsideredOlderThanGooglePlay,
  getIsConsideredOlderThanAmazon,
  getIsConsideredOlderThanSamsungGalaxy,
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

export async function getGooglePlayAgeRangeStatus(): Promise<PlayAgeSignalsResult> {
  return await PlayAgeRangeDeclarationHybridObject.getPlayAgeRangeDeclaration();
}

export async function getAmazonAgeRangeStatus(): Promise<AmazonGetUserAgeDataResult> {
  return await PlayAgeRangeDeclarationHybridObject.getAmazonAgeRangeDeclaration();
}

export async function getSamsungGalaxyAgeRangeStatus(): Promise<SamsungGetAgeSignalsResult> {
  return await PlayAgeRangeDeclarationHybridObject.getGalaxyAgeRangeDeclaration();
}

export const setAgeRangeThresholds = (
  thresholds: [number, number?, number?]
): void => {
  ageRangeThresholdManager.setAgeRangeThresholds(thresholds);
};

export type {
  DeclaredAgeRangeResult,
  PlayAgeSignalsResult,
  PlayAgeSignalsUserStatus,
  PlayAgeSignalsUserStatusString,
  AmazonGetUserAgeDataResult,
  AmazonGetUserAgeDataResponseStatus,
  AmazonGetUserAgeDataResponseStatusString,
  AmazonGetUserAgeDataUserStatus,
  AmazonGetUserAgeDataUserStatusString,
  SamsungGetAgeSignalsResult,
  SamsungGetAgeSignalsUserStatus,
  SamsungGetAgeSignalsUserStatusString,
};

export const getIsConsideredOlderThan = async (
  age: number
): Promise<boolean> => {
  const appStore = PlayAgeRangeDeclarationHybridObject.detectStore();

  if (Platform.OS === 'ios' && appStore === AppStore.APPLE_APPSTORE) {
    const ageData = await getAppleDeclaredAgeRangeStatus();
    return getIsConsideredOlderThaniOS(ageData, age);
  } else if (appStore === AppStore.AMAZON_APPSTORE) {
    const ageData = await getAmazonAgeRangeStatus();
    return getIsConsideredOlderThanAmazon(ageData, age);
  } else if (appStore === AppStore.SAMSUNG_GALAXY_STORE) {
    const ageData = await getSamsungGalaxyAgeRangeStatus();
    return getIsConsideredOlderThanSamsungGalaxy(ageData, age);
  } else if (
    appStore === AppStore.GOOGLE_PLAY ||
    appStore === AppStore.UNKNOWN
  ) {
    const ageData = await getGooglePlayAgeRangeStatus();
    return getIsConsideredOlderThanGooglePlay(ageData, age);
  }

  // Default to true if none of the APIs can be used
  return true;
};
