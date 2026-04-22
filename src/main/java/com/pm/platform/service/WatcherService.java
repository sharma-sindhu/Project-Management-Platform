package com.pm.platform.service;

import com.pm.platform.domain.IssueWatcherEntity;
import com.pm.platform.repository.IssueWatcherRepository;
import com.pm.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WatcherService {

    private final IssueWatcherRepository issueWatcherRepository;
    private final UserRepository userRepository;
    private final IssueService issueService;

    public WatcherService(
            IssueWatcherRepository issueWatcherRepository, UserRepository userRepository, IssueService issueService) {
        this.issueWatcherRepository = issueWatcherRepository;
        this.userRepository = userRepository;
        this.issueService = issueService;
    }

    @Transactional
    public void watch(String issueId, String userId) {
        var issue = issueService.requireIssue(issueId);
        var user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!issueWatcherRepository.existsByIssueAndUser(issue, user)) {
            issueWatcherRepository.save(new IssueWatcherEntity(issue, user));
        }
    }

    @Transactional
    public void unwatch(String issueId, String userId) {
        var issue = issueService.requireIssue(issueId);
        var user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        issueWatcherRepository.deleteByIssueAndUser(issue, user);
    }
}
