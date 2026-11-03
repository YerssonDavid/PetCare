package com.petcare.petcarebackend.infrastructure.adapter.repository;

import com.petcare.petcarebackend.infrastructure.entity.PetJpa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PetRepository extends JpaRepository<PetJpa, UUID> {
    boolean existsByOwnerIdAndNameNormalized(UUID ownerId, String nameNormalized);
    List<PetJpa> findByOwnerId(UUID ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PetJpa p where p.id = :id")
    Optional<PetJpa> findByIdForUpdate(@Param("id") UUID id);
}
