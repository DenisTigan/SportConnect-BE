package com.SportConnect.demo.controller;


import com.SportConnect.demo.dto.FieldRequest;
import com.SportConnect.demo.dto.FieldResponse;
import com.SportConnect.demo.service.FieldService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/fields")
public class FieldController {

    private final FieldService fieldService;

    public FieldController(FieldService fieldService) {
        this.fieldService = fieldService;
    }

    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyAuthority('ROLE_PARTNER', 'ROLE_ADMIN')")
    public ResponseEntity<FieldResponse> addField(
            @RequestPart("fieldDetails") FieldRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication) throws IOException {

        String ownerEmail = authentication.getName();
        FieldResponse response = fieldService.addField(request, ownerEmail, image);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<FieldResponse>> getFields() {
        List<FieldResponse> fields = fieldService.getAllFields();

        return ResponseEntity.ok(fields);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<FieldResponse>> getFieldsByCategory(@PathVariable String category) {
        List<FieldResponse> fields = fieldService.getFieldsByCategory(category);
        return ResponseEntity.ok(fields);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FieldResponse> getFieldById(@PathVariable Long id) {
        FieldResponse field = fieldService.getFieldById(id);
        return ResponseEntity.ok(field);
    }

    @PutMapping(value = "/update/{id}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyAuthority('ROLE_PARTNER', 'ROLE_ADMIN')")
    public ResponseEntity<FieldResponse> updateField(
            @PathVariable Long id,
            @RequestPart("fieldDetails") FieldRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication) throws IOException {

        String currentUserEmail = authentication.getName();
        FieldResponse response = fieldService.updateField(id, request, currentUserEmail, image);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_PARTNER', 'ROLE_ADMIN')")
    public ResponseEntity<String> deleteField(@PathVariable Long id, Authentication authentication) {
        String currentUserEmail = authentication.getName();
        fieldService.deleteField(id, currentUserEmail);
        return ResponseEntity.ok("Terenul a fost șters cu succes!");
    }

}
