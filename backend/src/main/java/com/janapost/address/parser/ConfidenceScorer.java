package com.janapost.address.parser;

import com.janapost.address.dto.AddressOutput;
import org.springframework.stereotype.Component;

@Component
public class ConfidenceScorer {

    public double calculate(AddressOutput.Builder b, boolean districtInCatalogOrString) {
        double score = 0.10;

        // Key address components (60% weight in hackathon criteria)
        boolean hasCity = b != null; // check individual fields in service
        return score;
    }

    public double score(
        String province,
        String city,
        String district,
        String streetName,
        String streetType,
        String house,
        String apartment
    ) {
        double score = 0.15;

        if (city != null && !city.isBlank()) score += 0.25;
        if (province != null && !province.isBlank()) score += 0.10;
        if (streetName != null && !streetName.isBlank()) score += 0.20;
        if (house != null && !house.isBlank()) score += 0.15;
        if (apartment != null && !apartment.isBlank()) score += 0.06;
        if (district != null && !district.isBlank()) score += 0.05;
        if (streetType != null && !streetType.isBlank()) score += 0.05;

        // Cap at 0.98 max
        return Math.min(0.98, Math.max(0.10, score));
    }
}
