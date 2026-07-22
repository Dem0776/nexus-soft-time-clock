package com.condor.nexussoft.timeclock.organization.domain;

/** Value Object de coordenada geográfica (WGS84). */
public record GeoPoint(double latitude, double longitude) {

    public GeoPoint {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitud fuera de rango: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitud fuera de rango: " + longitude);
        }
    }
}
