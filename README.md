<a href="https://gauthamvijay.com">
  <picture>
    <img alt="react-native-play-age-range-declaration" src="./docs/img/banner.png" />
  </picture>
</a>

# react-native-play-age-range-declaration (Alpha)

A **React Native Nitro Module** providing a unified API for **age-appropriate experiences** across platforms — bridging:

- 🟢 **Google Play Age Signals API** (Android)
- 🔵 **Apple Declared Age Range API** (iOS 26+)

Built using the **Nitro Modules** framework by [Margelo](https://nitro.margelo.com) for high-performance, fully typed native integration.

> [!IMPORTANT]
>
> - The APIs for both Google & Apple's Age Signal & Declared Age Range are not responding with full required info as of October 2025.
> - Once they are working as intended I will update according to their API changes

---

## 📦 Installation

```bash
npm install react-native-play-age-range-declaration react-native-nitro-modules
```

### ⚙️ Configuration

Nitro Modules autolink automatically — no manual steps required.

✅ Works with

- React Native 0.76+ (New Architecture)
- Expo Custom Dev Clients
- TypeScript

---

## 🧠 Overview

| Platform    | API Used                                                     | Purpose                                            |
| ----------- | ------------------------------------------------------------ | -------------------------------------------------- |
| **Android** | Play Age Signals API (`com.google.android.play:age-signals`) | Detect user supervision / verified status          |
| **iOS**     | Declared Age Range API (`AgeRangeService.requestAgeRange`)   | Get user’s declared age range (e.g., 13–15, 16–17) |

---

## ⚙️ Usage

```tsx
import { getAgeData } from 'react-native-play-age-range-declaration';

export default async function Example() {
  try {
    // Android → returns Play Age Signals
    // iOS → opens Declared Age Range UI
    const result = await getAgeData(16);
    console.log('Age Range Result:', result);
  } catch (err) {
    console.error('Error fetching age range:', err);
  }
}
```

---

## 🧩 API Reference

### `PlayAgeRangeDeclaration` (HybridObject)

```ts
import type { HybridObject } from 'react-native-nitro-modules';

export interface PlayAgeRangeDeclaration
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  getPlayAgeRangeDeclaration(): Promise<PlayAgeRangeDeclarationResult>;
  requestDeclaredAgeRange(ageGate: number): Promise<DeclaredAgeRangeResult>;
}

export interface PlayAgeRangeDeclarationResult {
  installId?: string | null;
  userStatus?: string | null;
  error?: string | null;
}

export interface DeclaredAgeRangeResult {
  status?: string | null;
  lowerBound?: number | null;
  upperBound?: number | null;
  error?: string | null;
}
```

---

## 📱 Example Output

### 🟢 Android – Play Age Signals

```json
{
  "installId": "abcd-1234-efgh-5678",
  "userStatus": "SUPERVISED"
}
```

### 🔵 iOS – Declared Age Range

```json
{
  "status": "sharing",
  "lowerBound": 16,
  "upperBound": 17
}
```

---

## 🧱 Under the Hood

### 🟩 Android

Implemented in **Kotlin** using Nitro’s async promises:

```kotlin
val manager = AgeSignalsManagerFactory.create(appContext)
val request = AgeSignalsRequest.builder().build()
manager.checkAgeSignals(request)
```

Wrapped in:

```kotlin
Promise.async {
  suspendCancellableCoroutine { cont -> ... }
}
```

No `RCT_EXPORT_MODULE` — pure Nitro interop.

---

### 🟦 iOS

Implemented in **Swift** using Nitro async promises and Apple’s Declared Age Range API:

```swift
let response = try await AgeRangeService.shared.requestAgeRange(
  ageGates: intGate,
  in: viewController
)
```

Bridged via:

```swift
class PlayAgeRangeDeclaration: HybridPlayAgeRangeDeclarationSpec { ... }
```

---

## 🧾 Real-World Use Case

Comply with **digital safety** and **age-appropriate design** laws automatically:

- **Android** → Play Age Signals (COPPA & supervision data)
- **iOS** → Declared Age Range (Apple’s privacy-preserving age disclosure)

---

## 🧩 Supported Platforms

| Platform                   | Status                             |
| -------------------------- | ---------------------------------- |
| **Android**                | ✅ Supported (SDK Beta)            |
| **iOS 26+**                | ✅ Supported                       |
| **Expo Custom Dev Client** | ✅ Supported via Nitro autolinking |
| **AOSP Emulator**          | ⚠️ Not supported (requires Play)   |

---

## 🧭 Roadmap

- ✅ Kotlin + Swift Nitro implementation
- ✅ Cross-platform TypeScript definitions
- ✅ Async Promise bridge
- 🚧 Add example app with Fabric UI demo
- 🚧 Add GitHub Actions auto-publish pipeline

---

## 🤝 Contributing

Pull requests welcome!

- [Development Workflow](CONTRIBUTING.md#development-workflow)
- [Sending a PR](CONTRIBUTING.md#sending-a-pull-request)
- [Code of Conduct](CODE_OF_CONDUCT.md)

---

## 🪪 License

MIT © [**Gautham Vijayan**](https://gauthamvijay.com)

---

Made with ❤️ and [**Nitro Modules**](https://nitro.margelo.com)
