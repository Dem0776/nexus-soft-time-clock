package com.condor.nexussoft.timeclock.hr.domain;

/**
 * Qué hacer con las antigüedades mayores al último tramo definido:
 *  - FLAT: se mantiene el valor del último tramo.
 *  - INCREMENT: se suman `beyondIncrementDays` días cada `beyondEveryYears` años.
 */
public enum BeyondMode { FLAT, INCREMENT }
