package com.condor.nexussoft.timeclock.reporting.infrastructure.web;

import com.condor.nexussoft.timeclock.platform.tenant.TenantContext;
import com.condor.nexussoft.timeclock.reporting.application.DashboardService;
import com.condor.nexussoft.timeclock.reporting.application.DashboardSummary;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Indicadores del dashboard (RF-24). Requiere {@code dashboard:read}. */
@RestController
@RequestMapping("/api/v1/dashboard")
@PreAuthorize("hasAuthority('dashboard:read')")
public class DashboardController {

    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/summary")
    public DashboardSummary summary() {
        return dashboard.summary(TenantContext.require());
    }
}
