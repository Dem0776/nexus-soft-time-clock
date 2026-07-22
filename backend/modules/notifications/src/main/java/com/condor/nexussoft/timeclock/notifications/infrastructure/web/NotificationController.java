package com.condor.nexussoft.timeclock.notifications.infrastructure.web;

import com.condor.nexussoft.timeclock.notifications.domain.Notification;
import com.condor.nexussoft.timeclock.notifications.domain.port.in.NotificationUseCase;
import com.condor.nexussoft.timeclock.platform.tenant.TenantContext;
import com.condor.nexussoft.timeclock.platform.web.PageResponse;
import com.condor.nexussoft.timeclock.shared.domain.Paged;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Notificaciones del usuario autenticado (RF-27). */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationUseCase notifications;

    public NotificationController(NotificationUseCase notifications) {
        this.notifications = notifications;
    }

    public record NotificationResponse(UUID id, String channel, String type, String title,
                                       String body, String status) {
        static NotificationResponse from(Notification n) {
            return new NotificationResponse(n.id(), n.channel().name(), n.type(),
                    n.title(), n.body(), n.status().name());
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<NotificationResponse> myNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        Paged<Notification> result = notifications.list(TenantContext.require(), userId, page, size);
        return PageResponse.of(result.items().stream().map(NotificationResponse::from).toList(),
                result.page(), result.size(), result.total());
    }
}
