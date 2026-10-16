package com.janapost.address.parser;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SlotExtractor {

    public static class ExtractionResult {
        public String floor;
        public String entrance;
        public String apartment;
        public String house;
        public String landmark;
        public String cleanedText;
    }

    // Floor patterns
    private static final Pattern FLOOR_PREFIX = Pattern.compile("(?i)(?U)(?:^|[,\\s])(?:этаж|эт|floor)\\s*[:#№]?\\s*(\\d+)(?=[,\\s]|$)");
    private static final Pattern FLOOR_SUFFIX = Pattern.compile("(?i)(?U)(?:^|[,\\s])(\\d+)\\s*(?:этаж|эт|floor)(?=[,\\s]|$)");

    // Entrance patterns
    private static final Pattern ENTRANCE_PREFIX = Pattern.compile("(?i)(?U)(?:^|[,\\s])(?:подъезд|подьезд|п-д|вход|entrance)\\s*[:#№]?\\s*(\\d+)(?=[,\\s]|$)");
    private static final Pattern ENTRANCE_SUFFIX = Pattern.compile("(?i)(?U)(?:^|[,\\s])(\\d+)\\s*(?:подъезд|подьезд|п-д|вход|entrance)(?=[,\\s]|$)");

    // Apartment patterns
    private static final Pattern APT_PREFIX = Pattern.compile("(?i)(?U)(?:^|[,\\s])(?:квартира|кв|flat|apt|комната|ком)\\s*[.\\-:#№]?\\s*(\\d+[а-яa-z]?)(?=[,\\s]|$)");
    private static final Pattern APT_SUFFIX = Pattern.compile("(?i)(?U)(?:^|[,\\s])(\\d+)\\s*(?:квартира|кв|flat)(?=[,\\s]|$)");

    // Landmark patterns: lookahead stops at detail markers
    private static final Pattern LANDMARK_KEYWORD = Pattern.compile("(?i)(?U)(?:^|[,\\s])((?:возле|рядом с|около|напротив|ориентир|ор-р|магазин|супермаркет|трц|тц|тд|бц|рынок|аптека|кафе|qazpost|казпочта|мечеть|парк)\\s+[\\p{L}0-9\\s\"'-]+?)(?=[,;]|\\b(?:дом|д|кв|квартира|подъезд|подьезд|этаж|эт|floor|flat)\\b|\\d+\\s*(?:подъезд|подьезд|этаж|эт|дом|кв)|$)");

    // House explicit pattern
    private static final Pattern HOUSE_EXPLICIT = Pattern.compile("(?i)(?U)(?:^|[,\\s])(?:дом|д|house)\\s*[.\\-:#№]?\\s*(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)(?=[,\\s]|$)");

    // Standalone house pattern
    private static final Pattern HOUSE_STANDALONE = Pattern.compile("(?i)(?U)(?:^|[,\\s])(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)(?=[,\\s]|$)");

    // Cleanup patterns
    private static final Pattern CLEAN_SPACES = Pattern.compile("\\s+");
    private static final Pattern CLEAN_COMMAS = Pattern.compile("\\s*,\\s*");
    private static final Pattern CLEAN_EDGES = Pattern.compile("^[,\\s]+|[,\\s]+$");

    public ExtractionResult extract(String text) {
        ExtractionResult res = new ExtractionResult();
        String current = text;

        // 1. Extract Floor first
        Matcher floorMatcher = FLOOR_PREFIX.matcher(current);
        if (floorMatcher.find()) {
            res.floor = floorMatcher.group(1).trim();
            current = current.substring(0, floorMatcher.start()) + " " + current.substring(floorMatcher.end());
        } else {
            Matcher floorSuffixMatcher = FLOOR_SUFFIX.matcher(current);
            if (floorSuffixMatcher.find()) {
                res.floor = floorSuffixMatcher.group(1).trim();
                current = current.substring(0, floorSuffixMatcher.start()) + " " + current.substring(floorSuffixMatcher.end());
            }
        }

        // 2. Extract Entrance
        Matcher entMatcher = ENTRANCE_PREFIX.matcher(current);
        if (entMatcher.find()) {
            res.entrance = entMatcher.group(1).trim();
            current = current.substring(0, entMatcher.start()) + " " + current.substring(entMatcher.end());
        } else {
            Matcher entSuffixMatcher = ENTRANCE_SUFFIX.matcher(current);
            if (entSuffixMatcher.find()) {
                res.entrance = entSuffixMatcher.group(1).trim();
                current = current.substring(0, entSuffixMatcher.start()) + " " + current.substring(entSuffixMatcher.end());
            }
        }

        // 3. Extract Apartment
        Matcher aptMatcher = APT_PREFIX.matcher(current);
        if (aptMatcher.find()) {
            res.apartment = aptMatcher.group(1).trim();
            current = current.substring(0, aptMatcher.start()) + " " + current.substring(aptMatcher.end());
        } else {
            Matcher aptSuffixMatcher = APT_SUFFIX.matcher(current);
            if (aptSuffixMatcher.find()) {
                res.apartment = aptSuffixMatcher.group(1).trim();
                current = current.substring(0, aptSuffixMatcher.start()) + " " + current.substring(aptSuffixMatcher.end());
            }
        }

        // 4. Extract Landmark
        Matcher lmMatcher = LANDMARK_KEYWORD.matcher(current);
        if (lmMatcher.find()) {
            res.landmark = lmMatcher.group(1).trim();
            current = current.substring(0, lmMatcher.start(1)) + " " + current.substring(lmMatcher.end(1));
        }

        // 5. Extract Explicit House ("дом 59", "д. 55/6", "дом 12а")
        Matcher houseMatcher = HOUSE_EXPLICIT.matcher(current);
        if (houseMatcher.find()) {
            res.house = houseMatcher.group(1).trim();
            current = current.substring(0, houseMatcher.start()) + " " + current.substring(houseMatcher.end());
        }

        // Preserve commas cleanly for segment identification
        String cleaned = CLEAN_SPACES.matcher(current).replaceAll(" ");
        cleaned = CLEAN_COMMAS.matcher(cleaned).replaceAll(", ");
        cleaned = CLEAN_EDGES.matcher(cleaned).replaceAll("");
        res.cleanedText = cleaned.trim();
        return res;
    }

    public String extractRemainingHouse(String text) {
        if (text == null || text.isBlank()) return null;
        Matcher m = HOUSE_STANDALONE.matcher(text);
        String lastFound = null;
        while (m.find()) {
            lastFound = m.group(1).trim();
        }
        return lastFound;
    }
}
