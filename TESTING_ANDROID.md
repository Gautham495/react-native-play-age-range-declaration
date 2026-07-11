# Testing Age Signals (Android)

All three Android store APIs can be mocked **from JavaScript**, so you can test every scenario on any device or emulator — no store account, supervised user, or published app required.

While a mock is active, `detectStore()` reports the mocked store, so `getIsConsideredOlderThan` routes to it automatically. Only activate one store's mock at a time; pass `undefined` to any mock setter to go back to the real API.

## Google Play — `setGooglePlayMockUser`

Backed by Google's official [`FakeAgeSignalsManager`](https://developer.android.com/google/play/age-signals/test-age-signals-api). No manifest changes needed.

```ts
import {
  setGooglePlayMockUser,
  PlayAgeSignalsUserStatus,
} from 'react-native-play-age-range-declaration';

setGooglePlayMockUser({
  userStatus: PlayAgeSignalsUserStatus.SUPERVISED,
  ageLower: 13,
  ageUpper: 15,
  mostRecentApprovalDate: '2026-01-01', // optional, YYYY-MM-DD
  installId: 'my_test_install_id', // optional
});

setGooglePlayMockUser(undefined); // disable, use the real API
```

| `userStatus`                  | Simulates                                              |
| ----------------------------- | ------------------------------------------------------ |
| `VERIFIED`                    | 18+ user, age verified by Google                       |
| `DECLARED`                    | User (or guardian) declared their age                  |
| `SUPERVISED`                  | Supervised account; parent set the age                 |
| `SUPERVISED_APPROVAL_PENDING` | Significant change awaiting parent approval            |
| `SUPERVISED_APPROVAL_DENIED`  | Parent denied one or more significant changes          |
| `UNKNOWN`                     | Applicable region, but age could not be determined     |

## Amazon Appstore — `setAmazonMockScenario`

Implements [Amazon's 11 test scenarios](https://developer.amazon.com/docs/app-submission/test-getuseragedata-api.html) via a local test ContentProvider. Register it once in your app's `AndroidManifest.xml` (the example app already does):

```xml
<provider
    android:name="com.margelo.nitro.playagerangedeclaration.AmazonTestContentProvider"
    android:authorities="${applicationId}.amzn_test_appstore"
    android:exported="false" />
```

```ts
setAmazonMockScenario(4); // SUPERVISED (13-15)
setAmazonMockScenario(undefined); // disable, use the real API
```

| Scenario | Simulates                                    |
| -------- | -------------------------------------------- |
| 1        | `VERIFIED` (18+)                             |
| 2        | `UNKNOWN` (indeterminate age)                |
| 3        | `SUPERVISED` (0–12)                          |
| 4        | `SUPERVISED` (13–15)                         |
| 5        | `SUPERVISED` (16–17)                         |
| 6        | `CONSENT_NOT_GRANTED`                        |
| 7        | Law not applicable (`SUCCESS`, empty status) |
| 8        | `APP_NOT_OWNED` error                        |
| 9        | `INTERNAL_TRANSIENT_ERROR`                   |
| 10       | `INTERNAL_ERROR`                             |
| 11       | `FEATURE_NOT_SUPPORTED`                      |

## Samsung Galaxy Store — `setSamsungMockScenario`

Implements the [Samsung ASAA test scenarios](https://developer.samsung.com/galaxy-store/galaxy-store-content-provider-api/get-age-signals.html) via a local test ContentProvider:

```xml
<provider
    android:name="com.margelo.nitro.playagerangedeclaration.SamsungTestContentProvider"
    android:authorities="${applicationId}.samsung_test_provider"
    android:exported="false" />
```

```ts
setSamsungMockScenario(4); // SUPERVISED (13-15)
setSamsungMockScenario(undefined); // disable, use the real API
```

| Scenario | Simulates                                     |
| -------- | --------------------------------------------- |
| 1        | `VERIFIED` (18+)                              |
| 2        | `UNKNOWN` (indeterminate age)                 |
| 3        | `SUPERVISED` (0–12)                           |
| 4        | `SUPERVISED` (13–15)                          |
| 5        | `SUPERVISED` (16–17)                          |
| 6        | `SUPERVISED_APPROVAL_PENDING`                 |
| 7        | `SUPERVISED_APPROVAL_DENIED`                  |
| 8        | Unsupported-method error (`result_code = 1`)  |
| 9        | Device not registered error (`result_code = 1`) |

The test providers are `exported="false"`, so they are only reachable from within your own app. They are safe to ship, though you can also register them only in `src/debug/AndroidManifest.xml` to keep release builds clean.

## Production requirements (real APIs)

> [!NOTE]
> The real APIs require:
>
> - The app installed from the store being tested (installer package detection)
> - **Google Play**: Play Store installed, Android 15+ (SDK beta)
> - **Samsung**: Galaxy Store 4.6.03.1+ with a registered Samsung account and ASAA enabled
> - **Amazon**: app installed from the Amazon Appstore and enabled by Amazon

## Additional resources

- [Google Play — Test the Age Signals API](https://developer.android.com/google/play/age-signals/test-age-signals-api)
- [Amazon — Test the GetUserAgeData API](https://developer.amazon.com/docs/app-submission/test-getuseragedata-api.html)
- [Samsung — Get Age Signals](https://developer.samsung.com/galaxy-store/galaxy-store-content-provider-api/get-age-signals.html)
