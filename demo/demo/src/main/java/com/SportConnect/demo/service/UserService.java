package com.SportConnect.demo.service;

import com.SportConnect.demo.dto.ChangePasswordRequest;
import com.SportConnect.demo.dto.UserResponse;
import com.SportConnect.demo.dto.UserUpdateRequest;
import com.SportConnect.demo.model.Role;
import com.SportConnect.demo.model.User;
import com.SportConnect.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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


    // Adaugă importul pentru Role dacă nu îl ai deja:
    // import com.SportConnect.demo.model.Role;

    public String demotePartnerToClient(Long userId) {
        // 1. Căutăm utilizatorul
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost găsit!"));

        // 2. Protecție: Nu lăsăm un Admin să retrogradeze un alt Admin
        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new RuntimeException("Acțiune interzisă: Nu poți retrograda un administrator!");
        }

        // 3. Verificăm dacă este deja client
        if (user.getRole() == Role.ROLE_CLIENT) {
            throw new RuntimeException("Utilizatorul este deja un client normal!");
        }

        // 4. Îi retragem drepturile și îl facem CLIENT
        user.setRole(Role.ROLE_CLIENT);
        userRepository.save(user);

        return "Succes! Utilizatorul " + user.getEmail() + " a fost retrogradat la rolul de CLIENT.";
    }

    public String changePassword(String email, ChangePasswordRequest request) {
        // 1. Găsim utilizatorul curent
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost găsit!"));

        // 2. Verificăm dacă parola curentă este corectă
        // Metoda matches compară textul simplu cu hash-ul criptat din baza de date
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new RuntimeException("Parola curentă este incorectă!");
        }

        // 3. Verificăm dacă noua parolă și confirmarea ei coincid
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new RuntimeException("Noua parolă și confirmarea nu coincid!");
        }

        // 4. Validare opțională: Să nu pună aceeași parolă veche
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new RuntimeException("Noua parolă nu poate fi identică cu cea veche!");
        }

        // 5. Criptăm noua parolă și o salvăm
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return "Parola a fost modificată cu succes!";
    }
}
