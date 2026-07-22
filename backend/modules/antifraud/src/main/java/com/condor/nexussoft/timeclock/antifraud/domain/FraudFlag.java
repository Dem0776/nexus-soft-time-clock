package com.condor.nexussoft.timeclock.antifraud.domain;

/** Bandera antifraude detectada. {@code blocking} indica si impide aceptar el registro. */
public record FraudFlag(FraudFlagType type, boolean blocking, String detail) {
}
