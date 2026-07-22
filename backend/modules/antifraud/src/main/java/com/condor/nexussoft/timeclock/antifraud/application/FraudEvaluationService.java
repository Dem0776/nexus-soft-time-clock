package com.condor.nexussoft.timeclock.antifraud.application;

import com.condor.nexussoft.timeclock.antifraud.domain.DeviceSignals;
import com.condor.nexussoft.timeclock.antifraud.domain.FraudEvaluation;
import com.condor.nexussoft.timeclock.antifraud.domain.FraudFlag;
import com.condor.nexussoft.timeclock.antifraud.domain.FraudFlagType;
import com.condor.nexussoft.timeclock.antifraud.domain.port.in.FraudEvaluationUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Aplica la política antifraude por defecto de la plataforma (configurable; en el futuro
 * por tenant desde company_settings). Política: {@code REJECT} bloquea, {@code FLAG} solo marca.
 */
@Service
public class FraudEvaluationService implements FraudEvaluationUseCase {

    private final String mockLocationPolicy;
    private final String rootedPolicy;
    private final String gpsSpoofPolicy;

    public FraudEvaluationService(
            @Value("${security.fraud.mock-location-policy:REJECT}") String mockLocationPolicy,
            @Value("${security.fraud.rooted-policy:FLAG}") String rootedPolicy,
            @Value("${security.fraud.gps-spoof-policy:REJECT}") String gpsSpoofPolicy) {
        this.mockLocationPolicy = mockLocationPolicy;
        this.rootedPolicy = rootedPolicy;
        this.gpsSpoofPolicy = gpsSpoofPolicy;
    }

    @Override
    public FraudEvaluation evaluate(DeviceSignals s) {
        List<FraudFlag> flags = new ArrayList<>();

        if (s.gpsDisabled()) {
            flags.add(new FraudFlag(FraudFlagType.GPS_DISABLED, true, "GPS deshabilitado"));
        }
        if (s.mockLocation()) {
            flags.add(new FraudFlag(FraudFlagType.MOCK_LOCATION, blocks(mockLocationPolicy), "Ubicación simulada"));
        }
        if (s.gpsSpoofApp()) {
            flags.add(new FraudFlag(FraudFlagType.GPS_SPOOF_APP, blocks(gpsSpoofPolicy), "App de falsificación de GPS"));
        }
        if (s.rootedOrJailbroken()) {
            flags.add(new FraudFlag(FraudFlagType.ROOTED_DEVICE, blocks(rootedPolicy), "Dispositivo root/jailbreak"));
        }
        if (!s.deviceTrusted()) {
            flags.add(new FraudFlag(FraudFlagType.UNTRUSTED_DEVICE, false, "Dispositivo no verificado"));
        }

        FraudFlag blocking = flags.stream().filter(FraudFlag::blocking).findFirst().orElse(null);
        String reason = blocking == null ? null : reasonFor(blocking.type());
        return new FraudEvaluation(flags, blocking != null, reason);
    }

    private boolean blocks(String policy) {
        return "REJECT".equalsIgnoreCase(policy);
    }

    private String reasonFor(FraudFlagType type) {
        return switch (type) {
            case MOCK_LOCATION -> "FRAUD_MOCK_LOCATION";
            case GPS_SPOOF_APP -> "FRAUD_GPS_SPOOF_APP";
            case ROOTED_DEVICE -> "FRAUD_ROOTED_DEVICE";
            case GPS_DISABLED -> "GPS_UNAVAILABLE";
            case UNTRUSTED_DEVICE -> "UNTRUSTED_DEVICE";
        };
    }
}
