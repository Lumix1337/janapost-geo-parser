package com.janapost.address.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;

@Repository
public class KatoRepository {

    public record CityInfo(String name, String province, String defaultDistrict) {}
    public record StreetInfo(String district, String streetType) {}

    private final List<String> provinces = new ArrayList<>();
    private final Map<String, String> provinceAliases = new HashMap<>();
    private final Map<String, CityInfo> cities = new HashMap<>();
    private final List<Map.Entry<String, CityInfo>> sortedCitiesByLength = new ArrayList<>();
    private final Map<String, List<String>> cityDistricts = new HashMap<>();
    private final Map<String, Map<String, StreetInfo>> streetDistricts = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("data/kazakhstan_geo.json");
            try (InputStream is = resource.getInputStream()) {
                JsonNode root = mapper.readTree(is);

                // Provinces
                JsonNode provNode = root.get("provinces");
                if (provNode != null && provNode.isArray()) {
                    for (JsonNode p : provNode) {
                        provinces.add(p.asText());
                    }
                }

                // Province aliases
                JsonNode aliasNode = root.get("province_aliases");
                if (aliasNode != null) {
                    aliasNode.fields().forEachRemaining(entry -> 
                        provinceAliases.put(entry.getKey().toLowerCase(), entry.getValue().asText())
                    );
                }

                // Cities
                JsonNode citiesNode = root.get("cities");
                if (citiesNode != null) {
                    citiesNode.fields().forEachRemaining(entry -> {
                        String cityName = entry.getKey();
                        JsonNode val = entry.getValue();
                        String province = val.has("province") && !val.get("province").isNull() ? val.get("province").asText() : null;
                        String district = val.has("district") && !val.get("district").isNull() ? val.get("district").asText() : null;
                        cities.put(cityName.toLowerCase(), new CityInfo(cityName, province, district));
                    });
                }

                // City districts
                JsonNode cDistricts = root.get("city_districts");
                if (cDistricts != null) {
                    cDistricts.fields().forEachRemaining(entry -> {
                        List<String> dists = new ArrayList<>();
                        for (JsonNode d : entry.getValue()) {
                            dists.add(d.asText());
                        }
                        cityDistricts.put(entry.getKey(), dists);
                    });
                }

                // Street districts
                JsonNode sDistricts = root.get("street_districts");
                if (sDistricts != null) {
                    sDistricts.fields().forEachRemaining(cityEntry -> {
                        String cityName = cityEntry.getKey();
                        Map<String, StreetInfo> streetMap = new HashMap<>();
                        cityEntry.getValue().fields().forEachRemaining(streetEntry -> {
                            String streetName = streetEntry.getKey().toLowerCase();
                            JsonNode val = streetEntry.getValue();
                            String dist = val.has("district") && !val.get("district").isNull() ? val.get("district").asText() : null;
                            String sType = val.has("type") && !val.get("type").isNull() ? val.get("type").asText() : null;
                            streetMap.put(streetName, new StreetInfo(dist, sType));
                        });
                        streetDistricts.put(cityName, streetMap);
                    });
                }

                // Sort cities by length descending for instant early-exit matching
                sortedCitiesByLength.addAll(cities.entrySet());
                sortedCitiesByLength.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load kazakhstan_geo.json", e);
        }
    }

    public List<String> getProvinces() {
        return provinces;
    }

    public String resolveProvince(String raw) {
        if (raw == null) return null;
        String lower = raw.trim().toLowerCase();
        if (provinceAliases.containsKey(lower)) {
            return provinceAliases.get(lower);
        }
        for (String p : provinces) {
            if (lower.contains(p.toLowerCase())) {
                return p;
            }
        }
        return null;
    }

    public CityInfo findCity(String raw) {
        if (raw == null) return null;
        String lower = raw.trim().toLowerCase();

        // 1. Direct exact match (O(1))
        if (cities.containsKey(lower)) {
            return cities.get(lower);
        }

        // 2. Fast early-exit matching from longest to shortest
        for (Map.Entry<String, CityInfo> entry : sortedCitiesByLength) {
            String key = entry.getKey();
            int idx = lower.indexOf(key);
            if (idx >= 0) {
                boolean validBefore = (idx == 0) || !Character.isLetterOrDigit(lower.charAt(idx - 1));
                boolean validAfter = (idx + key.length() == lower.length()) || !Character.isLetterOrDigit(lower.charAt(idx + key.length()));
                if (validBefore && validAfter) {
                    return entry.getValue(); // Since it's sorted by length descending, first valid match is the longest!
                }
            }
        }
        return null;
    }

    public String findDistrictInText(String text, String cityName) {
        if (text == null) return null;
        String lower = text.toLowerCase();

        // Check city-specific districts
        if (cityName != null && cityDistricts.containsKey(cityName)) {
            for (String d : cityDistricts.get(cityName)) {
                String dLower = d.toLowerCase();
                // Avoid matching "Алматы" as district when city is "Алматы"
                if (cityName.equalsIgnoreCase("Алматы") && dLower.equals("алматы")) continue;
                String root = dLower.length() > 5 ? dLower.substring(0, dLower.length() - 2) : dLower;
                if (lower.contains(root)) {
                    return d;
                }
            }
            return null;
        }

        // Check all districts if city is unknown
        for (Map.Entry<String, List<String>> entry : cityDistricts.entrySet()) {
            for (String d : entry.getValue()) {
                String dLower = d.toLowerCase();
                if (dLower.equals("алматы")) continue;
                String root = dLower.length() > 5 ? dLower.substring(0, dLower.length() - 2) : dLower;
                if (lower.contains(root)) {
                    return d;
                }
            }
        }
        return null;
    }

    public StreetInfo lookupStreet(String cityName, String streetName) {
        if (cityName == null || streetName == null) return null;
        Map<String, StreetInfo> streetMap = streetDistricts.get(cityName);
        if (streetMap == null) return null;

        String lower = streetName.toLowerCase().trim();
        if (streetMap.containsKey(lower)) {
            return streetMap.get(lower);
        }

        // Fuzzy/partial match
        for (Map.Entry<String, StreetInfo> entry : streetMap.entrySet()) {
            if (lower.contains(entry.getKey()) || entry.getKey().contains(lower)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
