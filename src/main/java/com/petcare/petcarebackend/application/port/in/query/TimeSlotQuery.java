package com.petcare.petcarebackend.application.port.in.query;

import com.petcare.petcarebackend.application.dto.TimeSlotResponse;

import java.util.List;
import java.util.UUID;

public interface TimeSlotQuery {
    List<TimeSlotResponse> listFreeTimeSlots(UUID offeringId);
}
