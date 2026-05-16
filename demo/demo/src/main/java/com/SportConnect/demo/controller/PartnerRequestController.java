package com.SportConnect.demo.controller;

import com.SportConnect.demo.service.PartnerRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/partner-requests")
public class PartnerRequestController {

    private final PartnerRequestService requestService;

    public PartnerRequestController(PartnerRequestService requestService) {
        this.requestService = requestService;
    }

    // URL pt Client: POST /api/partner-requests/submit
    @PostMapping("/submit")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<String> submitRequest(@RequestBody Map<String, String> payload, Authentication authentication) {
        String email = authentication.getName();
        String message = payload.get("message");
        return ResponseEntity.ok(requestService.submitRequest(email, message));
    }

    // URL pt Admin: GET /api/partner-requests/pending
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getPendingRequests() {
        return ResponseEntity.ok(requestService.getPendingRequests());
    }

    // URL pt Admin: POST /api/partner-requests/approve/1
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> approveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.approveRequest(id));
    }
}
