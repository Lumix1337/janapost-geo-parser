package com.example.myapplication.data

import android.util.Log

data class ParsedDeliveryAddress(
    val rawAddress: String,
    val province: String? = null,
    val city: String? = null,
    val district: String? = null,
    val streetType: String? = null,
    val streetName: String? = null,
    val house: String? = null,
    val apartment: String? = null,
    val entrance: String? = null,
    val floor: String? = null,
    val landmark: String? = null,
    val confidence: Double = 0.0,
)

object SmartAddressParser {

    private const val TAG = "SmartAddressParser"

    // Pre-compiled static Regex constants (Zero GC overhead on mobile UI thread)
    private val RE_ZERO_WIDTH = Regex("[\u200B-\u200D\uFEFF\u00AD]")
    private val RE_BILINGUAL = Regex("(?i)([^,/]+)/([^,/]+(?:қаласы|каласы|облысы|ауданы))")
    private val RE_SPACED_DOT = Regex("(?i)(ул|мкр|пр|просп|пер|д|кв)\\s*\\.\\s*")
    private val RE_PODEZD = Regex("(?i)подьезд")
    private val RE_DIGIT_WORD = Regex("(?i)(\\d+)(квартал|дом|мкр|микрорайон|подъезд|этаж|эт|кв|квартира)")
    private val RE_WORD_DIGIT = Regex("(?i)(дом|подъезд|этаж|кв|квартира|flat|apt)(\\d+)")
    private val RE_SPACES = Regex("\\s+")

    private val RE_FL_PREFIX = Regex("(?i)(?:^|[,\\s])(?:этаж|эт|floor)\\s*[:#№]?\\s*(\\d+)")
    private val RE_FL_SUFFIX = Regex("(?i)(?:^|[,\\s])(\\d+)\\s*(?:этаж|эт|floor)")
    private val RE_ENT_PREFIX = Regex("(?i)(?:^|[,\\s])(?:подъезд|п-д|вход|entrance)\\s*[:#№]?\\s*(\\d+)")
    private val RE_ENT_SUFFIX = Regex("(?i)(?:^|[,\\s])(\\d+)\\s*(?:подъезд|п-д|вход)")
    private val RE_APT_PREFIX = Regex("(?i)(?:^|[,\\s])(?:квартира|кв|flat|apt|комната)\\s*[.\\-:#№]?\\s*(\\d+[а-яa-z]?)")
    private val RE_APT_SUFFIX = Regex("(?i)(?:^|[,\\s])(\\d+)\\s*(?:квартира|кв|flat)")
    private val RE_LANDMARK = Regex("(?i)(?:^|[,\\s])((?:магазин|qazpost|трц|тц|аптека|кафе|возле|рядом с|около)\\s+[^,;]+?)(?=[,;]|\\b(?:дом|кв|подъезд|этаж)\\b|$)")
    private val RE_HOUSE_EXPLICIT = Regex("(?i)(?:^|[,\\s])(?:дом|д|house)\\s*[.\\-:#№]?\\s*(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)")
    private val RE_HOUSE_NUM = Regex("^\\d+(?:[/\\\\]\\d+)?[а-яa-z]?$", RegexOption.IGNORE_CASE)

    private val RE_MKR_SEG = Regex("(?i)^(?:мкр|микрорайон|м-н)\\.?\\s*(.+)$")
    private val RE_UL_SEG = Regex("(?i)^(?:улица|ул)\\.?\\s*(.+)$")
    private val RE_PR_SEG = Regex("(?i)^(?:проспект|пр|просп)\\.?\\s*(.+)$")

    private val RE_MKR_FULL = Regex("(?i)(?:^|\\s)(?:мкр|микрорайон|м-н)\\.?\\s+([^,]+?)(?:\\s+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)|$)")
    private val RE_UL_FULL = Regex("(?i)(?:^|\\s)(?:улица|ул)\\.?\\s+([^,]+?)(?:\\s+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)|$)")
    private val RE_PR_FULL = Regex("(?i)(?:^|\\s)(?:проспект|пр|просп)\\.?\\s+([^,]+?)(?:\\s+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)|$)")
    private val RE_GENERIC = Regex("(?i)^([^\\d,]+?)(?:\\s+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?))?$")
    private val RE_NUM = Regex("(?i)(?:^|\\s)(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)(?=\\s|$)")

