# Testing Android Age Signals

This guide explains how to test Android Age Signals using Google's `FakeAgeSignalsManager`. This allows you to test different user verification scenarios without requiring a real device with Play Console installed.

## Overview

The library includes support for testing Android Age Signals using Google's `FakeAgeSignalsManager`. This testing utility enables you to simulate various user verification statuses and test your app's behavior without needing:
- A physical Android device with Play Console installed
- Android 15+ (or Android 16+ depending on API availability)
- An app published on Google Play Console

## Using FakeAgeSignalsManager

To test different scenarios, modify the code in `android/src/main/java/com/margelo/nitro/playagerangedeclaration/PlayAgeRangeDeclaration.kt`:

```kotlin
// MOCK: Using FakeAgeSignalsManager for testing
// https://developer.android.com/google/play/age-signals/test-age-signals-api
// 
// To test different scenarios, change the AgeSignalsVerificationStatus:
// - AgeSignalsVerificationStatus.VERIFIED - User is 18+ (verified adult)
// - AgeSignalsVerificationStatus.SUPERVISED - User is supervised (13-17 with parental controls)
// - AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_PENDING - Pending approval
// - AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_DENIED - Approval denied
// - AgeSignalsVerificationStatus.UNKNOWN - User status unknown

val fakeVerifiedUser = AgeSignalsResult.builder()
  .setUserStatus(AgeSignalsVerificationStatus.VERIFIED) // Change this to test different scenarios
  .setAgeLower(13)
  .setAgeUpper(17)
  .setMostRecentApprovalDate(
    Date.from(LocalDate.of(2025, 2, 1).atStartOfDay(ZoneOffset.UTC).toInstant())
  )
  .setInstallId("fake_install_id_12345")
  .build()

val manager = FakeAgeSignalsManager()
manager.setNextAgeSignalsResult(fakeVerifiedUser)
```

## Available Verification Statuses

You can test the following `AgeSignalsVerificationStatus` values:

| Status | Description | Use Case |
|--------|-------------|----------|
| `VERIFIED` | User is 18+ (verified adult) | Test adult user experience |
| `SUPERVISED` | User is supervised (13-17 with parental controls) | Test supervised teen experience |
| `SUPERVISED_APPROVAL_PENDING` | Pending approval | Test pending approval state |
| `SUPERVISED_APPROVAL_DENIED` | Approval denied | Test denied approval state |
| `UNKNOWN` | User status unknown | Test fallback/default behavior |

## Example: Testing Different Scenarios

### Test Verified Adult User

```kotlin
val verifiedAdult = AgeSignalsResult.builder()
  .setUserStatus(AgeSignalsVerificationStatus.VERIFIED)
  .setAgeLower(18)
  .setAgeUpper(18)
  .setInstallId("verified_adult_123")
  .build()

val manager = FakeAgeSignalsManager()
manager.setNextAgeSignalsResult(verifiedAdult)
```

### Test Supervised Teen

```kotlin
val supervisedTeen = AgeSignalsResult.builder()
  .setUserStatus(AgeSignalsVerificationStatus.SUPERVISED)
  .setAgeLower(13)
  .setAgeUpper(17)
  .setMostRecentApprovalDate(
    Date.from(LocalDate.of(2025, 1, 15).atStartOfDay(ZoneOffset.UTC).toInstant())
  )
  .setInstallId("supervised_teen_456")
  .build()

val manager = FakeAgeSignalsManager()
manager.setNextAgeSignalsResult(supervisedTeen)
```

### Test Pending Approval

```kotlin
val pendingApproval = AgeSignalsResult.builder()
  .setUserStatus(AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_PENDING)
  .setAgeLower(13)
  .setAgeUpper(17)
  .setInstallId("pending_approval_789")
  .build()

val manager = FakeAgeSignalsManager()
manager.setNextAgeSignalsResult(pendingApproval)
```

### Test Denied Approval

```kotlin
val deniedApproval = AgeSignalsResult.builder()
  .setUserStatus(AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_DENIED)
  .setAgeLower(13)
  .setAgeUpper(17)
  .setInstallId("denied_approval_012")
  .build()

val manager = FakeAgeSignalsManager()
manager.setNextAgeSignalsResult(deniedApproval)
```

### Test Unknown Status

```kotlin
val unknownStatus = AgeSignalsResult.builder()
  .setUserStatus(AgeSignalsVerificationStatus.UNKNOWN)
  .setInstallId("unknown_status_345")
  .build()

val manager = FakeAgeSignalsManager()
manager.setNextAgeSignalsResult(unknownStatus)
```


## Production Requirements

> [!NOTE]
> The real API requires:
> - Android device with Play Console installed
> - Android 15+ (or Android 16+ depending on API availability)
> - App published on Google Play Console (for production testing)

## Additional Resources

For more information, see the [official Android documentation](https://developer.android.com/google/play/age-signals/test-age-signals-api).
