// src/NativePlayAgeSignals.ts
import { TurboModule, TurboModuleRegistry } from 'react-native';

export interface PlayAgeSignalsResult {
  installId?: string | null;
  userStatus?: string | null;
  error?: string | null;
}

export interface Spec extends TurboModule {
  /**
   * Fetch the current user's Play Age Signals data.
   * Resolves with `installId`, `userStatus`, or throws an error if unsupported.
   */
  getPlayAgeSignals(): Promise<PlayAgeSignalsResult>;
}

const PlayAgeSignalsModule = TurboModuleRegistry.getEnforcing<Spec>('PlayAgeSignals');

export default PlayAgeSignalsModule;
