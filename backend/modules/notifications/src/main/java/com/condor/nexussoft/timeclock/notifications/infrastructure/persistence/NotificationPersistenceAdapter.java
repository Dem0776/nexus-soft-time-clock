package com.condor.nexussoft.timeclock.notifications.infrastructure.persistence;

import com.condor.nexussoft.timeclock.notifications.domain.Notification;
import com.condor.nexussoft.timeclock.notifications.domain.port.out.NotificationRepositoryPort;
import com.condor.nexussoft.timeclock.shared.domain.Paged;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class NotificationPersistenceAdapter implements NotificationRepositoryPort {

    private final NotificationJpaRepository jpa;

    public NotificationPersistenceAdapter(NotificationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Notification save(Notification n) {
        jpa.save(new NotificationJpaEntity(n.id(), n.tenantId(), n.userId(), n.channel().name(),
                n.type(), n.title(), n.body(), n.status().name()));
        return n;
    }

    @Override
    public Paged<Notification> findByUser(UUID tenantId, UUID userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<NotificationJpaEntity> result = jpa.findByTenantIdAndUserId(tenantId, userId, pageable);
        return new Paged<>(result.map(this::toDomain).getContent(),
                result.getNumber(), result.getSize(), result.getTotalElements());
    }

    private Notification toDomain(NotificationJpaEntity e) {
        return new Notification(e.getId(), e.getTenantId(), e.getUserId(),
                Notification.Channel.valueOf(e.getChannel()), e.getType(), e.getTitle(), e.getBody(),
                Notification.Status.valueOf(e.getStatus()));
    }
}
