class AgeRangeThresholdManager {
  private static instance: AgeRangeThresholdManager;
  private thresholds: [number, number?, number?] | null = null;

  private constructor() {}

  static getInstance(): AgeRangeThresholdManager {
    if (!AgeRangeThresholdManager.instance) {
      AgeRangeThresholdManager.instance = new AgeRangeThresholdManager();
    }
    return AgeRangeThresholdManager.instance;
  }

  setAgeRangeThresholds(thresholds: [number, number?, number?]): void {
    if (thresholds[0] === undefined || thresholds[0] === null) {
      throw new Error(
        'PlayAgeRangeDeclaration: First threshold age is required'
      );
    }

    // Validate: minimum value is 1, maximum value is 18
    if (thresholds[0] < 1 || thresholds[0] > 18) {
      throw new Error(
        'PlayAgeRangeDeclaration: setAgeRangeThresholds: Age range thresholds must be between 1 and 18 (inclusive)'
      );
    }

    // Validate: ascending order
    for (let i = 1; i < thresholds.length; i++) {
      const current = thresholds[i];
      const previous = thresholds[i - 1];
      if (
        current !== undefined &&
        previous !== undefined &&
        current <= previous
      ) {
        throw new Error(
          'PlayAgeRangeDeclaration: Age range thresholds must be in ascending order'
        );
      }
    }

    // Validate: thresholds must be at least 2 years apart
    for (let i = 1; i < thresholds.length; i++) {
      const current = thresholds[i];
      const previous = thresholds[i - 1];
      if (
        current !== undefined &&
        previous !== undefined &&
        current - previous < 2
      ) {
        throw new Error(
          'PlayAgeRangeDeclaration: Age range thresholds must be at least 2 years apart'
        );
      }
    }

    this.thresholds = [thresholds[0]!, thresholds[1], thresholds[2]];
  }

  getThresholds(): [number, number?, number?] {
    if (this.thresholds === null) {
      throw new Error('PlayAgeRangeDeclaration: ageRangeThresholds not set');
    }

    return this.thresholds;
  }
}

export const ageRangeThresholdManager = AgeRangeThresholdManager.getInstance();
