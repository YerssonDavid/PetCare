package com.petcare.petcarebackend.infrastructure.adapter.repository;

import com.petcare.petcarebackend.infrastructure.entity.PetJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

import java.util.UUID;

@Repository
public interface PetRepository extends JpaRepository<PetJpa, UUID> {
    boolean existsByOwnerIdAndNameNormalized(UUID ownerId, String nameNormalized);
    boolean deleteByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("select p from PetJpa p where p.owner.id = :ownerId")
    List<PetJpa> findByOwnerId(@Param("ownerId") UUID ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PetJpa p where p.id = :id")
    Optional<PetJpa> findByIdForUpdate(@Param("id") UUID id);
}
