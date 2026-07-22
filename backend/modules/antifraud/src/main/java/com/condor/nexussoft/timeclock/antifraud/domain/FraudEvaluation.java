package com.condor.nexussoft.timeclock.antifraud.domain;

import java.util.List;

/** Resultado de la evaluación antifraude. */
public record FraudEvaluation(List<FraudFlag> flags, boolean blocked, String blockingReason) {

    public boolean hasFlags() {
        return !flags.isEmpty();
    }
}
