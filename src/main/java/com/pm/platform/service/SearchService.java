package com.pm.platform.service;

import com.pm.platform.domain.IssueEntity;
import com.pm.platform.domain.Priority;
import com.pm.platform.domain.ProjectEntity;
import com.pm.platform.dto.IssueDto;
import com.pm.platform.dto.SearchResultDto;
import com.pm.platform.repository.IssueRepository;
import jakarta.persistence.criteria.JoinType;
import java.util.Base64;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchService {

    private final IssueRepository issueRepository;
    private final IssueMapper issueMapper;
    private final IssueService issueService;

    public SearchService(IssueRepository issueRepository, IssueMapper issueMapper, IssueService issueService) {
        this.issueRepository = issueRepository;
        this.issueMapper = issueMapper;
        this.issueService = issueService;
    }

    @Transactional(readOnly = true)
    public SearchResultDto search(
            String projectId,
            String q,
            String statusName,
            String assigneeId,
            Priority minPriority,
            int limit,
            String cursor) {
        ProjectEntity project = issueService.requireProject(projectId);
        int page = decodeCursor(cursor);
        int size = Math.min(Math.max(limit, 1), 100);

        Specification<IssueEntity> spec =
                (root, query, cb) -> {
                    var p = cb.conjunction();
                    p = cb.and(p, cb.equal(root.get("project"), project));
                    if (q != null && !q.isBlank()) {
                        String like = "%" + q.trim().toLowerCase() + "%";
                        p = cb.and(
                                p,
                                cb.or(
                                        cb.like(cb.lower(root.get("title")), like),
                                        cb.like(cb.lower(root.get("description")), like)));
                    }
                    if (statusName != null && !statusName.isBlank()) {
                        var st = root.join("status", JoinType.INNER);
                        p = cb.and(p, cb.equal(cb.lower(st.get("name")), statusName.toLowerCase()));
                    }
                    if (assigneeId != null) {
                        var a = root.join("assignee", JoinType.INNER);
                        p = cb.and(p, cb.equal(a.get("id"), assigneeId));
                    }
                    if (minPriority != null) {
                        p = cb.and(p, cb.greaterThanOrEqualTo(root.get("priority"), minPriority));
                    }
                    return p;
                };

        Page<IssueEntity> result =
                issueRepository.findAll(spec, PageRequest.of(page, size, Sort.by("issueNumber").ascending()));
        List<IssueDto> items = result.getContent().stream().map(issueMapper::toDto).toList();
        String next = result.hasNext() ? encodeCursor(page + 1) : null;
        return new SearchResultDto(next, items);
    }

    private static String encodeCursor(int page) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(("p=" + page).getBytes());
    }

    private static int decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            String s = new String(Base64.getUrlDecoder().decode(cursor));
            if (s.startsWith("p=")) {
                return Integer.parseInt(s.substring(2));
            }
        } catch (Exception ignored) {
        }
        return 0;
    }
}
