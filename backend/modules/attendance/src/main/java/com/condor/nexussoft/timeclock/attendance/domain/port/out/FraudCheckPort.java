package com.condor.nexussoft.timeclock.attendance.domain.port.out;

import java.util.List;

/** Evalúa las señales antifraude del dispositivo (delegado al BC Anti-Fraud). */
public interface FraudCheckPort {

    record FraudCheckResult(List<String> flagTypes, boolean blocked, String blockingReason) {
    }

    FraudCheckResult evaluate(boolean mockLocation, boolean rootedOrJailbroken, boolean gpsSpoofApp,
                              boolean gpsDisabled, boolean deviceTrusted);
}
