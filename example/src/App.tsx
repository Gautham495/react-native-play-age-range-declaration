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
  getAndroidPlayAgeRangeStatus,
  getAppleDeclaredAgeRangeStatus,
  type PlayAgeRangeDeclarationResult,
  type DeclaredAgeRangeResult,
  PlayAgeRangeDeclarationUserStatusString,
  PlayAgeRangeDeclarationUserStatus,
} from 'react-native-play-age-range-declaration';

export default function App() {
  const [androidResult, setAndroidResult] =
    useState<PlayAgeRangeDeclarationResult | null>(null);

  const [appleResult, setAppleResult] = useState<DeclaredAgeRangeResult | null>(
    null
  );

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchStatus = async () => {
    try {
      setLoading(true);

      setError(null);

      if (Platform.OS === 'android') {
        const data = await getAndroidPlayAgeRangeStatus();

        setAndroidResult(data);
      } else {
        const data = await getAppleDeclaredAgeRangeStatus(10, 13, 16);

        setAppleResult(data);
      }
    } catch (err: any) {
      console.error('❌ Failed to fetch Age Signals:', err);
      const msg =
        err?.message ??
        err?.nativeStackAndroid ??
        'Unknown error retrieving Play Age Signals';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const title =
    Platform.OS === 'ios'
      ? 'Apple Declared Age Range Demo'
      : 'Google Play Age Signals Demo';

  return (
    <View style={styles.container}>
      <Text style={styles.header}>{title}</Text>

      {loading ? (
        <ActivityIndicator size="large" color="#007aff" />
      ) : error ? (
        <View style={styles.errorBox}>
          <Text style={styles.errorTitle}>Error</Text>
          <Text style={styles.errorText}>{error}</Text>
        </View>
      ) : (
        <ScrollView style={styles.resultBox}>
          {Platform.OS === 'ios' ? (
            <Text style={styles.resultText}>
              Status: {appleResult ? appleResult?.status : ''} {`\n`}
              ParentControls: {appleResult
                ? appleResult?.parentControls
                : ''}{' '}
              {`\n`}
              Lower Bound: {appleResult ? appleResult?.lowerBound : ''} {`\n`}
            </Text>
          ) : (
            <Text style={styles.resultText}>
              Install Id: {androidResult ? androidResult?.installId : ''} {`\n`}
              User Status:{' '}
              {androidResult
                ? PlayAgeRangeDeclarationUserStatusString[
                    androidResult?.userStatus as PlayAgeRangeDeclarationUserStatus
                  ]
                : ''}{' '}
              {`\n`}
              Most Recent Approval Date:{' '}
              {androidResult ? androidResult?.mostRecentApprovalDate : ''}{' '}
              {`\n`}
              Age Lower: {androidResult ? androidResult?.ageLower : ''} {`\n`}
              Age Upper: {androidResult ? androidResult?.ageUpper : ''} {`\n`}
              Error: {androidResult ? androidResult?.error : ''} {`\n`}
            </Text>
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
  resultBox: {
    maxHeight: 240,
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
