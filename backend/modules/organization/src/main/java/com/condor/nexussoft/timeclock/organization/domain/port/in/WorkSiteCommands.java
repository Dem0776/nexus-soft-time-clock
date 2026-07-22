package com.condor.nexussoft.timeclock.organization.domain.port.in;

public final class WorkSiteCommands {

    private WorkSiteCommands() {
    }

    public record CreateWorkSiteCommand(String code, String name, String address,
                                        double latitude, double longitude, String timezone,
                                        Integer gpsAccuracyMaxM, Boolean requirePhoto, Boolean requireBiometric) {
    }

    public record UpdateWorkSiteCommand(String name, String address,
                                        double latitude, double longitude, String timezone,
                                        Integer gpsAccuracyMaxM, Boolean requirePhoto, Boolean requireBiometric) {
    }
}
