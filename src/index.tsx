import NativePlayAgeSignals from './NativePlayAgeSignals';

export async function getPlayAgeSignals() {
  return await NativePlayAgeSignals.getPlayAgeSignals();
}
