package com.condor.nexussoft.timeclock.attendance.infrastructure.integration;

import com.condor.nexussoft.timeclock.attendance.domain.port.out.DeviceRecognitionPort;
import com.condor.nexussoft.timeclock.identity.domain.port.in.DeviceBindingUseCase;
import com.condor.nexussoft.timeclock.identity.domain.port.in.DeviceBindingUseCase.DeviceDecision;
import com.condor.nexussoft.timeclock.identity.domain.port.in.DeviceBindingUseCase.DeviceInfo;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Puente hacia el BC Identity para reconocer el dispositivo del colaborador (RF-28). */
@Component
public class DeviceRecognitionAdapter implements DeviceRecognitionPort {

    private final DeviceBindingUseCase deviceBinding;

    public DeviceRecognitionAdapter(DeviceBindingUseCase deviceBinding) {
        this.deviceBinding = deviceBinding;
    }

    @Override
    public DeviceRecognition resolve(UUID tenantId, UUID userId, String deviceIdentifier,
                                     String platform, String model, String osVersion) {
        DeviceDecision decision = deviceBinding.resolve(tenantId, userId, deviceIdentifier,
                new DeviceInfo(platform, model, osVersion));
        return new DeviceRecognition(decision.recognized(), switch (decision.action()) {
            case ALLOW -> Action.ALLOW;
            case FLAG -> Action.FLAG;
            case REJECT -> Action.REJECT;
        });
    }
}
