package com.SportConnect.demo.controller;


import com.SportConnect.demo.dto.FieldRequest;
import com.SportConnect.demo.dto.FieldResponse;
import com.SportConnect.demo.service.FieldService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/fields")
public class FieldController {

    private final FieldService fieldService;

    public FieldController(FieldService fieldService) {
        this.fieldService = fieldService;
    }

    @PostMapping("/add")
    public ResponseEntity<FieldResponse> addField(@RequestBody FieldRequest request, Authentication authentication) {

        String ownerEmail = authentication.getName();

        FieldResponse response = fieldService.addField(request, ownerEmail);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<FieldResponse>> getFields() {
        List<FieldResponse> fields = fieldService.getAllFields();

        return ResponseEntity.ok(fields);
    }


}
