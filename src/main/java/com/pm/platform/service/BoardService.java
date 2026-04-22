package com.pm.platform.service;

import com.pm.platform.domain.ProjectEntity;
import com.pm.platform.domain.WorkflowStatusEntity;
import com.pm.platform.dto.BoardColumnDto;
import com.pm.platform.dto.BoardDto;
import com.pm.platform.dto.IssueDto;
import com.pm.platform.repository.IssueRepository;
import com.pm.platform.repository.WorkflowStatusRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardService {

    private final IssueRepository issueRepository;
    private final WorkflowStatusRepository workflowStatusRepository;
    private final IssueMapper issueMapper;
    private final IssueService issueService;

    public BoardService(
            IssueRepository issueRepository,
            WorkflowStatusRepository workflowStatusRepository,
            IssueMapper issueMapper,
            IssueService issueService) {
        this.issueRepository = issueRepository;
        this.workflowStatusRepository = workflowStatusRepository;
        this.issueMapper = issueMapper;
        this.issueService = issueService;
    }

    @Transactional(readOnly = true)
    public BoardDto getBoard(String projectId) {
        ProjectEntity project = issueService.requireProject(projectId);
        List<WorkflowStatusEntity> statuses = workflowStatusRepository.findByProjectOrderBySortOrderAsc(project);
        List<BoardColumnDto> columns = new ArrayList<>();
        for (WorkflowStatusEntity st : statuses) {
            List<IssueDto> issues =
                    issueRepository.findByProjectAndStatusOrderByIssueNumberAsc(project, st).stream()
                            .map(issueMapper::toDto)
                            .toList();
            columns.add(new BoardColumnDto(st.getId(), st.getName(), issues));
        }
        return new BoardDto(project.getId(), columns);
    }
}
