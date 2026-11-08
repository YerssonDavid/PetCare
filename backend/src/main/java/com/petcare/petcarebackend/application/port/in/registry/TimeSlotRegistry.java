package com.petcare.petcarebackend.application.port.in.registry;

import com.petcare.petcarebackend.application.dto.CreateTimeSlotRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;

import java.util.UUID;

public interface TimeSlotRegistry {
    RegisterResponse createTimeSlot(UUID offeringId, CreateTimeSlotRequest request);
}
