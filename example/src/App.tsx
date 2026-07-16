import { useState } from 'react';
import {
  Text,
  View,
  StyleSheet,
  ActivityIndicator,
  ScrollView,
  Platform,
  Pressable,
} from 'react-native';

import {
  getGooglePlayAgeRangeStatus,
  getAmazonAgeRangeStatus,
  getSamsungGalaxyAgeRangeStatus,
  getAppleDeclaredAgeRangeStatus,
  getIsConsideredOlderThan,
  setAgeRangeThresholds,
  setGooglePlayMockUser,
  setAmazonMockScenario,
  setSamsungMockScenario,
  detectStore,
  AppStore,
  AppStoreString,
  AgeSignalsUserStatusString,
  PlayAgeSignalsUserStatus,
  type AgeSignalsResult,
  type DeclaredAgeRangeResult,
  type PlayAgeSignalsMockConfig,
} from 'react-native-play-age-range-declaration';

setAgeRangeThresholds([13, 15]);

type AndroidStore =
  | AppStore.GOOGLE_PLAY
  | AppStore.AMAZON_APPSTORE
  | AppStore.SAMSUNG_GALAXY_STORE;

const STORE_OPTIONS: { store: AndroidStore; label: string }[] = [
  { store: AppStore.GOOGLE_PLAY, label: 'Google Play' },
  { store: AppStore.AMAZON_APPSTORE, label: 'Amazon' },
  { store: AppStore.SAMSUNG_GALAXY_STORE, label: 'Samsung' },
];

const GOOGLE_PLAY_SCENARIOS: {
  label: string;
  config?: PlayAgeSignalsMockConfig;
}[] = [
  { label: 'Real API' },
  {
    label: 'Verified 18+',
    config: { userStatus: PlayAgeSignalsUserStatus.VERIFIED, ageLower: 18 },
  },
  {
    label: 'Supervised 13-17',
    config: {
      userStatus: PlayAgeSignalsUserStatus.SUPERVISED,
      ageLower: 13,
      ageUpper: 17,
      mostRecentApprovalDate: '2026-01-01',
    },
  },
  {
    label: 'Unknown',
    config: { userStatus: PlayAgeSignalsUserStatus.UNKNOWN },
  },
];

// Labels for AmazonTestContentProvider's testOption values (index + 1).
const AMAZON_SCENARIOS = [
  'Verified 18+',
  'Unknown',
  'Supervised 0-12',
  'Supervised 13-15',
  'Supervised 16-17',
  'Consent not granted',
  'Law not applicable',
  'App not owned',
  'Transient error',
  'Internal error',
  'Feature not supported',
];

// Labels for SamsungTestContentProvider's testOption values (index + 1).
const SAMSUNG_SCENARIOS = [
  'Verified 18+',
  'Unknown',
  'Supervised 0-12',
  'Supervised 13-15',
  'Supervised 16-17',
  'Approval pending',
  'Approval denied',
  'Unsupported method',
  'No Samsung account',
];

const ANDROID_FETCHERS: Record<AndroidStore, () => Promise<AgeSignalsResult>> =
  {
    [AppStore.GOOGLE_PLAY]: getGooglePlayAgeRangeStatus,
    [AppStore.AMAZON_APPSTORE]: getAmazonAgeRangeStatus,
    [AppStore.SAMSUNG_GALAXY_STORE]: getSamsungGalaxyAgeRangeStatus,
  };

function scenarioLabelsFor(store: AndroidStore): string[] {
  switch (store) {
    case AppStore.GOOGLE_PLAY:
      return GOOGLE_PLAY_SCENARIOS.map((s) => s.label);
    case AppStore.AMAZON_APPSTORE:
      return AMAZON_SCENARIOS;
    case AppStore.SAMSUNG_GALAXY_STORE:
      return SAMSUNG_SCENARIOS;
  }
}

