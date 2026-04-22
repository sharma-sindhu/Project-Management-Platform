package com.pm.platform.web;

import com.pm.platform.dto.ApiErrorDto;
import com.pm.platform.service.IssueNotFoundException;
import com.pm.platform.service.OptimisticLockConflictException;
import com.pm.platform.service.ProjectNotFoundException;
import com.pm.platform.service.WorkflowViolationException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WorkflowViolationException.class)
    public ResponseEntity<ApiErrorDto> workflow(WorkflowViolationException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiErrorDto("workflow_violation", e.getMessage(), e.getAllowedTransitions()));
    }

    @ExceptionHandler(OptimisticLockConflictException.class)
    public ResponseEntity<ApiErrorDto> conflict(OptimisticLockConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorDto("conflict", e.getMessage(), List.of()));
    }

    @ExceptionHandler({ProjectNotFoundException.class, IssueNotFoundException.class})
    public ResponseEntity<ApiErrorDto> notFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorDto("not_found", e.getMessage(), List.of()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorDto> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorDto("bad_request", e.getMessage(), List.of()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorDto> badState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorDto("illegal_state", e.getMessage(), List.of()));
    }
}
