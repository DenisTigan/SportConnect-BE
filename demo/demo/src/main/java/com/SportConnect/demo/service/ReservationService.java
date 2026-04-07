package com.SportConnect.demo.service;


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
import java.time.LocalDateTime;
import java.time.LocalTime;

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
}
