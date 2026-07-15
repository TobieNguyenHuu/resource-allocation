package com.company.resourceallocation.exception;

import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BR2 — tổng allocation của nhân viên vượt 100%. HTTP 409, code ALLOCATION_EXCEEDED.
 *
 * <p>Giữ đúng message tối thiểu theo assignment: {@code "Employee allocation exceeds 100%"}.
 * Bổ sung khối {@code details} giàu ngữ cảnh (§10.2 PRD): currentAllocation, requestedAllocation,
 * resultingAllocation, maxAllocation.
 *
 * <p>Với create: {@code currentAllocation} = tổng hiện tại của nhân viên.
 * Với update: {@code currentAllocation} = tổng của các allocation KHÁC (đã loại trừ bản ghi đang sửa).
 */
public class AllocationExceededException extends ApiException {

    public AllocationExceededException(Long employeeId, int currentAllocation, int requestedAllocation) {
        super(HttpStatus.CONFLICT, "ALLOCATION_EXCEEDED", "Employee allocation exceeds 100%",
                buildDetails(employeeId, currentAllocation, requestedAllocation));
    }

    private static Map<String, Object> buildDetails(Long employeeId, int currentAllocation,
                                                    int requestedAllocation) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("employeeId", employeeId);
        details.put("currentAllocation", currentAllocation);
        details.put("requestedAllocation", requestedAllocation);
        details.put("resultingAllocation", currentAllocation + requestedAllocation);
        details.put("maxAllocation", 100);
        return details;
    }
}
