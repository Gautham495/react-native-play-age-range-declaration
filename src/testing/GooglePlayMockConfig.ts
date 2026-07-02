import type { PlayAgeSignalsUserStatus } from '../providers/GooglePlayAgeSignals';

export interface GooglePlayMockConfig {
  userStatus: PlayAgeSignalsUserStatus;
  ageLower?: number;
  ageUpper?: number;
  installId?: string;
  mostRecentApprovalDate?: string; // ISO 8601 format (YYYY-MM-DD)
}
