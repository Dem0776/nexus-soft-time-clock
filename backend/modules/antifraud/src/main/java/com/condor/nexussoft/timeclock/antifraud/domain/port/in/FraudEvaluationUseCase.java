package com.condor.nexussoft.timeclock.antifraud.domain.port.in;

import com.condor.nexussoft.timeclock.antifraud.domain.DeviceSignals;
import com.condor.nexussoft.timeclock.antifraud.domain.FraudEvaluation;

/** Evalúa las señales del dispositivo contra la política antifraude (RF-20, RN-20..RN-28). */
public interface FraudEvaluationUseCase {

    FraudEvaluation evaluate(DeviceSignals signals);
}
