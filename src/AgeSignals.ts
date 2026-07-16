import { AppStore } from './PlayAgeRangeDeclaration.nitro';
import {
  type AmazonGetUserAgeDataResult,
  AmazonGetUserAgeDataResponseStatus,
  AmazonGetUserAgeDataResponseStatusString,
  AmazonGetUserAgeDataUserStatus,
} from './providers/AmazonGetUserAgeData';
import {
  type PlayAgeSignalsResult,
  PlayAgeSignalsUserStatus,
} from './providers/GooglePlayAgeSignals';
import {
  type SamsungGetAgeSignalsResult,
  SamsungGetAgeSignalsUserStatus,
} from './providers/SamsungGetAgeSignals';

/** Human-readable names for the AppStore enum values. */
export const AppStoreString: Record<AppStore, string> = {
  [AppStore.UNKNOWN]: 'UNKNOWN',
  [AppStore.GOOGLE_PLAY]: 'GOOGLE_PLAY',
  [AppStore.SAMSUNG_GALAXY_STORE]: 'SAMSUNG_GALAXY_STORE',
  [AppStore.AMAZON_APPSTORE]: 'AMAZON_APPSTORE',
  [AppStore.APPLE_APPSTORE]: 'APPLE_APPSTORE',
};

/**
 * Store-agnostic age-signals user status: the superset of the Google Play,
 * Amazon and Samsung statuses. Each store's raw status maps onto exactly one
 * of these (see the normalize* transformers below).
 */
export enum AgeSignalsUserStatus {
  /** The store verified the user is 18+ (e.g. government ID, payment card). */
  VERIFIED = 0,
  /** The user, or their parent/guardian, declared the age. Google Play only. */
  DECLARED = 1,
  /** Supervised account; a parent set the age. Use ageLower/ageUpper. */
  SUPERVISED = 2,
  /** Supervised account with significant changes awaiting parent approval. */
  SUPERVISED_APPROVAL_PENDING = 3,
  /** Supervised account where the parent denied one or more significant changes. */
  SUPERVISED_APPROVAL_DENIED = 4,
  /** Parental consent revoked or not (re-)granted. Amazon only. */
  CONSENT_NOT_GRANTED = 5,
  /** The user is in an applicable region but their age could not be determined. */
  UNKNOWN = 6,
}

export const AgeSignalsUserStatusString: Record<AgeSignalsUserStatus, string> =
  {
    [AgeSignalsUserStatus.VERIFIED]: 'VERIFIED',
    [AgeSignalsUserStatus.DECLARED]: 'DECLARED',
    [AgeSignalsUserStatus.SUPERVISED]: 'SUPERVISED',
    [AgeSignalsUserStatus.SUPERVISED_APPROVAL_PENDING]:
      'SUPERVISED_APPROVAL_PENDING',
    [AgeSignalsUserStatus.SUPERVISED_APPROVAL_DENIED]:
      'SUPERVISED_APPROVAL_DENIED',
    [AgeSignalsUserStatus.CONSENT_NOT_GRANTED]: 'CONSENT_NOT_GRANTED',
    [AgeSignalsUserStatus.UNKNOWN]: 'UNKNOWN',
  };

/**
 * A store-agnostic age-signals result. Produced by normalizing the raw
 * response of one of the store APIs; the untouched response is kept in `raw`.
 */
export interface AgeSignalsResult<Raw = unknown> {
  /** The store whose API produced this result. */
  store: AppStore;
  /**
   * Normalized user status. `undefined` when the store returned no signal,
   * i.e. the user is not in a region where an age-verification law applies
   * (or, for Google Play, did not consent to share their age).
   */
  userStatus?: AgeSignalsUserStatus;
  /** Inclusive lower bound of the user's age range, when known. */
  ageLower?: number;
  /** Inclusive upper bound of the user's age range. `undefined` for 18+ users. */
  ageUpper?: number;
  /** ISO 8601 date of the most recently approved significant change. */
  mostRecentApprovalDate?: string;
  /** Set when the underlying store call failed; all signal fields are then `undefined`. */
  error?: string;
  /** The unmodified response from the store's native API. */
  raw: Raw;
}

const googlePlayUserStatusMap: Record<
  PlayAgeSignalsUserStatus,
  AgeSignalsUserStatus
