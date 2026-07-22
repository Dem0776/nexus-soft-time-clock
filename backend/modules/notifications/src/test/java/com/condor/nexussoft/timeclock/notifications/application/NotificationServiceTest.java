package com.condor.nexussoft.timeclock.notifications.application;

import com.condor.nexussoft.timeclock.notifications.domain.Notification;
import com.condor.nexussoft.timeclock.notifications.domain.port.out.NotificationRepositoryPort;
import com.condor.nexussoft.timeclock.notifications.domain.port.out.PushSenderPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepositoryPort notifications;
    @Mock PushSenderPort push;

    @Test
    void notifyRejectedAttendance_enviaPushYPersisteComoSent() {
        NotificationService service = new NotificationService(notifications, push);
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(push.send(eq(userId), any(), any())).thenReturn(true);

        service.notifyRejectedAttendance(tenantId, userId, "OUT_OF_GEOFENCE");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notifications).save(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(Notification.Status.SENT);
        assertThat(captor.getValue().type()).isEqualTo("ATTENDANCE_REJECTED");
        assertThat(captor.getValue().body()).contains("OUT_OF_GEOFENCE");
    }
}
