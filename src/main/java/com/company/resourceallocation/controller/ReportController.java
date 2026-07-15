package com.company.resourceallocation.controller;

import com.company.resourceallocation.dto.response.AvailableResourceResponse;
import com.company.resourceallocation.dto.response.OverloadedResponse;
import com.company.resourceallocation.dto.response.UtilizationResponse;
import com.company.resourceallocation.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST API cho báo cáo (FR-11, FR-12, FR-13). */
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/utilization")
    public List<UtilizationResponse> utilization() {
        return reportService.utilization();
    }

    @GetMapping("/available-resources")
    public List<AvailableResourceResponse> availableResources(
            @RequestParam(name = "minAvailable", defaultValue = "0") int minAvailable) {
        return reportService.availableResources(minAvailable);
    }

    @GetMapping("/overloaded")
    public List<OverloadedResponse> overloaded() {
        return reportService.overloaded();
    }
}
