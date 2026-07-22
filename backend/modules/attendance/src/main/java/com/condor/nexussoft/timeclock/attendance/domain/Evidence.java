package com.condor.nexussoft.timeclock.attendance.domain;

/** Evidencia fotográfica opcional (referencia en MinIO), RF-18. */
public record Evidence(String bucket, String key, String hash) {

    public static Evidence none() {
        return null;
    }
}
