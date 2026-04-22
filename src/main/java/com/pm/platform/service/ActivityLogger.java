package com.pm.platform.service;

import com.pm.platform.domain.ActivityLogEntity;
import com.pm.platform.domain.IssueEntity;
import com.pm.platform.domain.UserEntity;
import com.pm.platform.repository.ActivityLogRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityLogger {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogger(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public void log(IssueEntity issue, UserEntity actor, String action, String payloadJson) {
        activityLogRepository.save(
                new ActivityLogEntity(UUID.randomUUID().toString(), issue, actor, action, payloadJson));
    }
}