// Point the native module at the selected store: activate its mock scenario
// and clear the others so detectStore() routes to it.
function applyMockScenario(store: AndroidStore, scenarioIndex: number) {
  setGooglePlayMockUser(undefined);
  setAmazonMockScenario(undefined);
  setSamsungMockScenario(undefined);

  switch (store) {
    case AppStore.GOOGLE_PLAY:
      setGooglePlayMockUser(GOOGLE_PLAY_SCENARIOS[scenarioIndex]?.config);
      break;
    case AppStore.AMAZON_APPSTORE:
      setAmazonMockScenario(scenarioIndex + 1);
      break;
    case AppStore.SAMSUNG_GALAXY_STORE:
      setSamsungMockScenario(scenarioIndex + 1);
      break;
  }
}

export default function App() {
  const [selectedStore, setSelectedStore] = useState<AndroidStore>(
    AppStore.GOOGLE_PLAY
  );
  const [scenarioIndex, setScenarioIndex] = useState(0);

  const [androidResult, setAndroidResult] = useState<AgeSignalsResult | null>(
    null
  );
  const [appleResult, setAppleResult] = useState<DeclaredAgeRangeResult | null>(
    null
  );
  const [detectedStore, setDetectedStore] = useState<AppStore | null>(null);

  const [isConsideredOlderThan18, setIsConsideredOlderThan18] = useState<
    boolean | null
  >(null);
  const [isConsideredOlderThan15, setIsConsideredOlderThan15] = useState<
    boolean | null
  >(null);
  const [isConsideredOlderThan13, setIsConsideredOlderThan13] = useState<
    boolean | null
  >(null);

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const selectStore = (store: AndroidStore) => {
    setSelectedStore(store);
    setScenarioIndex(0);
  };

  const fetchStatus = async () => {
    try {
      setLoading(true);
      setError(null);

      if (Platform.OS === 'android') {
        applyMockScenario(selectedStore, scenarioIndex);
        setDetectedStore(detectStore());

        const result = await ANDROID_FETCHERS[selectedStore]();
        setAndroidResult(result);
      } else {
        const data = await getAppleDeclaredAgeRangeStatus();
        setAppleResult(data);
      }

      setIsConsideredOlderThan18(await getIsConsideredOlderThan(18));
      setIsConsideredOlderThan15(await getIsConsideredOlderThan(15));
      setIsConsideredOlderThan13(await getIsConsideredOlderThan(13));
    } catch (err: any) {
      console.error('❌ Failed to fetch Age Signals:', err);
      const msg =
        err?.message ??
        err?.nativeStackAndroid ??
        'Unknown error retrieving Age Signals';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const hasResult =
    Platform.OS === 'ios' ? appleResult != null : androidResult != null;

  const title =
    Platform.OS === 'ios'
      ? 'Apple Declared Age Range Demo'
      : 'Android Age Signals Demo';

  return (
    <View style={styles.container}>
      <Text style={styles.header}>{title}</Text>

      {Platform.OS === 'android' && (
        <>
          <View style={styles.storeRow}>
            {STORE_OPTIONS.map(({ store, label }) => (
              <Pressable
                key={store}
                style={[
                  styles.storeButton,
                  selectedStore === store && styles.storeButtonSelected,
                ]}
                onPress={() => selectStore(store)}
              >
                <Text
                  style={[
                    styles.storeButtonText,
                    selectedStore === store && styles.storeButtonTextSelected,
                  ]}
                >
                  {label}
                </Text>
              </Pressable>
            ))}
          </View>

          <View style={styles.chipRow}>
            {scenarioLabelsFor(selectedStore).map((label, index) => (
              <Pressable
                key={label}
                style={[
                  styles.chip,
                  scenarioIndex === index && styles.chipSelected,
                ]}
                onPress={() => setScenarioIndex(index)}
              >
                <Text
                  style={[
                    styles.chipText,
                    scenarioIndex === index && styles.chipTextSelected,
                  ]}
                >
                  {label}
                </Text>
              </Pressable>
            ))}
          </View>
        </>
      )}

      {loading ? (
        <ActivityIndicator size="large" color="#007aff" />
      ) : error ? (
        <View style={styles.errorBox}>
          <Text style={styles.errorTitle}>Error</Text>
          <Text style={styles.errorText}>{error}</Text>
        </View>
      ) : !hasResult ? (
        <Text style={styles.hint}>Press Check to fetch age signals.</Text>
      ) : (
        <ScrollView style={styles.resultBox}>
          {Platform.OS === 'ios' ? (
            <Text style={styles.resultText}>
              Is Eligible: {appleResult ? String(appleResult.isEligible) : ''}
              {'\n'}
              Status: {appleResult?.status ?? '—'}
              {'\n'}
              Parent Controls: {appleResult?.parentControls ?? '—'}
              {'\n'}
              Lower Bound: {appleResult?.lowerBound ?? '—'}
            </Text>
          ) : (
            androidResult && (
              <>
                <Text style={styles.resultText}>
                  Detected Store:{' '}
                  {detectedStore != null ? AppStoreString[detectedStore] : '—'}
                  {'\n'}
                  User Status:{' '}
                  {androidResult.userStatus != null
                    ? AgeSignalsUserStatusString[androidResult.userStatus]
                    : '(no signal)'}
                  {'\n'}
                  Age Range: {androidResult.ageLower ?? '—'} to{' '}
                  {androidResult.ageUpper ?? '—'}
                  {'\n'}
                  Most Recent Approval:{' '}
                  {androidResult.mostRecentApprovalDate ?? '—'}
                  {'\n'}
                  Error: {androidResult.error ?? '—'}
                </Text>
                <Text style={styles.rawLabel}>Raw store response</Text>
                <Text style={styles.resultText}>
                  {JSON.stringify(androidResult.raw, null, 2)}
                </Text>
              </>
            )
          )}

          {isConsideredOlderThan18 ? (
            <Text style={styles.resultText}>
              This is only visible to users older than 18
            </Text>
          ) : (
            <Text style={styles.resultText}>The user is younger than 18</Text>
          )}
          {isConsideredOlderThan15 ? (
            <Text style={styles.resultText}>
              This is only visible to users older than 15
            </Text>
          ) : (
            <Text style={styles.resultText}>The user is younger than 15</Text>
          )}
          {isConsideredOlderThan13 ? (
            <Text style={styles.resultText}>
              This is only visible to users older than 13
            </Text>
          ) : (
            <Text style={styles.resultText}>The user is younger than 13</Text>
          )}
        </ScrollView>
      )}

      <Pressable style={styles.button} onPress={fetchStatus}>
        <Text style={styles.buttonTitle}>Check</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#f4f6f8',
    alignItems: 'center',
    justifyContent: 'center',
  },
  header: {
    fontSize: 20,
    fontWeight: '700',
    marginBottom: 20,
  },
  storeRow: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 12,
  },
  storeButton: {
    flex: 1,
    paddingVertical: 10,
    borderRadius: 10,
    backgroundColor: '#fff',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#d5dbe1',
  },
  storeButtonSelected: {
    backgroundColor: '#007aff',
    borderColor: '#007aff',
  },
  storeButtonText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
  },
  storeButtonTextSelected: {
    color: '#fff',
  },
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 6,
    marginBottom: 16,
  },
  chip: {
    paddingVertical: 6,
    paddingHorizontal: 10,
    borderRadius: 14,
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#d5dbe1',
  },
  chipSelected: {
    backgroundColor: '#0a2540',
    borderColor: '#0a2540',
  },
  chipText: {
    fontSize: 12,
    color: '#333',
  },
  chipTextSelected: {
    color: '#fff',
  },
  hint: {
    fontSize: 14,
    color: '#667',
    marginVertical: 24,
  },
  resultBox: {
    maxHeight: 320,
    width: '100%',
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 12,
    shadowColor: '#000',
    shadowOpacity: 0.1,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 6,
    elevation: 2,
  },
  resultText: {
    fontFamily: Platform.select({ ios: 'Menlo', android: 'monospace' }),
    fontSize: 13,
    color: '#333',
  },
  rawLabel: {
    fontSize: 12,
    fontWeight: '700',
    color: '#667',
    marginTop: 12,
    marginBottom: 4,
    textTransform: 'uppercase',
  },

  button: {
    backgroundColor: '#2fe5e5',
    borderRadius: 10,
    padding: 12,
    width: '100%',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 20,
  },

  buttonTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: 'black',
  },

  errorBox: {
    backgroundColor: '#ffe5e5',
    borderRadius: 10,
    padding: 12,
    width: '100%',
  },
  errorTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#b00020',
    marginBottom: 4,
  },
  errorText: {
    color: '#b00020',
    fontSize: 14,
  },
});
