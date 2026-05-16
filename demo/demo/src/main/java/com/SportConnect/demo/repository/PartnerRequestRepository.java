package com.SportConnect.demo.repository;

import com.SportConnect.demo.model.PartnerRequest;
import com.SportConnect.demo.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerRequestRepository extends JpaRepository<PartnerRequest, Long> {
    List<PartnerRequest> findByStatus(RequestStatus status);
    boolean existsByUserEmailAndStatus(String email, RequestStatus status);
}
