package com.SportConnect.demo.repository;

import com.SportConnect.demo.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByFieldId(Long fieldId);


    @Query("SELECT COUNT(r)>0 FROM Reservation r WHERE r.field.id = :fieldId " +
    "AND r.status = 'CONFIRMED'" +
    "AND (:start<r.endTime AND :end > r.startTime)")
    boolean isFieldOccupied(@Param("fieldId") Long fieldId,
                            @Param("start") LocalDateTime start,
                            @Param("end") LocalDateTime end);
}
