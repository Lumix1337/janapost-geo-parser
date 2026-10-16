package com.janapost.address.parser;

import com.janapost.address.geo.KatoRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GeoMatcher {

    private final KatoRepository katoRepository;
    private final ConcurrentHashMap<String, Pattern> cityRemovalPatterns = new ConcurrentHashMap<>();

    public GeoMatcher(KatoRepository katoRepository) {
        this.katoRepository = katoRepository;
    }

    public static class GeoResult {
        public String province;
        public String city;
        public String district;
        public String livingArea;
        public String streetType;
        public String streetName;
        public String house;
        public boolean districtInString = false;
    }

    // Pre-compiled static regex patterns (Zero-allocation in hot path)
    private static final Pattern DISTRICT_PATTERN = Pattern.compile("(?i)(?U)(?:^|[,\\s])([\\p{L}]+(?:ский|ской|ский|ның| ауданы)?)\\s+(?:район|р-н|ауданы)(?=[,\\s]|$)");
    private static final Pattern DISTRICT_PREFIX_PATTERN = Pattern.compile("(?i)(?U)(?:^|[,\\s])(?:район|р-н|ауданы)\\s+([\\p{L}]+)(?=[,\\s]|$)");
    private static final Pattern DISTRICT_CLEANUP_PATTERN = Pattern.compile("(?i)(?U)\\s*(?:район|р-н|ауданы)$");
    private static final Pattern LIVING_AREA_PREFIX = Pattern.compile("(?i)(?U)(?:^|[,\\s])(?:жк|жилой комплекс|массив|жилой массив)\\s+([\\p{L}0-9\\s\"'-]+?)(?=[,/]|$)");

    // Cleanup patterns
    private static final Pattern COMMA_SEMICOLON_PATTERN = Pattern.compile("[,;]+");
    private static final Pattern MULTI_SPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern EDGE_CLEANUP_PATTERN = Pattern.compile("^[,\\s]+|[,\\s]+$");

    // Segment patterns
    private static final Pattern HOUSE_SEGMENT_PATTERN = Pattern.compile("^\\d+(?:[/\\\\]\\d+)?[а-яa-z]?$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern MKR_SEG_PATTERN = Pattern.compile("(?i)(?U)^(?:мкр|микрорайон|м-н|шағын ауданы)\\.?\\s*(.+)$");
    private static final Pattern UL_SEG_PATTERN = Pattern.compile("(?i)(?U)^(?:улица|ул|көшесі)\\.?\\s*(.+)$");
    private static final Pattern PR_SEG_PATTERN = Pattern.compile("(?i)(?U)^(?:проспект|пр|просп|даңғылы)\\.?\\s*(.+)$");
    private static final Pattern PER_SEG_PATTERN = Pattern.compile("(?i)(?U)^(.+?)\\s+(?:переулок|пер)\\.?$");
    private static final Pattern KV_SEG_PATTERN = Pattern.compile("(?i)(?U)^(.+?)\\s+(?:квартал|кв-л)\\.?$");

    // Fallback full patterns
    private static final Pattern MKR_FULL_PATTERN = Pattern.compile("(?i)(?U)(?:^|[,\\s])(?:мкр|микрорайон|м-н|шағын ауданы)\\.?\\s+([\\p{L}0-9\\s-]+?)(?=[,\\s]+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)|$)");
    private static final Pattern MKR_PRE_PATTERN = Pattern.compile("(?i)(?U)(?:^|[,\\s])([\\p{L}0-9-]+)\\s+(?:мкр|микрорайон|м-н)\\b(?:[,\\s]+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?))?");
    private static final Pattern UL_PATTERN = Pattern.compile("(?i)(?U)(?:^|[,\\s])(?:улица|ул|көшесі)\\.?\\s*([\\p{L}0-9\\s-]+?)(?=[,\\s]+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)|$)");
    private static final Pattern PR_PATTERN = Pattern.compile("(?i)(?U)(?:^|[,\\s])(?:проспект|пр|просп|даңғылы)\\.?\\s*([\\p{L}0-9\\s-]+?)(?=[,\\s]+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)|$)");
    private static final Pattern PER_PATTERN = Pattern.compile("(?i)(?U)(?:^|[,\\s])([\\p{L}0-9\\s-]+?)\\s+(?:переулок|пер)\\.?(?=[,\\s]+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)|$)");
    private static final Pattern KV_PATTERN = Pattern.compile("(?i)(?U)(?:^|[,\\s])([\\p{L}0-9-]+)\\s+(?:квартал|кв-л)\\.?(?=[,\\s]+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)|$)");
    private static final Pattern GENERIC_PATTERN = Pattern.compile("(?i)(?U)^([\\p{L}\\s-]+?)(?:\\s+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?))?$");
    private static final Pattern NUM_PATTERN = Pattern.compile("(?i)(?U)(?:^|[,\\s])(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)(?=[,\\s]|$)");

    public GeoResult match(String rawAddress, String normalized, SlotExtractor.ExtractionResult slotResult) {
        GeoResult res = new GeoResult();
        String current = slotResult.cleanedText;

        // 1. Check for explicit district in the input string
        Matcher distMatcher = DISTRICT_PATTERN.matcher(current);
        if (distMatcher.find()) {
            String rawDist = distMatcher.group(1).trim();
            String cleanedDist = DISTRICT_CLEANUP_PATTERN.matcher(rawDist).replaceAll("").trim();
            if (!cleanedDist.isEmpty()) {
                res.district = capitalize(cleanedDist);
                res.districtInString = true;
                current = current.substring(0, distMatcher.start()) + " " + current.substring(distMatcher.end());
            }
        } else {
            Matcher distPreMatcher = DISTRICT_PREFIX_PATTERN.matcher(current);
            if (distPreMatcher.find()) {
                String cleanedDist = distPreMatcher.group(1).trim();
                if (!cleanedDist.isEmpty()) {
                    res.district = capitalize(cleanedDist);
                    res.districtInString = true;
                    current = current.substring(0, distPreMatcher.start()) + " " + current.substring(distPreMatcher.end());
                }
            }
        }

        // 2. Identify City and Province
        KatoRepository.CityInfo cityInfo = katoRepository.findCity(current);
        if (cityInfo == null) {
            cityInfo = katoRepository.findCity(rawAddress);
        }

        if (cityInfo != null) {
            res.city = cityInfo.name();
            res.province = cityInfo.province();
            if (res.district == null && cityInfo.defaultDistrict() != null) {
                res.district = cityInfo.defaultDistrict();
            }
            current = removeCityMention(current, cityInfo.name());
        } else {
            String prov = katoRepository.resolveProvince(rawAddress);
            if (prov != null) {
                res.province = prov;
            }
        }

        // 3. Check for explicit Living Area (ЖК / массив)
        Matcher laMatcher = LIVING_AREA_PREFIX.matcher(current);
        if (laMatcher.find()) {
            res.livingArea = laMatcher.group(1).trim();
            current = current.substring(0, laMatcher.start()) + " " + current.substring(laMatcher.end());
        }

        // 4. Street Type & Street Name extraction
        parseStreetAndHouse(rawAddress, current, res, slotResult);

        // 5. Enrich District and StreetType from catalog
        if (res.city != null && res.streetName != null) {
            KatoRepository.StreetInfo sInfo = katoRepository.lookupStreet(res.city, res.streetName);
            if (sInfo != null) {
                if (res.district == null && sInfo.district() != null) {
                    res.district = sInfo.district();
                }
                if (res.streetType == null && sInfo.streetType() != null) {
                    res.streetType = sInfo.streetType();
                }
            }
        }

        // Fallback: If district was mentioned in text directly from Kato
        if (res.district == null) {
            String foundDist = katoRepository.findDistrictInText(rawAddress, res.city);
            if (foundDist != null) {
                res.district = foundDist;
                res.districtInString = true;
            }
        }

        return res;
    }

    private void parseStreetAndHouse(String rawAddress, String current, GeoResult res, SlotExtractor.ExtractionResult slotResult) {
        String clean = current.trim();
        if (clean.isEmpty()) return;

        String detectedType = null;
        String detectedName = null;
        String detectedHouse = slotResult.house;

        // A. Segment-based extraction if commas exist
        if (clean.contains(",")) {
            String[] segments = clean.split(",");
            List<String> validSegs = new ArrayList<>(segments.length);
            for (String seg : segments) {
                String t = seg.trim();
                if (!t.isEmpty()) validSegs.add(t);
            }

            for (String seg : validSegs) {
                if (detectedHouse == null && HOUSE_SEGMENT_PATTERN.matcher(seg).matches()) {
                    detectedHouse = seg;
                    continue;
                }

                Matcher mm = MKR_SEG_PATTERN.matcher(seg);
                if (mm.find()) {
                    detectedType = "микрорайон";
                    detectedName = mm.group(1).trim();
                    continue;
                }

                Matcher um = UL_SEG_PATTERN.matcher(seg);
                if (um.find()) {
                    detectedType = "улица";
                    detectedName = um.group(1).trim();
                    continue;
                }

                Matcher pm = PR_SEG_PATTERN.matcher(seg);
                if (pm.find()) {
                    detectedType = "проспект";
                    detectedName = pm.group(1).trim();
                    continue;
                }

                Matcher perM = PER_SEG_PATTERN.matcher(seg);
                if (perM.find()) {
                    detectedType = "переулок";
                    detectedName = perM.group(1).trim();
                    continue;
                }

                Matcher kvM = KV_SEG_PATTERN.matcher(seg);
                if (kvM.find()) {
                    detectedType = "квартал";
                    detectedName = kvM.group(1).trim();
                    continue;
                }
            }
        }

        // B. Regex-based extraction if not resolved by commas
        String s = COMMA_SEMICOLON_PATTERN.matcher(clean).replaceAll(" ");
        s = MULTI_SPACE_PATTERN.matcher(s).replaceAll(" ").trim();

        if (detectedName == null) {
            Matcher mkrMatcher = MKR_FULL_PATTERN.matcher(s);
            if (mkrMatcher.find()) {
                detectedType = "микрорайон";
                detectedName = mkrMatcher.group(1).trim();
                if (detectedHouse == null && mkrMatcher.groupCount() >= 2 && mkrMatcher.group(2) != null) {
                    detectedHouse = mkrMatcher.group(2).trim();
                }
            }
        }

        if (detectedName == null) {
            Matcher preMatcher = MKR_PRE_PATTERN.matcher(s);
            if (preMatcher.find()) {
                detectedType = "микрорайон";
                detectedName = preMatcher.group(1).trim();
                if (detectedHouse == null && preMatcher.group(2) != null) {
                    detectedHouse = preMatcher.group(2).trim();
                }
            }
        }

        if (detectedName == null) {
            Matcher ulMatcher = UL_PATTERN.matcher(s);
            if (ulMatcher.find()) {
                detectedType = "улица";
                detectedName = ulMatcher.group(1).trim();
                if (detectedHouse == null && ulMatcher.groupCount() >= 2 && ulMatcher.group(2) != null) {
                    detectedHouse = ulMatcher.group(2).trim();
                }
            }
        }

        if (detectedName == null) {
            Matcher prMatcher = PR_PATTERN.matcher(s);
            if (prMatcher.find()) {
                detectedType = "проспект";
                detectedName = prMatcher.group(1).trim();
                if (detectedHouse == null && prMatcher.groupCount() >= 2 && prMatcher.group(2) != null) {
                    detectedHouse = prMatcher.group(2).trim();
                }
            }
        }

        if (detectedName == null) {
            Matcher perMatcher = PER_PATTERN.matcher(s);
            if (perMatcher.find()) {
                detectedType = "переулок";
                detectedName = perMatcher.group(1).trim();
                if (detectedHouse == null && perMatcher.groupCount() >= 2 && perMatcher.group(2) != null) {
                    detectedHouse = perMatcher.group(2).trim();
                }
            }
        }

        if (detectedName == null) {
            Matcher kvMatcher = KV_PATTERN.matcher(s);
            if (kvMatcher.find()) {
                detectedType = "квартал";
                detectedName = kvMatcher.group(1).trim();
                if (detectedHouse == null && kvMatcher.groupCount() >= 2 && kvMatcher.group(2) != null) {
                    detectedHouse = kvMatcher.group(2).trim();
                }
            }
        }

        if (detectedName == null) {
            Matcher genMatcher = GENERIC_PATTERN.matcher(s);
            if (genMatcher.find()) {
                detectedName = genMatcher.group(1).trim();
                if (detectedHouse == null && genMatcher.group(2) != null) {
                    detectedHouse = genMatcher.group(2).trim();
                }
            }
        }

        if (detectedHouse == null) {
            Matcher nm = NUM_PATTERN.matcher(s);
            while (nm.find()) {
                String candidate = nm.group(1).trim();
                if (detectedName == null || !detectedName.contains(candidate)) {
                    detectedHouse = candidate;
                }
            }
        }

        if (detectedName != null) {
            detectedName = findInRaw(rawAddress, detectedName);
        }

        res.streetType = detectedType;
        res.streetName = detectedName;
        res.house = detectedHouse;
    }

    private String removeCityMention(String text, String cityName) {
        Pattern pat = cityRemovalPatterns.computeIfAbsent(cityName, name -> 
            Pattern.compile("(?i)(?U)(?:город|г|қ|каласы|қаласы)?\\.?\\s*" + Pattern.quote(name) + "(?:\\s*(?:қаласы|каласы))?")
        );
        String s = pat.matcher(text).replaceAll("");
        return EDGE_CLEANUP_PATTERN.matcher(s).replaceAll("").trim();
    }

    private String findInRaw(String raw, String query) {
        if (raw == null || query == null || query.isBlank()) return query;
        int idx = raw.toLowerCase().indexOf(query.toLowerCase());
        if (idx >= 0 && idx + query.length() <= raw.length()) {
            return raw.substring(idx, idx + query.length());
        }
        return query;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }
}
