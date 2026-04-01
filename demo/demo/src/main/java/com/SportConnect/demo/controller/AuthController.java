package com.SportConnect.demo.controller;


import com.SportConnect.demo.dto.AuthRespone;
import com.SportConnect.demo.dto.LoginRequest;
import com.SportConnect.demo.dto.RegisterRequest;
import com.SportConnect.demo.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {

        try {
            String responseMessages = authService.register(request);

            return  new ResponseEntity<>(responseMessages, HttpStatus.CREATED);
        }catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthRespone> login(@RequestBody LoginRequest request) {
        AuthRespone token = authService.login(request);

        return ResponseEntity.ok(token);
    }


}
