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
  getPlayAgeRangeDeclaration(): Promise<PlayAgeSignalsResult>;
  getAmazonAgeRangeDeclaration(): Promise<AmazonGetUserAgeDataResult>;
  getGalaxyAgeRangeDeclaration(): Promise<SamsungGetAgeSignalsResult>;
  requestDeclaredAgeRange(
    firstThresholdAge: number,
    secondThresholdAge?: number,
    thirdThresholdAge?: number
  ): Promise<DeclaredAgeRangeResult>;
  setGooglePlayMockUser(config?: PlayAgeSignalsMockConfig): void;
  setAmazonMockScenario(scenario?: number): void;
  setSamsungMockScenario(scenario?: number): void;
}
