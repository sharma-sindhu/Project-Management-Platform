package com.pm.platform.service;

import com.pm.platform.domain.IssueEntity;
import com.pm.platform.domain.NotificationEntity;
import com.pm.platform.domain.NotificationType;
import com.pm.platform.domain.UserEntity;
import com.pm.platform.repository.NotificationRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void notify(UserEntity user, IssueEntity issue, NotificationType type, String message) {
        notificationRepository.save(
                new NotificationEntity(UUID.randomUUID().toString(), user, issue, type, message));
    }
}
