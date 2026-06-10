package com.minifullstack.ems.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AssignWorkflowRequestDto {
    private List<Long> reviewerIds;
    private List<Long> approverIds;
}