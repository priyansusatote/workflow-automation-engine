package com.priyansu.workflow.service;

import com.priyansu.workflow.dto.DashboardResponse;
import com.priyansu.workflow.dto.WorkflowStatsResponse;

import java.util.List;

public interface DashboardService {

    DashboardResponse getDashboard();

    List<WorkflowStatsResponse> getWorkflowStats();

}