package com.petcare.petcarebackend.application.dto;

import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.entity.Owner;
import com.petcare.petcarebackend.domain.entity.Pet;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.entity.ServiceRequest;
import com.petcare.petcarebackend.domain.entity.TimeSlot;

// A request together with everything needed to answer it or show it, loaded in a single query
public record ServiceRequestDetails(
        ServiceRequest request,
        Owner owner,
        Pet pet,
        Offering offering,
        Provider provider,
        TimeSlot slot
) {
}
