package com.minifullstack.ems.dto.response;

import com.minifullstack.ems.enums.WorkflowActionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WorkflowActionDto {
    private Long id;
    private String performedByName;
    private String performedByEmail;
    private WorkflowActionType actionType;
    private String note;
    private LocalDateTime createdAt;
}