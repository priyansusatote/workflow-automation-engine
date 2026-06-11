package com.priyansu.workflow.controller;

import com.priyansu.workflow.dto.DashboardResponse;
import com.priyansu.workflow.dto.WorkflowStatsResponse;
import com.priyansu.workflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;


    @GetMapping
    public DashboardResponse getDashboard() {
        return dashboardService.getDashboard();
    }

    @GetMapping("/workflow-stats")
    public List<WorkflowStatsResponse> getWorkflowStats() {

        return dashboardService.getWorkflowStats();
    }

}