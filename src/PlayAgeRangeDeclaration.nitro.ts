import type { HybridObject } from 'react-native-nitro-modules';
import type { AmazonGetUserAgeDataResult } from './providers/AmazonGetUserAgeData';
import type { DeclaredAgeRangeResult } from './providers/AppleDeclaredAgeRange';
import type {
  PlayAgeSignalsResult,
  PlayAgeSignalsMockConfig,
} from './providers/GooglePlayAgeSignals';
import type { SamsungGetAgeSignalsResult } from './providers/SamsungGetAgeSignals';

export enum AppStore {
  UNKNOWN = 0,
  GOOGLE_PLAY = 1,
  SAMSUNG_GALAXY_STORE = 2,
  AMAZON_APPSTORE = 3,
  APPLE_APPSTORE = 4,
}

export interface PlayAgeRangeDeclaration
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  detectStore(): AppStore;
  getGooglePlayAgeSignals(): Promise<PlayAgeSignalsResult>;
  getAmazonUserAgeData(): Promise<AmazonGetUserAgeDataResult>;
  getSamsungAgeSignals(): Promise<SamsungGetAgeSignalsResult>;
  /**
   * iOS only. Whether the current user/device is eligible for Apple's
   * Declared Age Range API — i.e. requestDeclaredAgeRange can present the
   * sheet. Always false on Android and on iOS < 26.2.
   */
  isEligibleForAgeFeatures(): Promise<boolean>;
  /**
   * iOS only. Unconditionally presents the Declared Age Range sheet on
   * iOS 26.2+ (does NOT pre-check eligibility — call
   * isEligibleForAgeFeatures() first if you want that). On iOS < 26.2 or on
   * Android, resolves to { isEligible: false, ... } rather than throwing.
   */
  requestDeclaredAgeRange(
    firstThresholdAge: number,
    secondThresholdAge?: number,
    thirdThresholdAge?: number
  ): Promise<DeclaredAgeRangeResult>;
  setGooglePlayMockUser(config?: PlayAgeSignalsMockConfig): void;
  setAmazonMockScenario(scenario?: number): void;
  setSamsungMockScenario(scenario?: number): void;
}
