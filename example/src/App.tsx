import { useEffect, useState } from 'react';
import { Text, View, StyleSheet, ActivityIndicator, ScrollView } from 'react-native';
import { getPlayAgeSignals } from 'react-native-play-age-signals';

type AgeSignalsResult = {
  installId?: string | null;
  userStatus?: string | null;
  error?: string | null;
};

export default function App() {
  const [result, setResult] = useState<AgeSignalsResult | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchSignals = async () => {
      try {
        setLoading(true);
        setError(null);
        const signals = await getPlayAgeSignals();
        setResult(signals);
      } catch (err: any) {
        console.error('❌ Failed to fetch Age Signals:', err);
        const msg =
          err?.message ??
          err?.nativeStackAndroid ??
          'Unknown error retrieving Play Age Signals';
        setError(msg);
        setResult(null);
      } finally {
        setLoading(false);
      }
    };

    fetchSignals();
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.header}>Google Play Age Signals Demo</Text>

      {loading ? (
        <ActivityIndicator size="large" color="#007aff" />
      ) : error ? (
        <View style={styles.errorBox}>
          <Text style={styles.errorTitle}>Error</Text>
          <Text style={styles.errorText}>{error}</Text>
        </View>
      ) : (
        <ScrollView style={styles.resultBox}>
          <Text style={styles.resultText}>
            {result ? JSON.stringify(result, null, 2) : 'No result'}
          </Text>
        </ScrollView>
      )}
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
    maxHeight: 220,
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
    fontFamily: 'monospace',
    fontSize: 13,
    color: '#333',
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
