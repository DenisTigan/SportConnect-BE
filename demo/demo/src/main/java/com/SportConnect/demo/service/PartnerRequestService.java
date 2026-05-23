package com.SportConnect.demo.service;

import com.SportConnect.demo.model.PartnerRequest;
import com.SportConnect.demo.model.RequestStatus;
import com.SportConnect.demo.model.Role;
import com.SportConnect.demo.model.User;
import com.SportConnect.demo.repository.PartnerRequestRepository;
import com.SportConnect.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PartnerRequestService {

    private final PartnerRequestRepository requestRepository;
    private final UserRepository userRepository;

    public PartnerRequestService(PartnerRequestRepository requestRepository, UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    // 1. Clientul trimite o cerere
    public String submitRequest(String email, String message) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost gasit!"));

        if (user.getRole() == Role.ROLE_PARTNER || user.getRole() == Role.ROLE_ADMIN) {
            throw new RuntimeException("Ești deja Partener sau Admin!");
        }

        if (requestRepository.existsByUserEmailAndStatus(email, RequestStatus.PENDING)) {
            throw new RuntimeException("Ai deja o cerere in asteptare!");
        }

        PartnerRequest request = new PartnerRequest(user, message, RequestStatus.PENDING);
        requestRepository.save(request);

        return "Cererea a fost trimisa cu succes!";
    }

    // 2. Adminul vede toate cererile PENDING
    public List<Map<String, Object>> getPendingRequests() {
        return requestRepository.findByStatus(RequestStatus.PENDING).stream()
                .map(req -> Map.<String, Object>of(
                        "id", req.getId(),
                        "userEmail", req.getUser().getEmail(),
                        "userName", req.getUser().getFirstName() + " " + req.getUser().getLastName(),
                        "message", req.getMessage(),
                        "createdAt", req.getCreatedAt()
                )).toList();
    }

    // 3. Adminul aproba cererea
    public String approveRequest(Long requestId) {
        PartnerRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Cererea nu exista!"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Aceasta cerere a fost deja procesata!");
        }

        // Promovăm utilizatorul
        User user = request.getUser();
        user.setRole(Role.ROLE_PARTNER);
        userRepository.save(user);

        // Actualizăm statusul cererii
        request.setStatus(RequestStatus.APPROVED);
        requestRepository.save(request);

        return "Cererea a fost aprobata! " + user.getEmail() + " este acum PARTENER.";
    }

    // 4. Adminul respinge cererea (Simplificat)
    public String rejectRequest(Long requestId) {
        // 1. Căutăm cererea în bază
        PartnerRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Cererea nu exista!"));

        // 2. Verificăm să nu fie deja procesată (să fie strict PENDING)
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Aceasta cerere a fost deja procesata (este aprobată sau respinsă)!");
        }

        // 3. Schimbăm statusul direct în REJECTED
        request.setStatus(RequestStatus.REJECTED);

        // 4. Salvăm modificarea
        requestRepository.save(request);

        return "Cererea a fost respinsă! Utilizatorul a rămas CLIENT.";
    }
}
