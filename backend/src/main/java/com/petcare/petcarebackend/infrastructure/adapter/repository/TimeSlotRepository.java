package com.petcare.petcarebackend.infrastructure.adapter.repository;

import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.SlotStatus;
import com.petcare.petcarebackend.infrastructure.entity.TimeSlotJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlotJpa, UUID> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    java.util.Optional<TimeSlotJpa> findById(UUID id);

    // Conditional update: the database decides the winner when several requests target one slot
    @Modifying(clearAutomatically = true)
    @Query("update TimeSlotJpa s set s.status = :newStatus where s.id = :id and s.status = :currentStatus")
    int updateStatusIfCurrent(@Param("id") UUID id,
                              @Param("currentStatus") SlotStatus currentStatus,
                              @Param("newStatus") SlotStatus newStatus);

    // Adjacent slots do not overlap: 10:00-11:00 and 11:00-12:00 are both allowed
    @Query("""
            select count(s) > 0 from TimeSlotJpa s
            where s.offering.provider.id = :providerId
              and s.offering.type = :type
              and s.slotDate = :date
              and s.startTime < :endTime
              and s.endTime > :startTime
            """)
    boolean existsOverlappingForProviderAndType(@Param("providerId") UUID providerId,
                                                @Param("type") OfferingType type,
                                                @Param("date") LocalDate date,
                                                @Param("startTime") LocalTime startTime,
                                                @Param("endTime") LocalTime endTime);

    @Query("""
            select s from TimeSlotJpa s
            where s.offering.id = :offeringId
              and s.status = :status
              and (s.slotDate > :date or (s.slotDate = :date and s.startTime > :time))
            order by s.slotDate, s.startTime
            """)
    List<TimeSlotJpa> findByOfferingAndStatusStartingAfter(@Param("offeringId") UUID offeringId,
                                                           @Param("status") SlotStatus status,
                                                           @Param("date") LocalDate date,
                                                           @Param("time") LocalTime time);
}
