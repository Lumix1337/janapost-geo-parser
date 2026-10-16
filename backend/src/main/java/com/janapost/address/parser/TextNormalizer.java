package com.janapost.address.parser;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TextNormalizer {

    private static final Pattern ZERO_WIDTH_PATTERN = Pattern.compile("[\u200B-\u200D\uFEFF\u200E\u200F\u00AD]");
    private static final Pattern MULTI_SPACE_PATTERN = Pattern.compile("[\\s\u00A0]+");
    
    // Split digit + word without space (e.g., "66квартал", "11дом", "4подьезд", "9этаж", "69квартира")
    private static final Pattern DIGIT_WORD_PATTERN = Pattern.compile("(?i)(?U)(\\d+)(квартал|дом|мкр|микрорайон|подьезд|подъезд|этаж|эт|кв|квартира|вход|блок)");
    
    // Split word + digit without space (e.g., "дом11", "подьезд4", "кв70", "flat191")
    private static final Pattern WORD_DIGIT_PATTERN = Pattern.compile("(?i)(?U)(дом|подьезд|подъезд|этаж|кв|квартира|flat|apt|под)(\\d+)");
    
    // Fix spaced dots like "ул .курильская" -> "ул. курильская"
    private static final Pattern SPACED_DOT_PATTERN = Pattern.compile("(?i)(?U)\\b(ул|мкр|пр|просп|пер|д|кв)\\s*\\.\\s*");

    // Common typo fixes
    private static final Pattern PODEZD_TYPO = Pattern.compile("(?i)(?U)\\bподьезд\\b");

    // Bilingual slash cleanup like "город Алматы/Алматы қаласы"
    private static final Pattern BILINGUAL_SLASH_PATTERN = Pattern.compile("(?i)(?U)([^,/]+)/([^,/]+(?:қаласы|облысы|ауданы|көшесі|шағын ауданы))");

    public String cleanInvisible(String text) {
        if (text == null) return "";
        String s = ZERO_WIDTH_PATTERN.matcher(text).replaceAll("");
        s = MULTI_SPACE_PATTERN.matcher(s).replaceAll(" ");
        return s.trim();
    }

    public String normalize(String text) {
        if (text == null || text.isBlank()) return "";
        
        String s = cleanInvisible(text);

        // Handle bilingual slashes: replace e.g. "город Алматы/Алматы қаласы" with "город Алматы"
        Matcher slashMatcher = BILINGUAL_SLASH_PATTERN.matcher(s);
        if (slashMatcher.find()) {
            s = slashMatcher.replaceAll("$1");
        }

        // Fix spaced dots: "ул . " -> "ул. "
        s = SPACED_DOT_PATTERN.matcher(s).replaceAll("$1. ");

        // Normalize soft sign in "подьезд" -> "подъезд"
        s = PODEZD_TYPO.matcher(s).replaceAll("подъезд");

        // Separate attached numbers and words
        s = DIGIT_WORD_PATTERN.matcher(s).replaceAll("$1 $2");
        s = WORD_DIGIT_PATTERN.matcher(s).replaceAll("$1 $2");

        // Clean double spaces again
        s = MULTI_SPACE_PATTERN.matcher(s).replaceAll(" ");

        return s.trim();
    }
}
