// https://developer.amazon.com/docs/app-submission/user-age-verification.html

export enum AmazonGetUserAgeDataResponseStatus {
  // The API successfully returned the user information.
  SUCCESS = 0,
  /**
   * The app that called the GetUserAgeData API wasn't installed from
   * Amazon Appstore.
   */
  APP_NOT_OWNED = 1,
  /**
   * An error occurred. Indicates the error is transient and might resolve
   * after retrying. Consider setting a maximum of two retries.
   */
  INTERNAL_TRANSIENT_ERROR = 2,
  // An error occurred. Indicates retries are unlikely to resolve the error.
  INTERNAL_ERROR = 3,
  // an app called the API, but isn't enabled by Amazon.
  FEATURE_NOT_SUPPORTED = 4,
}

export const AmazonGetUserAgeDataResponseStatusString: Record<
  AmazonGetUserAgeDataResponseStatus,
  string
> = {
  [AmazonGetUserAgeDataResponseStatus.SUCCESS]: 'SUCCESS',
  [AmazonGetUserAgeDataResponseStatus.APP_NOT_OWNED]: 'APP_NOT_OWNED',
  [AmazonGetUserAgeDataResponseStatus.INTERNAL_TRANSIENT_ERROR]:
    'INTERNAL_TRANSIENT_ERROR',
  [AmazonGetUserAgeDataResponseStatus.INTERNAL_ERROR]: 'INTERNAL_ERROR',
  [AmazonGetUserAgeDataResponseStatus.FEATURE_NOT_SUPPORTED]:
    'FEATURE_NOT_SUPPORTED',
};

export enum AmazonGetUserAgeDataUserStatus {
  VERIFIED = 0,
  SUPERVISED = 1,
  UNKNOWN = 2,
  CONSENT_NOT_GRANTED = 3,
}

export const AmazonGetUserAgeDataUserStatusString: Record<
  AmazonGetUserAgeDataUserStatus,
  string
> = {
  [AmazonGetUserAgeDataUserStatus.VERIFIED]: 'VERIFIED',
  [AmazonGetUserAgeDataUserStatus.SUPERVISED]: 'SUPERVISED',
  [AmazonGetUserAgeDataUserStatus.UNKNOWN]: 'UNKNOWN',
  [AmazonGetUserAgeDataUserStatus.CONSENT_NOT_GRANTED]: 'CONSENT_NOT_GRANTED',
};

/**
 * If the responseStatus field has any value other than SUCCESS,
 * all other fields in the response will be null or empty.
 */
export interface AmazonGetUserAgeDataResult {
  responseStatus?: AmazonGetUserAgeDataResponseStatus;
  /**
   * For a user in a region where the age verification law isn't applicable, the value is empty.
   * Also, if the app is a part of the Amazon Kids+ subscription, the value is empty.
   */
  userStatus?: AmazonGetUserAgeDataUserStatus;
  // Null if userStatus is UNKNOWN, or empty.
  ageLower?: number;
  /**
   * Null for a user who is 18+ years old.
   * Null if userStatus is VERIFIED, UNKNOWN, or empty.
   */
  ageUpper?: number;
  // Null if userStatus is VERIFIED, UNKNOWN, or empty.
  userId?: string;
  /**
   * Null for a user who is 18+ years old, where consent isn't applicable.
   * Null if userStatus is VERIFIED, UNKNOWN, or empty.
   */
  mostRecentApprovalDate?: string; // ISO 8601
}
