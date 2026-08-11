package com.sdt.feedback.controller;

import com.sdt.feedback.dto.response.DashboardStatsResponse;
import com.sdt.feedback.dto.response.DashboardTrendResponse;
import com.sdt.feedback.enums.TrendInterval;
import com.sdt.feedback.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/trend")
    public ResponseEntity<DashboardTrendResponse> getTrend(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            @RequestParam(required = false)
            TrendInterval interval
    ) {
        return ResponseEntity.ok(
                dashboardService.getTrend(fromDate, toDate, interval)
        );
    }
}
