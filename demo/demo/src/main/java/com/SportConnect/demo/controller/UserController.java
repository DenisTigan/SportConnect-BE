package com.SportConnect.demo.controller;


import com.SportConnect.demo.dto.ChangePasswordRequest;
import com.SportConnect.demo.dto.UserResponse;
import com.SportConnect.demo.dto.UserUpdateRequest;
import com.SportConnect.demo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();

        UserResponse userProfile = userService.getUserProfile(email);

        return ResponseEntity.ok(userProfile);
    }
    // Adaugă asta sub metoda getCurrentUser existentă
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @RequestBody UserUpdateRequest request) {

        // Luăm email-ul din token-ul JWT (exact ca la GetMapping)
        String email = authentication.getName();

        // Trimitem cererea la Service
        UserResponse updatedProfile = userService.updateUserProfile(email, request);

        // Returnăm profilul actualizat cu statusul 200 OK
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/{id}/demote")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> demotePartner(@PathVariable Long id) {
        String resultMessage = userService.demotePartnerToClient(id);
        return ResponseEntity.ok(resultMessage);
    }

    @PutMapping("/me/change-password")
    public ResponseEntity<String> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest request) {

        String email = authentication.getName();
        String result = userService.changePassword(email, request);

        return ResponseEntity.ok(result);
    }
}
