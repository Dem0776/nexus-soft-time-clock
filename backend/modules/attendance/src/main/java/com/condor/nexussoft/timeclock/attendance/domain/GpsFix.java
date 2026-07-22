package com.condor.nexussoft.timeclock.attendance.domain;

/** Lectura GPS: coordenadas y precisión (radio de incertidumbre en metros). */
public record GpsFix(double latitude, double longitude, double accuracyM) {

    public GpsFix {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("coordenadas fuera de rango");
        }
        if (accuracyM < 0) {
            throw new IllegalArgumentException("precisión inválida");
        }
    }
}