    private val RE_CITY_ALMATY = Regex("(?i)(?:город|г|қ)?\\.?\\s*алматы(?:\\s*қаласы)?")
    private val RE_CITY_ASTANA = Regex("(?i)(?:город|г|қ)?\\.?\\s*(?:астана|нур-султан|нұр-сұлтан)(?:\\s*қаласы)?")
    private val RE_CITY_SHYMKENT = Regex("(?i)(?:город|г|қ)?\\.?\\s*шымкент(?:\\s*қаласы)?")
    private val RE_CITY_AKTOBE = Regex("(?i)(?:город|г|қ)?\\.?\\s*(?:актобе|ақтөбе)(?:\\s*қаласы)?")
    private val RE_CITY_KARAGANDA = Regex("(?i)(?:город|г|қ)?\\.?\\s*(?:караганда|қарағанды)(?:\\s*қаласы)?")

    fun parse(raw: String): ParsedDeliveryAddress {
        if (raw.isBlank()) return ParsedDeliveryAddress(raw, confidence = 0.0)

        try {
            // 1. Clean invisible / zero-width characters
            var s = raw.replace(RE_ZERO_WIDTH, "")

            // 2. Bilingual slashes
            s = s.replace(RE_BILINGUAL, "$1")

            // 3. Fix spaced dots
            s = s.replace(RE_SPACED_DOT, "$1. ")

            // 4. Normalize typo in "подьезд" -> "подъезд"
            s = s.replace(RE_PODEZD, "подъезд")

            // 5. Separate attached digits and words
            s = s.replace(RE_DIGIT_WORD, "$1 $2")
            s = s.replace(RE_WORD_DIGIT, "$1 $2")
            s = s.replace(RE_SPACES, " ").trim()

            var current = s
            var floor: String? = null
            var entrance: String? = null
            var apartment: String? = null
            var landmark: String? = null
            var house: String? = null

            // 6. Floor
            val flM = RE_FL_PREFIX.find(current) ?: RE_FL_SUFFIX.find(current)
            if (flM != null) {
                floor = flM.groupValues[1]
                current = current.replace(flM.value, " ")
            }

            // 7. Entrance
            val entM = RE_ENT_PREFIX.find(current) ?: RE_ENT_SUFFIX.find(current)
            if (entM != null) {
                entrance = entM.groupValues[1]
                current = current.replace(entM.value, " ")
            }

            // 8. Apartment
            val aptM = RE_APT_PREFIX.find(current) ?: RE_APT_SUFFIX.find(current)
            if (aptM != null) {
                apartment = aptM.groupValues[1]
                current = current.replace(aptM.value, " ")
            }

            // 9. Landmark
            val lmM = RE_LANDMARK.find(current)
            if (lmM != null) {
                landmark = lmM.groupValues[1].trim()
                current = current.replace(lmM.value, " ")
            }

            // 10. Explicit House
            val hmM = RE_HOUSE_EXPLICIT.find(current)
            if (hmM != null) {
                house = hmM.groupValues[1]
                current = current.replace(hmM.value, " ")
            }

            current = current.replace(RE_SPACES, " ").trim()

            // 11. City & Province detection
            var city: String? = null
            var province: String? = null
            var district: String? = null

            val lower = current.lowercase()
            when {
                lower.contains("алматы") -> {
                    city = "Алматы"
                    province = "Алматы"
                    current = current.replace(RE_CITY_ALMATY, " ").trim()
                }
                lower.contains("астана") || lower.contains("нур-султан") || lower.contains("нұр-сұлтан") -> {
                    city = "Астана"
                    province = "Астана"
                    current = current.replace(RE_CITY_ASTANA, " ").trim()
                }
                lower.contains("шымкент") -> {
                    city = "Шымкент"
                    province = "Шымкент"
                    current = current.replace(RE_CITY_SHYMKENT, " ").trim()
                }
                lower.contains("актобе") || lower.contains("ақтөбе") -> {
                    city = "Актобе"
                    province = "Актюбинская область"
                    current = current.replace(RE_CITY_AKTOBE, " ").trim()
                }
                lower.contains("караганда") || lower.contains("қарағанды") -> {
                    city = "Караганда"
                    province = "Карагандинская область"
                    current = current.replace(RE_CITY_KARAGANDA, " ").trim()
                }
            }

            var streetType: String? = null
            var streetName: String? = null

            // 12. Segment or pattern extraction for Street & Microdistrict
            val cleanSegments = current.split(",").map { it.trim() }.filter { it.isNotBlank() }
            if (cleanSegments.size >= 2) {
                for (seg in cleanSegments) {
                    if (house == null && RE_HOUSE_NUM.matches(seg)) {
                        house = seg
                        continue
                    }
                    val mkrSeg = RE_MKR_SEG.find(seg)
                    if (mkrSeg != null) {
                        streetType = "микрорайон"
                        streetName = mkrSeg.groupValues[1].trim()
                        continue
                    }
                    val ulSeg = RE_UL_SEG.find(seg)
                    if (ulSeg != null) {
                        streetType = "улица"
                        streetName = ulSeg.groupValues[1].trim()
                        continue
                    }
                    val prSeg = RE_PR_SEG.find(seg)
                    if (prSeg != null) {
                        streetType = "проспект"
                        streetName = prSeg.groupValues[1].trim()
                        continue
                    }
                }
            }

            val sWithoutCommas = current.replace(",", " ").replace(RE_SPACES, " ").trim()

            if (streetName == null) {
                val mkrM = RE_MKR_FULL.find(sWithoutCommas)
                if (mkrM != null) {
                    streetType = "микрорайон"
                    streetName = mkrM.groupValues[1].trim()
                    if (house == null && mkrM.groupValues.size > 2 && mkrM.groupValues[2].isNotBlank()) {
                        house = mkrM.groupValues[2].trim()
                    }
                }
            }

            if (streetName == null) {
                val ulM = RE_UL_FULL.find(sWithoutCommas)
                if (ulM != null) {
                    streetType = "улица"
                    streetName = ulM.groupValues[1].trim()
                    if (house == null && ulM.groupValues.size > 2 && ulM.groupValues[2].isNotBlank()) {
                        house = ulM.groupValues[2].trim()
                    }
                }
            }

            if (streetName == null) {
                val prM = RE_PR_FULL.find(sWithoutCommas)
                if (prM != null) {
                    streetType = "проспект"
                    streetName = prM.groupValues[1].trim()
                    if (house == null && prM.groupValues.size > 2 && prM.groupValues[2].isNotBlank()) {
                        house = prM.groupValues[2].trim()
                    }
                }
            }

            if (streetName == null) {
                val genM = RE_GENERIC.find(sWithoutCommas)
                if (genM != null) {
                    streetName = genM.groupValues[1].trim()
                    if (house == null && genM.groupValues.size > 2 && genM.groupValues[2].isNotBlank()) {
                        house = genM.groupValues[2].trim()
                    }
                    if (streetName.equals("Туран", ignoreCase = true)) {
                        streetType = "проспект"
                        district = "Есиль"
                    }
                }
            }

            if (house == null) {
                val numM = RE_NUM.find(sWithoutCommas)
                if (numM != null) {
                    val candidate = numM.groupValues[1].trim()
                    if (streetName == null || !streetName.contains(candidate)) {
                        house = candidate
                    }
                }
            }

            // Automatic district enrichment from catalog for Almaty
            if (city == "Алматы" && district == null && streetName != null) {
                val lowerStreet = streetName.lowercase()
                if (lowerStreet.contains("аксай")) {
                    district = "Ауэзовский"
                } else if (lowerStreet.contains("орбита") || lowerStreet.contains("коктем") || lowerStreet.contains("розыбакиева") || lowerStreet.contains("гагарина")) {
                    district = "Бостандыкский"
                } else if (lowerStreet.contains("самал") || lowerStreet.contains("достык")) {
                    district = "Медеуский"
                } else if (lowerStreet.contains("курильская") || lowerStreet.contains("рыбинский") || lowerStreet.contains("суюнбая")) {
                    district = "Турксибский"
                } else if (lowerStreet.contains("абая") || lowerStreet.contains("толе би") || lowerStreet.contains("сейфуллина")) {
                    district = "Алмалинский"
                }
            }

            if (streetName != null) {
                val idx = raw.indexOf(streetName, ignoreCase = true)
                if (idx >= 0 && idx + streetName.length <= raw.length) {
                    streetName = raw.substring(idx, idx + streetName.length)
                }
            }

            var score = 0.20
            if (city != null) score += 0.25
            if (province != null) score += 0.10
            if (streetName != null) score += 0.20
            if (house != null) score += 0.15
            if (apartment != null) score += 0.06
            if (district != null) score += 0.05
            if (streetType != null) score += 0.05

            return ParsedDeliveryAddress(
                rawAddress = raw,
                province = province,
                city = city,
                district = district,
                streetType = streetType,
                streetName = streetName,
                house = house,
                apartment = apartment,
                entrance = entrance,
                floor = floor,
                landmark = landmark,
                confidence = Math.min(0.98, Math.max(0.15, score))
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Parsing error for: $raw", e)
            return ParsedDeliveryAddress(
                rawAddress = raw,
                confidence = 0.10
            )
        }
    }
}
