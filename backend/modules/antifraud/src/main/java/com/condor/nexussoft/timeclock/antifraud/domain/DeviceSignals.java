package com.condor.nexussoft.timeclock.antifraud.domain;

/** Señales antifraude reportadas por el dispositivo al registrar (RF-20). */
public record DeviceSignals(
        boolean mockLocation,
        boolean rootedOrJailbroken,
        boolean gpsSpoofApp,
        boolean gpsDisabled,
        boolean deviceTrusted) {
}
