package com.petcare.petcarebackend.infrastructure.adapter.repository;

import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.RequestStatus;
import com.petcare.petcarebackend.infrastructure.entity.ServiceRequestJpa;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequestJpa, UUID> {

    boolean existsByPetIdAndSlotIdAndStatusIn(UUID petId, UUID slotId, Collection<RequestStatus> statuses);

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

    @EntityGraph(attributePaths = {"owner", "pet", "offering", "provider", "slot"})
    Optional<ServiceRequestJpa> findDetailsById(UUID id);

    @EntityGraph(attributePaths = {"owner", "pet", "offering", "provider", "slot"})
    List<ServiceRequestJpa> findByProviderIdAndStatusInOrderByRequestedAtDesc(UUID providerId, Collection<RequestStatus> statuses);

    @EntityGraph(attributePaths = {"owner", "pet", "offering", "provider", "slot"})
    @Query("""
            select r from ServiceRequestJpa r
            where r.owner.id = :ownerId
            order by r.slot.slotDate desc, r.slot.startTime desc, r.requestedAt desc
            """)
    List<ServiceRequestJpa> findByOwnerIdOrderBySlotDesc(@Param("ownerId") UUID ownerId);

    @EntityGraph(attributePaths = {"owner", "pet", "offering", "provider", "slot"})
    @Query("""
            select r from ServiceRequestJpa r
            where r.provider.id = :providerId
              and r.status = :status
              and r.offering.type = :type
              and (r.slot.slotDate < :date or (r.slot.slotDate = :date and r.slot.startTime <= :time))
            order by r.slot.slotDate desc, r.slot.startTime desc
            """)
    List<ServiceRequestJpa> findByProviderStatusAndTypeStartedBy(@Param("providerId") UUID providerId,
                                                                 @Param("status") RequestStatus status,
                                                                 @Param("type") OfferingType type,
                                                                 @Param("date") LocalDate date,
                                                                 @Param("time") LocalTime time);

    // Conditional update: the database decides the winner when several answers target one request
    @Modifying(clearAutomatically = true)
    @Query("""
            update ServiceRequestJpa r
            set r.status = :newStatus, r.rejectionReason = :rejectionReason
            where r.id = :id and r.status = :currentStatus
            """)
    int updateAnswerIfCurrent(@Param("id") UUID id,
                              @Param("currentStatus") RequestStatus currentStatus,
                              @Param("newStatus") RequestStatus newStatus,
                              @Param("rejectionReason") String rejectionReason);
}
