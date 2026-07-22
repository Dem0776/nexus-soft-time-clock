package com.condor.nexussoft.timeclock.reporting.application;

import java.io.Serializable;

/** Indicadores del dashboard (read-model, RF-24). Serializable para la caché Redis. */
public record DashboardSummary(
        long attendanceTodayAccepted,
        long attendanceTodayRejected,
        long openIncidents,
        long activeUsers,
        long activeWorkSites) implements Serializable {
}
