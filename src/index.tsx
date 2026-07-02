import { NitroModules } from 'react-native-nitro-modules';
import { Platform } from 'react-native';

import {
  type PlayAgeRangeDeclaration,
  AppStore,
} from './PlayAgeRangeDeclaration.nitro';
import {
  type AmazonGetUserAgeDataResult,
  AmazonGetUserAgeDataResponseStatus,
  AmazonGetUserAgeDataResponseStatusString,
  AmazonGetUserAgeDataUserStatus,
  AmazonGetUserAgeDataUserStatusString,
} from './providers/AmazonGetUserAgeData';
import type { DeclaredAgeRangeResult } from './providers/AppleDeclaredAgeRange';
import {
  type PlayAgeSignalsResult,
  PlayAgeSignalsUserStatus,
  PlayAgeSignalsUserStatusString,
} from './providers/GooglePlayAgeSignals';
import {
  type SamsungGetAgeSignalsResult,
  SamsungGetAgeSignalsUserStatus,
  SamsungGetAgeSignalsUserStatusString,
} from './providers/SamsungGetAgeSignals';

import type { GooglePlayMockConfig } from './testing/GooglePlayMockConfig';

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

export const setGooglePlayMockUser = (config?: GooglePlayMockConfig): void => {
  PlayAgeRangeDeclarationHybridObject.setGooglePlayMockUser(config);
};

export type {
  GooglePlayMockConfig,
  DeclaredAgeRangeResult,
  PlayAgeSignalsResult,
  AmazonGetUserAgeDataResult,
  SamsungGetAgeSignalsResult,
};

export {
  PlayAgeSignalsUserStatus,
  PlayAgeSignalsUserStatusString,
  AmazonGetUserAgeDataResponseStatus,
  AmazonGetUserAgeDataResponseStatusString,
  AmazonGetUserAgeDataUserStatus,
  AmazonGetUserAgeDataUserStatusString,
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
