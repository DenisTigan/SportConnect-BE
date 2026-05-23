package com.SportConnect.demo.service;


import com.SportConnect.demo.dto.AnalyticsResponse;
import com.SportConnect.demo.dto.ReservationRequest;
import com.SportConnect.demo.dto.ReservationResponse;
import com.SportConnect.demo.model.Field;
import com.SportConnect.demo.model.Reservation;
import com.SportConnect.demo.model.User;
import com.SportConnect.demo.repository.FieldRepository;
import com.SportConnect.demo.repository.ReservationRepository;
import com.SportConnect.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservationService {


    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final FieldRepository fieldRepository;

    public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository, FieldRepository fieldRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.fieldRepository = fieldRepository;
    }

    public ReservationResponse createReservation(ReservationRequest request, String clientEmail) {


        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Clientul nu a fost gasit"));
        Field field = fieldRepository.findById(request.fieldId())
                .orElseThrow(() -> new RuntimeException("Terenul nu a fost gasit"));



        LocalDateTime now = LocalDateTime.now();
        if(request.startTime().isBefore(now)){
            throw new RuntimeException("Nu poti rezerva un teren in trecut!");
        }
        if(request.startTime().isAfter(request.endTime()) || request.startTime().isEqual(request.endTime())){
            throw new RuntimeException("Ora de final trebuie sa fie dupa ora de inceput!");
        }




        LocalTime requestedStartTime = request.startTime().toLocalTime();
        LocalTime requestedEndTime = request.endTime().toLocalTime();

        if(requestedStartTime.isBefore(field.getOpenTime()) || requestedEndTime.isAfter(field.getCloseTime())){
            throw new RuntimeException("Rezervarea este in afara programului de lucru (" + field.getOpenTime() + " - " + field.getCloseTime() + ")");
        }




        boolean isOccupied = reservationRepository.isFieldOccupied(field.getId(), request.startTime(), request.endTime());
        if(isOccupied){
            throw new RuntimeException("Terenul este deja rezervat in acel interval!");
        }




        long durationInMinutes = Duration.between(request.startTime(), request.endTime()).toMinutes();
        double durationInHours = (double)durationInMinutes / 60;
        double calculatePrice = durationInHours * field.getPricePerHour();




        Reservation reservation = new Reservation(
                request.startTime(),
                request.endTime(),
                calculatePrice,
                "CONFIRMED",
                client,
                field
        );

        Reservation savedReservation = reservationRepository.save(reservation);





        return new ReservationResponse(
                savedReservation.getId(),
                field.getId(),
                field.getName(),
                savedReservation.getStartTime(),
                savedReservation.getEndTime(),
                savedReservation.getTotalPrice(),
                savedReservation.getStatus(),
                client.getFirstName() + " " + client.getLastName()
        );


    }

    public List<ReservationResponse> getClientHistory(String email) {
        return reservationRepository.findAllByUserEmailOrderByStartTimeDesc(email)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    public List<ReservationResponse> getOwnerHistory(String email) {
        // 1. Găsim utilizatorul care a făcut cererea (pentru a-i afla rolul)
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost găsit!"));

        // 2. Verificăm dacă este ADMIN
        if (currentUser.getRole().name().equals("ROLE_ADMIN")) {
            // ADMIN-ul vede tot
            return reservationRepository.findAllByOrderByStartTimeDesc()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        } else {
            // PARTENERUL vede doar rezervările de pe terenurile lui
            return reservationRepository.findAllByFieldOwnerEmailOrderByStartTimeDesc(email)
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }
    }




    private ReservationResponse mapToResponse(Reservation reservation) {
        return new ReservationResponse(
          reservation.getId(),
          reservation.getField().getId(),
          reservation.getField().getName(),
          reservation.getStartTime(),
          reservation.getEndTime(),
          reservation.getTotalPrice(),
          reservation.getStatus(),
          reservation.getUser().getFirstName() + " " + reservation.getUser().getLastName()
        );
    }



    public String cancelReservation(Long reservationId, String currentUserEmail) {
        // 1. Căutăm rezervarea
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Rezervarea nu a fost gasita!"));

        // Dacă e deja anulată, oprim procesul ca să nu suprascriem aiurea
        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new RuntimeException("Această rezervare este deja anulată!");
        }

        // 2. Căutăm utilizatorul care face cererea
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost gasit!"));

        // 3. Verificăm CINE este utilizatorul (Drepturile)
        boolean isAdmin = currentUser.getRole().name().equals("ROLE_ADMIN");

        boolean isPartnerOwner = currentUser.getRole().name().equals("ROLE_PARTNER") &&
                reservation.getField().getOwner().getEmail().equals(currentUserEmail);

        // Verificăm dacă utilizatorul curent este cel care a creat rezervarea (indiferent de rol)
        boolean isAuthor = reservation.getUser().getEmail().equals(currentUserEmail);

        // 4. Aplicăm regulile de business și schimbăm statusul
        if (isAdmin || isPartnerOwner) {
            // ADMINUL și Proprietarul terenului pot anula oricând, fără limită de timp
            reservation.setStatus("CANCELLED");
            reservationRepository.save(reservation);
            return "Rezervarea a fost anulată cu succes. (Acțiune realizată de: " + currentUser.getRole().name() + ")";

        } else if (isAuthor) {
            // AUTORUL (cel care a făcut-o) trebuie să respecte regula de 24 de ore
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime matchStartTime = reservation.getStartTime();

            long hoursUntilMatch = Duration.between(now, matchStartTime).toHours();

            if (hoursUntilMatch < 24) {
                throw new RuntimeException("Prea târziu! Poți anula o rezervare doar cu minim 24 de ore înainte de meci.");
            }

            reservation.setStatus("CANCELLED");
            reservationRepository.save(reservation);
            return "Rezervarea ta a fost anulată cu succes.";

        } else {
            // Dacă nu ești nici Admin, nici proprietarul terenului, nici cel care a făcut rezervarea
            throw new RuntimeException("Nu ai permisiunea de a anula această rezervare!");
        }
    }
    public List<ReservationResponse> getOccupiedSlots(Long fieldId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay(); // Ex: 2026-05-22 00:00
        LocalDateTime endOfDay = date.atTime(23, 59, 59); // Ex: 2026-05-22 23:59

        return reservationRepository.findConfirmedReservationsForFieldAndDate(fieldId, startOfDay, endOfDay)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AnalyticsResponse getAnalytics(String email, LocalDate date) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost găsit!"));

        boolean isAdmin = currentUser.getRole().name().equals("ROLE_ADMIN");
        List<Reservation> allReservations;

        // 1. Extragem datele brute în funcție de Rol
        if (isAdmin) {
            // Adminul primește absolut toate rezervările din platformă
            allReservations = reservationRepository.findAll();
        } else {
            // Partenerul primește DOAR rezervările de la terenurile lui
            // Refolosim metoda ta din repository!
            allReservations = reservationRepository.findAllByFieldOwnerEmailOrderByStartTimeDesc(email);
        }

        // 2. Filtrăm după o ZI anume (dacă a fost cerută în URL)
        if (date != null) {
            allReservations = allReservations.stream()
                    .filter(r -> r.getStartTime().toLocalDate().equals(date))
                    .toList();
        }

        // 3. Păstrăm doar rezervările valide (Excludem "CANCELLED")
        List<Reservation> validReservations = allReservations.stream()
                .filter(r -> !r.getStatus().equals("CANCELLED"))
                .toList();

        // 4. Calculăm cifrele
        long totalCount = validReservations.size();
        double totalRevenue = validReservations.stream()
                .mapToDouble(Reservation::getTotalPrice)
                .sum();

        return new AnalyticsResponse(totalCount, totalRevenue);
    }
}
