import { NitroModules } from 'react-native-nitro-modules';
import { Platform } from 'react-native';

import {
  type PlayAgeRangeDeclaration,
  AppStore,
} from './PlayAgeRangeDeclaration.nitro';
import {
  type AgeSignalsResult,
  AgeSignalsUserStatus,
  AgeSignalsUserStatusString,
  AppStoreString,
  normalizeAmazonResult,
  normalizeGooglePlayResult,
  normalizeSamsungResult,
} from './AgeSignals';
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
  type PlayAgeSignalsMockConfig,
  PlayAgeSignalsUserStatus,
  PlayAgeSignalsUserStatusString,
} from './providers/GooglePlayAgeSignals';
import {
  type SamsungGetAgeSignalsResult,
  SamsungGetAgeSignalsUserStatus,
  SamsungGetAgeSignalsUserStatusString,
} from './providers/SamsungGetAgeSignals';

import { ageRangeThresholdManager } from './AgeRangeThresholdManager';

import {
  getIsConsideredOlderThaniOS,
  getIsConsideredOlderThanAgeSignals,
} from './isConsideredOlderThan';

const PlayAgeRangeDeclarationHybridObject =
  NitroModules.createHybridObject<PlayAgeRangeDeclaration>(
    'PlayAgeRangeDeclaration'
  );

/** The store this app was installed from, detected natively. */
export const detectStore = (): AppStore =>
  PlayAgeRangeDeclarationHybridObject.detectStore();

export async function getAppleDeclaredAgeRangeStatus(): Promise<DeclaredAgeRangeResult> {
  const thresholds = ageRangeThresholdManager.getThresholds();

  return await PlayAgeRangeDeclarationHybridObject.requestDeclaredAgeRange(
    thresholds[0],
    thresholds[1],
    thresholds[2]
  );
}

export async function getGooglePlayAgeRangeStatus(): Promise<
  AgeSignalsResult<PlayAgeSignalsResult>
> {
  return normalizeGooglePlayResult(
    await PlayAgeRangeDeclarationHybridObject.getPlayAgeRangeDeclaration()
  );
}

// Amazon advises at most two retries for INTERNAL_TRANSIENT_ERROR:
// https://developer.amazon.com/docs/app-submission/user-age-verification.html
const AMAZON_MAX_TRANSIENT_RETRIES = 2;

export async function getAmazonAgeRangeStatus(): Promise<
  AgeSignalsResult<AmazonGetUserAgeDataResult>
> {
  let raw =
    await PlayAgeRangeDeclarationHybridObject.getAmazonAgeRangeDeclaration();

  for (
    let retry = 0;
    retry < AMAZON_MAX_TRANSIENT_RETRIES &&
    raw.responseStatus ===
      AmazonGetUserAgeDataResponseStatus.INTERNAL_TRANSIENT_ERROR;
    retry++
  ) {
    raw =
      await PlayAgeRangeDeclarationHybridObject.getAmazonAgeRangeDeclaration();
  }

  return normalizeAmazonResult(raw);
}

export async function getSamsungGalaxyAgeRangeStatus(): Promise<
  AgeSignalsResult<SamsungGetAgeSignalsResult>
> {
  return normalizeSamsungResult(
    await PlayAgeRangeDeclarationHybridObject.getGalaxyAgeRangeDeclaration()
  );
}

export const setAgeRangeThresholds = (
  thresholds: [number, number?, number?]
): void => {
  ageRangeThresholdManager.setAgeRangeThresholds(thresholds);
};

export const setGooglePlayMockUser = (
  config?: PlayAgeSignalsMockConfig
): void => {
  PlayAgeRangeDeclarationHybridObject.setGooglePlayMockUser(config);
};

export const setAmazonMockScenario = (scenario?: number): void => {
  PlayAgeRangeDeclarationHybridObject.setAmazonMockScenario(scenario);
};

export const setSamsungMockScenario = (scenario?: number): void => {
  PlayAgeRangeDeclarationHybridObject.setSamsungMockScenario(scenario);
};

/**
 * How each Android store's age signals are fetched. UNKNOWN falls back to
 * Google Play — by far the most common installer — so behavior doesn't
 * regress when the installer can't be determined (sideloads, emulators).
 */
const ageSignalsFetchersByStore: Partial<
  Record<AppStore, () => Promise<AgeSignalsResult<unknown>>>
> = {
  [AppStore.GOOGLE_PLAY]: getGooglePlayAgeRangeStatus,
  [AppStore.AMAZON_APPSTORE]: getAmazonAgeRangeStatus,
  [AppStore.SAMSUNG_GALAXY_STORE]: getSamsungGalaxyAgeRangeStatus,
  [AppStore.UNKNOWN]: getGooglePlayAgeRangeStatus,
};

export const getIsConsideredOlderThan = async (
  age: number
): Promise<boolean> => {
  const appStore = detectStore();

  if (Platform.OS === 'ios') {
    if (appStore === AppStore.APPLE_APPSTORE) {
      const ageData = await getAppleDeclaredAgeRangeStatus();
      return getIsConsideredOlderThaniOS(ageData, age);
    }
    // No usable age API — default to older.
    return true;
  }

  const fetchAgeSignals =
    ageSignalsFetchersByStore[appStore] ?? getGooglePlayAgeRangeStatus;
  const ageData = await fetchAgeSignals();
  return getIsConsideredOlderThanAgeSignals(ageData, age);
};

export type {
  AgeSignalsResult,
  PlayAgeSignalsMockConfig,
  DeclaredAgeRangeResult,
  PlayAgeSignalsResult,
  AmazonGetUserAgeDataResult,
  SamsungGetAgeSignalsResult,
};

export {
  AppStore,
  AppStoreString,
  AgeSignalsUserStatus,
  AgeSignalsUserStatusString,
  PlayAgeSignalsUserStatus,
  PlayAgeSignalsUserStatusString,
  AmazonGetUserAgeDataResponseStatus,
  AmazonGetUserAgeDataResponseStatusString,
  AmazonGetUserAgeDataUserStatus,
  AmazonGetUserAgeDataUserStatusString,
  SamsungGetAgeSignalsUserStatus,
  SamsungGetAgeSignalsUserStatusString,
};
