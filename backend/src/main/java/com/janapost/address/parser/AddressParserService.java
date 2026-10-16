package com.janapost.address.parser;

import com.janapost.address.dto.AddressInput;
import com.janapost.address.dto.AddressOutput;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressParserService {

    private final TextNormalizer normalizer;
    private final SlotExtractor slotExtractor;
    private final GeoMatcher geoMatcher;
    private final ConfidenceScorer confidenceScorer;

    public AddressParserService(
        TextNormalizer normalizer,
        SlotExtractor slotExtractor,
        GeoMatcher geoMatcher,
        ConfidenceScorer confidenceScorer
    ) {
        this.normalizer = normalizer;
        this.slotExtractor = slotExtractor;
        this.geoMatcher = geoMatcher;
        this.confidenceScorer = confidenceScorer;
    }

    public AddressOutput parse(AddressInput input) {
        if (input == null) return null;
        String id = input.id();
        String raw = input.raw_address();

        if (raw == null || raw.isBlank()) {
            return AddressOutput.builder()
                .id(id)
                .rawAddress(raw)
                .confidence(0.0)
                .build();
        }

        // 1. Normalize
        String normalized = normalizer.normalize(raw);

        // 2. Extract detail slots (apt, floor, entrance, explicit house, landmark)
        SlotExtractor.ExtractionResult slots = slotExtractor.extract(normalized);

        // 3. Match geographic and street elements
        GeoMatcher.GeoResult geo = geoMatcher.match(raw, normalized, slots);

        // 4. Combine house numbers
        String house = slots.house != null ? slots.house : geo.house;

        // 5. Calculate confidence
        double confidence = confidenceScorer.score(
            geo.province,
            geo.city,
            geo.district,
            geo.streetName,
            geo.streetType,
            house,
            slots.apartment
        );

        return AddressOutput.builder()
            .id(id)
            .rawAddress(raw)
            .province(geo.province)
            .city(geo.city)
            .district(geo.district)
            .livingArea(geo.livingArea)
            .streetType(geo.streetType)
            .streetName(geo.streetName)
            .house(house)
            .apartment(slots.apartment)
            .entrance(slots.entrance)
            .floor(slots.floor)
            .landmark(slots.landmark)
            .confidence(confidence)
            .build();
    }

    public List<AddressOutput> parseBatch(List<AddressInput> inputs) {
        if (inputs == null) return List.of();
        // Parallel stream for blazing fast multi-core execution
        return inputs.parallelStream()
            .map(this::parse)
            .toList();
    }
}
