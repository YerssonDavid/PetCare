package com.petcare.petcarebackend.infrastructure.adapter.repository;

import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.RequestStatus;
import com.petcare.petcarebackend.infrastructure.entity.ServiceRequestJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.UUID;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequestJpa, UUID> {

    @Query("""
            select count(r) > 0 from ServiceRequestJpa r
            where r.pet.id = :petId
              and r.status in :statuses
              and r.offering.type = :type
              and r.slot.slotDate = :date
              and r.slot.startTime < :endTime
              and r.slot.endTime > :startTime
            """)
    boolean existsOverlapping(@Param("petId") UUID petId,
                              @Param("statuses") Collection<RequestStatus> statuses,
                              @Param("type") OfferingType type,
                              @Param("date") LocalDate date,
                              @Param("startTime") LocalTime startTime,
                              @Param("endTime") LocalTime endTime);
}
