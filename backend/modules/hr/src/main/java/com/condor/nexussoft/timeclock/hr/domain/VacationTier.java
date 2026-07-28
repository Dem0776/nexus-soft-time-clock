package com.condor.nexussoft.timeclock.hr.domain;

/** Tramo de la escalera de vacaciones: días de derecho al cumplir `year` años de antigüedad. */
public record VacationTier(int year, int days) {}
