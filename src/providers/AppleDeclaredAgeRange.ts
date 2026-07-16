// https://developer.apple.com/documentation/declaredagerange/agerangeservice/agerangedeclaration#Determining-the-age-set-method
export type AppleAgeRangeDeclarationUserStatusValues =
  | 'checkedByOtherMethod'
  | 'governmentIDChecked'
  | 'guardianCheckedByOtherMethod'
  | 'guardianDeclared'
  | 'guardianGovernmentIDChecked'
  | 'guardianPaymentChecked'
  | 'paymentChecked'
  | 'selfDeclared'
  | 'declined'
  | 'unknown';

export interface DeclaredAgeRangeResult {
  isEligible: boolean;
  status?: AppleAgeRangeDeclarationUserStatusValues;
  parentControls?: string;
  lowerBound?: number;
  upperBound?: number;
}
