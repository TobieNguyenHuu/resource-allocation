package com.company.resourceallocation.service;

import com.company.resourceallocation.dto.response.AvailableResourceResponse;
import com.company.resourceallocation.dto.response.OverloadedResponse;
import com.company.resourceallocation.dto.response.UtilizationResponse;
import com.company.resourceallocation.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Service báo cáo (FR-11, FR-12, FR-13). Đọc-only. */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final int MAX_ALLOCATION = 100;

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<UtilizationResponse> utilization() {
        return reportRepository.utilization().stream()
                .map(r -> new UtilizationResponse(
                        (Long) r[0], (String) r[1], (String) r[2], toInt(r[3])))
                .toList();
    }

    public List<AvailableResourceResponse> availableResources(int minAvailable) {
        return reportRepository.available().stream()
                .map(r -> new AvailableResourceResponse(
                        (Long) r[0], (String) r[1], (String) r[2], MAX_ALLOCATION - toInt(r[3])))
                .filter(res -> res.available() >= minAvailable)
                .toList();
    }

    public List<OverloadedResponse> overloaded() {
        return reportRepository.overloaded().stream()
                .map(r -> new OverloadedResponse(
                        (Long) r[0], (String) r[1], (String) r[2], toInt(r[3])))
                .toList();
    }

    /** SUM trong JPQL trả Long; COALESCE có thể trả Integer/Long — dùng Number cho an toàn. */
    private static int toInt(Object value) {
        return ((Number) value).intValue();
    }
}
