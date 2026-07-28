/** Un tramo de la escalera: al cumplir `year` años de antigüedad, corresponden `days` días. */
export interface VacationTier {
  year: number;
  days: number;
}

/** Qué pasa con las antigüedades mayores al último tramo definido. */
export type BeyondMode = 'FLAT' | 'INCREMENT';

/** Política de vacaciones de la empresa (escalera por antigüedad + regla posterior). */
export interface VacationPolicy {
  /** Escalera año → días, ordenada por año. */
  tiers: VacationTier[];
  /** FLAT: se mantiene el último valor · INCREMENT: sube +N días cada M años. */
  beyondMode: BeyondMode;
  beyondIncrementDays: number;
  beyondEveryYears: number;
  requireApproval: boolean;
  countBusinessDaysOnly: boolean;
}
