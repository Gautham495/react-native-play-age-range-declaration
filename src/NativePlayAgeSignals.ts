import { TurboModule, TurboModuleRegistry } from 'react-native';

export interface PlayAgeSignalsResult {
  age?: string | null;
  ageGroup?: string | null;
  verificationStatus?: string | null;
  timestamp?: number | null;
}

export interface Spec extends TurboModule {
  getPlayAgeSignals(): Promise<PlayAgeSignalsResult>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('PlayAgeSignals');
