package com.condor.nexussoft.timeclock.attendance.infrastructure.integration;

import com.condor.nexussoft.timeclock.antifraud.domain.DeviceSignals;
import com.condor.nexussoft.timeclock.antifraud.domain.FraudEvaluation;
import com.condor.nexussoft.timeclock.antifraud.domain.port.in.FraudEvaluationUseCase;
import com.condor.nexussoft.timeclock.attendance.domain.port.out.FraudCheckPort;
import org.springframework.stereotype.Component;

/** Puente hacia el BC Anti-Fraud para evaluar las señales del dispositivo (RF-20). */
@Component
public class FraudCheckAdapter implements FraudCheckPort {

    private final FraudEvaluationUseCase fraud;

    public FraudCheckAdapter(FraudEvaluationUseCase fraud) {
        this.fraud = fraud;
    }

    @Override
    public FraudCheckResult evaluate(boolean mockLocation, boolean rootedOrJailbroken, boolean gpsSpoofApp,
                                     boolean gpsDisabled, boolean deviceTrusted) {
        FraudEvaluation ev = fraud.evaluate(new DeviceSignals(
                mockLocation, rootedOrJailbroken, gpsSpoofApp, gpsDisabled, deviceTrusted));
        return new FraudCheckResult(
                ev.flags().stream().map(f -> f.type().name()).toList(),
                ev.blocked(),
                ev.blockingReason());
    }
}
