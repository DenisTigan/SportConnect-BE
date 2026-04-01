package com.SportConnect.demo.service;


import com.SportConnect.demo.dto.AuthRespone;
import com.SportConnect.demo.dto.LoginRequest;
import com.SportConnect.demo.dto.RegisterRequest;
import com.SportConnect.demo.model.Role;
import com.SportConnect.demo.model.User;
import com.SportConnect.demo.repository.UserRepository;
import com.SportConnect.demo.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())){
            throw new RuntimeException("Emailul este deja folosit");
        }
        Role userRole;

        try {
            userRole = Role.valueOf("ROLE_" + request.role().toUpperCase());
        }catch (IllegalArgumentException e){
            userRole = Role.ROLE_CLIENT;
        }

        User user = new User(
                request.firstName(),
                request.lastName(),
                request.email(),
                passwordEncoder.encode(request.password()),
                userRole,
                true
                );
        userRepository.save(user);

        return "Utilizator inregistrat cu succes";
    }

    public AuthRespone login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        return new AuthRespone(token);
    }

}
