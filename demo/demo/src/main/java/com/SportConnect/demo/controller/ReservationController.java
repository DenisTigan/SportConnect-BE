package com.SportConnect.demo.controller;


import com.SportConnect.demo.dto.ReservationRequest;
import com.SportConnect.demo.dto.ReservationResponse;
import com.SportConnect.demo.service.ReservationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController( ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/create")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationRequest request, Authentication authentication) {

        String clientEmail = authentication.getName();

        ReservationResponse response = reservationService.createReservation(request, clientEmail);

        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }


    @GetMapping("my-history")
    public ResponseEntity<List<ReservationResponse>> getMyHistory(Authentication authentication) {
        return ResponseEntity.ok(reservationService.getClientHistory(authentication.getName()));
    }


    @GetMapping("owner-calendar")
    @PreAuthorize("hasAnyAuthority('ROLE_PARTNER', 'ROLE_ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getOwnerCalendar(Authentication authentication) {
        return ResponseEntity.ok(reservationService.getOwnerHistory(authentication.getName()));
    }

    @PutMapping("/cancel/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> cancelReservation(
            @PathVariable Long id,
            Authentication authentication) {

        String currentUserEmail = authentication.getName();
        String responseMessage = reservationService.cancelReservation(id, currentUserEmail);

        return ResponseEntity.ok(responseMessage);
    }


    @GetMapping("/occupied")
    public ResponseEntity<List<ReservationResponse>> getOccupiedSlots(
            @RequestParam Long fieldId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<ReservationResponse> occupiedSlots = reservationService.getOccupiedSlots(fieldId, date);
        return ResponseEntity.ok(occupiedSlots);
    }
}
