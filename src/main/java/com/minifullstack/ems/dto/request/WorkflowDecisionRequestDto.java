package com.minifullstack.ems.dto.request;

import lombok.Data;

@Data
public class WorkflowDecisionRequestDto {
    private String decision; // "APPROVE" or "REJECT"
    private String note;
}