> = {
  [PlayAgeSignalsUserStatus.VERIFIED]: AgeSignalsUserStatus.VERIFIED,
  [PlayAgeSignalsUserStatus.DECLARED]: AgeSignalsUserStatus.DECLARED,
  [PlayAgeSignalsUserStatus.SUPERVISED]: AgeSignalsUserStatus.SUPERVISED,
  [PlayAgeSignalsUserStatus.SUPERVISED_APPROVAL_PENDING]:
    AgeSignalsUserStatus.SUPERVISED_APPROVAL_PENDING,
  [PlayAgeSignalsUserStatus.SUPERVISED_APPROVAL_DENIED]:
    AgeSignalsUserStatus.SUPERVISED_APPROVAL_DENIED,
  [PlayAgeSignalsUserStatus.UNKNOWN]: AgeSignalsUserStatus.UNKNOWN,
};

const amazonUserStatusMap: Record<
  AmazonGetUserAgeDataUserStatus,
  AgeSignalsUserStatus
> = {
  [AmazonGetUserAgeDataUserStatus.VERIFIED]: AgeSignalsUserStatus.VERIFIED,
  [AmazonGetUserAgeDataUserStatus.SUPERVISED]: AgeSignalsUserStatus.SUPERVISED,
  [AmazonGetUserAgeDataUserStatus.CONSENT_NOT_GRANTED]:
    AgeSignalsUserStatus.CONSENT_NOT_GRANTED,
  [AmazonGetUserAgeDataUserStatus.UNKNOWN]: AgeSignalsUserStatus.UNKNOWN,
};

const samsungUserStatusMap: Record<
  SamsungGetAgeSignalsUserStatus,
  AgeSignalsUserStatus
> = {
  [SamsungGetAgeSignalsUserStatus.VERIFIED]: AgeSignalsUserStatus.VERIFIED,
  [SamsungGetAgeSignalsUserStatus.SUPERVISED]: AgeSignalsUserStatus.SUPERVISED,
  [SamsungGetAgeSignalsUserStatus.SUPERVISED_APPROVAL_PENDING]:
    AgeSignalsUserStatus.SUPERVISED_APPROVAL_PENDING,
  [SamsungGetAgeSignalsUserStatus.SUPERVISED_APPROVAL_DENIED]:
    AgeSignalsUserStatus.SUPERVISED_APPROVAL_DENIED,
  [SamsungGetAgeSignalsUserStatus.UNKNOWN]: AgeSignalsUserStatus.UNKNOWN,
};

export function normalizeGooglePlayResult(
  raw: PlayAgeSignalsResult
): AgeSignalsResult<PlayAgeSignalsResult> {
  return {
    store: AppStore.GOOGLE_PLAY,
    userStatus:
      raw.userStatus != null
        ? googlePlayUserStatusMap[raw.userStatus]
        : undefined,
    ageLower: raw.ageLower,
    ageUpper: raw.ageUpper,
    mostRecentApprovalDate: raw.mostRecentApprovalDate,
    error: raw.error,
    raw,
  };
}

export function normalizeAmazonResult(
  raw: AmazonGetUserAgeDataResult
): AgeSignalsResult<AmazonGetUserAgeDataResult> {
  // Anything other than SUCCESS is a failed request; all other fields are
  // null. SUCCESS with an empty userStatus means the age-verification law
  // does not apply to this user (or the app is part of Amazon Kids+).
  if (raw.responseStatus !== AmazonGetUserAgeDataResponseStatus.SUCCESS) {
    const status =
      raw.responseStatus != null
        ? AmazonGetUserAgeDataResponseStatusString[raw.responseStatus]
        : 'NO_RESPONSE';
    return {
      store: AppStore.AMAZON_APPSTORE,
      error: `Amazon GetUserAgeData failed: ${status}`,
      raw,
    };
  }

  return {
    store: AppStore.AMAZON_APPSTORE,
    userStatus:
      raw.userStatus != null ? amazonUserStatusMap[raw.userStatus] : undefined,
    ageLower: raw.ageLower,
    ageUpper: raw.ageUpper,
    mostRecentApprovalDate: raw.mostRecentApprovalDate,
    raw,
  };
}

export function normalizeSamsungResult(
  raw: SamsungGetAgeSignalsResult
): AgeSignalsResult<SamsungGetAgeSignalsResult> {
  // result_code 1 means the call failed; result_message says why.
  if (raw.result_code != null && raw.result_code !== 0) {
    return {
      store: AppStore.SAMSUNG_GALAXY_STORE,
      error: raw.result_message ?? 'Samsung getAgeSignalResult failed',
      raw,
    };
  }

  return {
    store: AppStore.SAMSUNG_GALAXY_STORE,
    userStatus:
      raw.userStatus != null ? samsungUserStatusMap[raw.userStatus] : undefined,
    ageLower: raw.ageLower,
    ageUpper: raw.ageUpper,
    mostRecentApprovalDate: raw.mostRecentApprovalDate,
    raw,
  };
}
