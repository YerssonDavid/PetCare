package com.petcare.petcarebackend.domain.entity;

import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.valueobject.OfferingName;
import com.petcare.petcarebackend.domain.valueobject.Price;

import java.util.UUID;

public class Offering {
    private UUID id;
    private UUID providerId;
    private OfferingName name;
    private String description;
    private String conditions;
    private Price price;
    private OfferingCategory category;
    private OfferingType type;
    private String imageUrl;

    public Offering() {
    }

    public Offering(UUID id, UUID providerId, OfferingName name, String description, String conditions,
                    Price price, OfferingCategory category, OfferingType type, String imageUrl) {
        this.id = id;
        this.providerId = providerId;
        this.name = name;
        this.description = description;
        this.conditions = conditions;
        this.price = price;
        this.category = category;
        this.type = type;
        this.imageUrl = imageUrl;
    }

    // factory
    public static Offering publish(UUID providerId, OfferingName name, String description, String conditions,
                                   Price price, OfferingCategory category, OfferingType type) {
        return new Offering(UUID.randomUUID(), providerId, name, description, conditions,
                price, category, type, null);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }

    public OfferingName getName() {
        return name;
    }

    public void setName(OfferingName name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public OfferingCategory getCategory() {
        return category;
    }

    public void setCategory(OfferingCategory category) {
        this.category = category;
    }

    public OfferingType getType() {
        return type;
    }

    public void setType(OfferingType type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
