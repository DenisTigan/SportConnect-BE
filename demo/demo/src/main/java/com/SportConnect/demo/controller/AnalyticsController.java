package com.SportConnect.demo.controller;


import com.SportConnect.demo.dto.AnalyticsResponse;
import com.SportConnect.demo.service.ReservationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final ReservationService reservationService;

    public AnalyticsController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // URL: GET /api/analytics/summary
    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('ROLE_PARTNER', 'ROLE_ADMIN')")
    public ResponseEntity<AnalyticsResponse> getAnalyticsSummary(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        String email = authentication.getName();
        AnalyticsResponse response = reservationService.getAnalytics(email, date);

        return ResponseEntity.ok(response);
    }
}
