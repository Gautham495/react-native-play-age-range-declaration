class AgeRangeThresholdManager {
  private static instance: AgeRangeThresholdManager;
  private thresholds: number[] | null = null;

  private constructor() {}

  static getInstance(): AgeRangeThresholdManager {
    if (!AgeRangeThresholdManager.instance) {
      AgeRangeThresholdManager.instance = new AgeRangeThresholdManager();
    }
    return AgeRangeThresholdManager.instance;
  }

  setAgeRangeThresholds(thresholds: number[]): void {
    if (thresholds.length < 1 || thresholds.length > 3) {
      throw new Error(
        'PlayAgeRangeDeclaration: Age range thresholds must contain at least 1 and at most 3 values'
      );
    }

    if (!thresholds.every((t) => Number.isInteger(t))) {
      throw new Error('PlayAgeRangeDeclaration: All age range thresholds must be integers');
    }

    // Validate: minimum value is 1, maximum value is 18
    if (thresholds.some((t) => t < 1 || t > 18)) {
      throw new Error(
        'PlayAgeRangeDeclaration: setAgeRangeThresholds: Age range thresholds must be between 1 and 18 (inclusive)'
      );
    }

    // Validate: ascending order
    for (let i = 1; i < thresholds.length; i++) {
      const current = thresholds[i];
      const previous = thresholds[i - 1];
      if (current !== undefined && previous !== undefined && current <= previous) {
        throw new Error(
          'PlayAgeRangeDeclaration: Age range thresholds must be in ascending order'
        );
      }
    }

    // Validate: no duplicates (already covered by ascending order check, but explicit check for clarity)
    const uniqueThresholds = new Set(thresholds);
    if (uniqueThresholds.size !== thresholds.length) {
      throw new Error('PlayAgeRangeDeclaration: Age range thresholds must not contain duplicates');
    }

    this.thresholds = [...thresholds];
  }

  getThresholds(): number[] {
    if (this.thresholds === null) {
      throw new Error('PlayAgeRangeDeclaration: ageRangeThresholds not set');
    }
    return [...this.thresholds];
  }
}

export const ageRangeThresholdManager = AgeRangeThresholdManager.getInstance();
