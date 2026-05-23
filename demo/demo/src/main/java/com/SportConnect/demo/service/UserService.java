package com.SportConnect.demo.service;

import com.SportConnect.demo.dto.UserResponse;
import com.SportConnect.demo.dto.UserUpdateRequest;
import com.SportConnect.demo.model.User;
import com.SportConnect.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost găsit!"));

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().name()
        );
    }

    // Adaugă asta sub metoda getUserProfile existentă
    public UserResponse updateUserProfile(String email, UserUpdateRequest request) {
        // 1. Găsim utilizatorul curent în baza de date
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost găsit!"));

        // 2. Actualizăm datele, verificând mai întâi dacă ne-a trimis date valide
        if (request.firstName() != null && !request.firstName().isBlank()) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            user.setLastName(request.lastName());
        }
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            user.setPhoneNumber(request.phoneNumber());
        }

        // 3. Salvăm noile date în baza de date
        userRepository.save(user);

        // 4. Returnăm noul profil actualizat
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().name()
        );
    }
}
