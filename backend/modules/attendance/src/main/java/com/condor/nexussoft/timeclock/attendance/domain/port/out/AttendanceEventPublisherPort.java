package com.condor.nexussoft.timeclock.attendance.domain.port.out;

import com.condor.nexussoft.timeclock.shared.domain.DomainEvent;

public interface AttendanceEventPublisherPort {

    void publish(DomainEvent event);
}
