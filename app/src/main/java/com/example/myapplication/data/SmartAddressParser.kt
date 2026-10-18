package com.example.myapplication.data

import android.util.Log
import java.util.regex.Pattern

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

    // Pre-compiled static Regex constants with (?iu) for universal Android Unicode Cyrillic support
    private val ZERO_WIDTH = Pattern.compile("[\u200B-\u200D\uFEFF\u200E\u200F\u00AD]")
    private val MULTI_SPACE = Pattern.compile("[\\s\u00A0]+")
    private val BILINGUAL_SLASH = Pattern.compile("(?iu)([^,/]+)/([^,/]+(?:қаласы|каласы|облысы|ауданы|көшесі|шағын ауданы))")
    private val SPACED_DOT = Pattern.compile("(?iu)(?<=[\\s,;]|^)(ул|мкр|пр|просп|пер|д|кв)\\s*\\.\\s*")
    private val PODEZD_TYPO = Pattern.compile("(?iu)(?<=[\\s,;]|^)подьезд(?=[\\s,;]|$)")
    private val DIGIT_WORD = Pattern.compile("(?iu)(\\d+)(квартал|дом|мкр|микрорайон|подьезд|подъезд|этаж|эт|кв|квартира|вход|блок)")
    private val WORD_DIGIT = Pattern.compile("(?iu)(дом|подьезд|подъезд|этаж|кв|квартира|flat|apt|под)(\\d+)")

    private val FLOOR_PREFIX = Pattern.compile("(?iu)(?:^|[,\\s])(?:этаж|эт|floor)\\s*[:#№]?\\s*(\\d+)(?=[,\\s]|$)")
    private val FLOOR_SUFFIX = Pattern.compile("(?iu)(?:^|[,\\s])(\\d+)\\s*(?:этаж|эт|floor)(?=[,\\s]|$)")
    private val ENTRANCE_PREFIX = Pattern.compile("(?iu)(?:^|[,\\s])(?:подъезд|подьезд|п-д|вход|entrance)\\s*[:#№]?\\s*(\\d+)(?=[,\\s]|$)")
    private val ENTRANCE_SUFFIX = Pattern.compile("(?iu)(?:^|[,\\s])(\\d+)\\s*(?:подъезд|подьезд|п-д|вход|entrance)(?=[,\\s]|$)")
    private val APT_PREFIX = Pattern.compile("(?iu)(?:^|[,\\s])(?:квартира|кв|flat|apt|комната|ком)\\s*[.\\-:#№]?\\s*(\\d+[а-яa-z]?)(?=[,\\s]|$)")
    private val APT_SUFFIX = Pattern.compile("(?iu)(?:^|[,\\s])(\\d+)\\s*(?:квартира|кв|flat)(?=[,\\s]|$)")
    private val LANDMARK_KEYWORD = Pattern.compile("(?iu)(?:^|[,\\s])((?:возле|рядом с|около|напротив|ориентир|ор-р|магазин|супермаркет|трц|тц|тд|бц|рынок|аптека|кафе|qazpost|казпочта|мечеть|парк)\\s+[\\p{L}0-9\\s\"'-]+?)(?=[,;]|(?<=[\\s,;]|^)(?:дом|д|кв|квартира|подъезд|подьезд|этаж|эт|floor|flat)(?=[\\s,;]|$)|\\d+\\s*(?:подъезд|подьезд|этаж|эт|дом|кв)|$)")
    private val HOUSE_EXPLICIT = Pattern.compile("(?iu)(?:^|[,\\s])(?:дом|д|house)\\s*[.\\-:#№]?\\s*(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)(?=[,\\s]|$)")

    private val DISTRICT_PATTERN = Pattern.compile("(?iu)(?:^|[,\\s])([\\p{L}]+(?:ский|ской|ский|ның| ауданы)?)\\s+(?:район|р-н|ауданы)(?=[,\\s]|$)")
    private val DISTRICT_PREFIX_PATTERN = Pattern.compile("(?iu)(?:^|[,\\s])(?:район|р-н|ауданы)\\s+([\\p{L}]+)(?=[,\\s]|$)")
    private val EDGE_CLEANUP = Pattern.compile("^[,\\s]+|[,\\s]+$")

    private val HOUSE_SEGMENT_PATTERN = Pattern.compile("^\\d+(?:[/\\\\]\\d+)?[а-яa-z]?$", Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE)
    private val MKR_SEG_PATTERN = Pattern.compile("(?iu)^(?:мкр|микрорайон|м-н|шағын ауданы)\\.?\\s*(.+)$")
    private val UL_SEG_PATTERN = Pattern.compile("(?iu)^(?:улица|ул|көшесі)\\.?\\s*(.+)$")
    private val PR_SEG_PATTERN = Pattern.compile("(?iu)^(?:проспект|пр|просп|даңғылы)\\.?\\s*(.+)$")
    private val PER_SEG_PATTERN = Pattern.compile("(?iu)^(.+?)\\s+(?:переулок|пер)\\.?$")
    private val KV_SEG_PATTERN = Pattern.compile("(?iu)^(.+?)\\s+(?:квартал|кв-л)\\.?$")

    private val MKR_FULL_PATTERN = Pattern.compile("(?iu)(?:^|[,\\s])(?:мкр|микрорайон|м-н|шағын ауданы)\\.?\\s+([\\p{L}0-9\\s-]+?)(?=[,\\s]+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)|$)")
    private val MKR_PRE_PATTERN = Pattern.compile("(?iu)(?:^|[,\\s])([\\p{L}0-9-]+)\\s+(?:мкр|микрорайон|м-н)(?=[,\\s]+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)|[,\\s]|$)")
    private val UL_PATTERN = Pattern.compile("(?iu)(?:^|[,\\s])(?:улица|ул|көшесі)\\.?\\s*([\\p{L}0-9\\s-]+?)(?=[,\\s]+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)|$)")
    private val PR_PATTERN = Pattern.compile("(?iu)(?:^|[,\\s])(?:проспект|пр|просп|даңғылы)\\.?\\s*([\\p{L}0-9\\s-]+?)(?=[,\\s]+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)|$)")
    private val PER_PATTERN = Pattern.compile("(?iu)(?:^|[,\\s])([\\p{L}0-9\\s-]+?)\\s+(?:переулок|пер)\\.?(?=[,\\s]+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)|$)")
    private val KV_PATTERN = Pattern.compile("(?iu)(?:^|[,\\s])([\\p{L}0-9-]+)\\s+(?:квартал|кв-л)\\.?(?=[,\\s]+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)|$)")
    private val GENERIC_PATTERN = Pattern.compile("(?iu)^([\\p{L}\\s-]+?)(?:\\s+(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?))?$")
    private val NUM_PATTERN = Pattern.compile("(?iu)(?:^|[,\\s])(\\d+(?:[/\\\\]\\d+)?[а-яa-z]?)(?=[,\\s]|$)")

    private val RE_CITY_ALMATY = Pattern.compile("(?iu)(?:город|г|қ)?\\.?\\s*алматы(?:\\s*(?:қаласы|каласы))?")
    private val RE_CITY_ASTANA = Pattern.compile("(?iu)(?:город|г|қ)?\\.?\\s*(?:астана|нур-султан|нұр-сұлтан)(?:\\s*(?:қаласы|каласы))?")
    private val RE_CITY_SHYMKENT = Pattern.compile("(?iu)(?:город|г|қ)?\\.?\\s*шымкент(?:\\s*(?:қаласы|каласы))?")
    private val RE_CITY_AKTOBE = Pattern.compile("(?iu)(?:город|г|қ)?\\.?\\s*(?:актобе|ақтөбе)(?:\\s*(?:қаласы|каласы))?")
    private val RE_CITY_KARAGANDA = Pattern.compile("(?iu)(?:город|г|қ)?\\.?\\s*(?:караганда|қарағанды)(?:\\s*(?:қаласы|каласы))?")

    fun parse(raw: String): ParsedDeliveryAddress {
        if (raw.isBlank()) return ParsedDeliveryAddress(raw, confidence = 0.0)

        try {
            // 1. Clean invisible / zero-width characters
            var s = ZERO_WIDTH.matcher(raw).replaceAll("")
            s = MULTI_SPACE.matcher(s).replaceAll(" ").trim()

            // 2. Bilingual slashes
            val slashM = BILINGUAL_SLASH.matcher(s)
            if (slashM.find()) {
                s = slashM.replaceAll("$1")
            }

            // 3. Fix spaced dots
            s = SPACED_DOT.matcher(s).replaceAll("$1. ")

            // 4. Normalize typo in "подьезд" -> "подъезд"
            s = PODEZD_TYPO.matcher(s).replaceAll("подъезд")

            // 5. Separate attached digits and words
            s = DIGIT_WORD.matcher(s).replaceAll("$1 $2")
            s = WORD_DIGIT.matcher(s).replaceAll("$1 $2")
            s = MULTI_SPACE.matcher(s).replaceAll(" ").trim()

            var current = s
            var floor: String? = null
            var entrance: String? = null
            var apartment: String? = null
            var landmark: String? = null
            var house: String? = null

            // 6. Floor
            val flM = FLOOR_PREFIX.matcher(current)
            if (flM.find()) {
                floor = flM.group(1)?.trim()
                current = current.substring(0, flM.start()) + " " + current.substring(flM.end())
            } else {
                val flsM = FLOOR_SUFFIX.matcher(current)
                if (flsM.find()) {
                    floor = flsM.group(1)?.trim()
                    current = current.substring(0, flsM.start()) + " " + current.substring(flsM.end())
                }
            }

            // 7. Entrance
            val entM = ENTRANCE_PREFIX.matcher(current)
            if (entM.find()) {
                entrance = entM.group(1)?.trim()
                current = current.substring(0, entM.start()) + " " + current.substring(entM.end())
            } else {
                val entsM = ENTRANCE_SUFFIX.matcher(current)
                if (entsM.find()) {
                    entrance = entsM.group(1)?.trim()
                    current = current.substring(0, entsM.start()) + " " + current.substring(entsM.end())
                }
            }

            // 8. Apartment
            val aptM = APT_PREFIX.matcher(current)
            if (aptM.find()) {
                apartment = aptM.group(1)?.trim()
                current = current.substring(0, aptM.start()) + " " + current.substring(aptM.end())
            } else {
                val aptsM = APT_SUFFIX.matcher(current)
                if (aptsM.find()) {
                    apartment = aptsM.group(1)?.trim()
                    current = current.substring(0, aptsM.start()) + " " + current.substring(aptsM.end())
                }
            }

            // 9. Landmark
            val lmM = LANDMARK_KEYWORD.matcher(current)
            if (lmM.find()) {
                landmark = lmM.group(1)?.trim()
                current = current.substring(0, lmM.start(1)) + " " + current.substring(lmM.end(1))
            }

            // 10. Explicit House
            val hmM = HOUSE_EXPLICIT.matcher(current)
            if (hmM.find()) {
                house = hmM.group(1)?.trim()
                current = current.substring(0, hmM.start()) + " " + current.substring(hmM.end())
            }

            current = MULTI_SPACE.matcher(current).replaceAll(" ").trim()

            // 11. Explicit District in String
            var district: String? = null
            val distM = DISTRICT_PATTERN.matcher(current)
            if (distM.find()) {
                val rawDist = distM.group(1)?.trim().orEmpty()
                if (rawDist.isNotEmpty()) {
                    val cleanedDist = rawDist.replace(Regex("(?i)\\s*(?:район|р-н|ауданы)$"), "").trim()
                    if (cleanedDist.isNotEmpty()) {
                        district = cleanedDist.substring(0, 1).uppercase() + cleanedDist.substring(1).lowercase()
                    }
                    current = current.substring(0, distM.start()) + " " + current.substring(distM.end())
                }
            } else {
                val distPreM = DISTRICT_PREFIX_PATTERN.matcher(current)
                if (distPreM.find()) {
                    val rawDist = distPreM.group(1)?.trim().orEmpty()
                    if (rawDist.isNotEmpty()) {
                        district = rawDist.substring(0, 1).uppercase() + rawDist.substring(1).lowercase()
                        current = current.substring(0, distPreM.start()) + " " + current.substring(distPreM.end())
                    }
                }
            }

            // 12. City & Province detection
            var city: String? = null
            var province: String? = null

            val lower = current.lowercase()
            when {
                lower.contains("алматы") -> {
                    city = "Алматы"
                    province = "Алматы"
                    current = RE_CITY_ALMATY.matcher(current).replaceAll(" ")
                }
                lower.contains("астана") || lower.contains("нур-султан") || lower.contains("нұр-сұлтан") -> {
                    city = "Астана"
                    province = "Астана"
                    current = RE_CITY_ASTANA.matcher(current).replaceAll(" ")
                }
                lower.contains("шымкент") -> {
                    city = "Шымкент"
                    province = "Шымкент"
                    current = RE_CITY_SHYMKENT.matcher(current).replaceAll(" ")
                }
                lower.contains("актобе") || lower.contains("ақтөбе") -> {
                    city = "Актобе"
                    province = "Актюбинская область"
                    current = RE_CITY_AKTOBE.matcher(current).replaceAll(" ")
                }
                lower.contains("караганда") || lower.contains("қарағанды") -> {
                    city = "Караганда"
                    province = "Карагандинская область"
                    current = RE_CITY_KARAGANDA.matcher(current).replaceAll(" ")
                }
            }

            // Clean edge separators after city removal
            current = EDGE_CLEANUP.matcher(current).replaceAll("").trim()

            var streetType: String? = null
            var streetName: String? = null

            // 13. Street & House Extraction
            if (current.contains(",")) {
                val segments = current.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                for (seg in segments) {
                    if (house == null && HOUSE_SEGMENT_PATTERN.matcher(seg).matches()) {
                        house = seg
                        continue
                    }
                    val mm = MKR_SEG_PATTERN.matcher(seg)
                    if (mm.find()) {
                        streetType = "микрорайон"
                        streetName = mm.group(1)?.trim()
                        continue
                    }
                    val um = UL_SEG_PATTERN.matcher(seg)
                    if (um.find()) {
                        streetType = "улица"
                        streetName = um.group(1)?.trim()
                        continue
                    }
                    val pm = PR_SEG_PATTERN.matcher(seg)
                    if (pm.find()) {
                        streetType = "проспект"
                        streetName = pm.group(1)?.trim()
                        continue
                    }
                    val perM = PER_SEG_PATTERN.matcher(seg)
                    if (perM.find()) {
                        streetType = "переулок"
                        streetName = perM.group(1)?.trim()
                        continue
                    }
                    val kvM = KV_SEG_PATTERN.matcher(seg)
                    if (kvM.find()) {
                        streetType = "квартал"
                        streetName = kvM.group(1)?.trim()
                        continue
                    }
                }
            }

            // Regex fallback if not resolved by commas
            val sWithoutCommas = current.replace("[,;]+".toRegex(), " ")
                .replace(MULTI_SPACE.toRegex(), " ")
                .trim()

            if (streetName == null) {
                val mm = MKR_FULL_PATTERN.matcher(sWithoutCommas)
                if (mm.find()) {
                    streetType = "микрорайон"
                    streetName = mm.group(1)?.trim()
                    if (house == null && mm.groupCount() >= 2 && mm.group(2) != null) {
                        house = mm.group(2)?.trim()
                    }
                }
            }

            if (streetName == null) {
                val preM = MKR_PRE_PATTERN.matcher(sWithoutCommas)
                if (preM.find()) {
                    streetType = "микрорайон"
                    streetName = preM.group(1)?.trim()
                    if (house == null && preM.group(2) != null) {
                        house = preM.group(2)?.trim()
                    }
                }
            }

            if (streetName == null) {
                val um = UL_PATTERN.matcher(sWithoutCommas)
                if (um.find()) {
                    streetType = "улица"
                    streetName = um.group(1)?.trim()
                    if (house == null && um.groupCount() >= 2 && um.group(2) != null) {
                        house = um.group(2)?.trim()
                    }
                }
            }

            if (streetName == null) {
                val pm = PR_PATTERN.matcher(sWithoutCommas)
                if (pm.find()) {
                    streetType = "проспект"
                    streetName = pm.group(1)?.trim()
                    if (house == null && pm.groupCount() >= 2 && pm.group(2) != null) {
                        house = pm.group(2)?.trim()
                    }
                }
            }

            if (streetName == null) {
                val perM = PER_PATTERN.matcher(sWithoutCommas)
                if (perM.find()) {
                    streetType = "переулок"
                    streetName = perM.group(1)?.trim()
                    if (house == null && perM.groupCount() >= 2 && perM.group(2) != null) {
                        house = perM.group(2)?.trim()
                    }
                }
            }

            if (streetName == null) {
                val kvM = KV_PATTERN.matcher(sWithoutCommas)
                if (kvM.find()) {
                    streetType = "квартал"
                    streetName = kvM.group(1)?.trim()
                    if (house == null && kvM.groupCount() >= 2 && kvM.group(2) != null) {
                        house = kvM.group(2)?.trim()
                    }
                }
            }

            if (streetName == null) {
                val genM = GENERIC_PATTERN.matcher(sWithoutCommas)
                if (genM.find()) {
                    streetName = genM.group(1)?.trim()
                    if (house == null && genM.group(2) != null) {
                        house = genM.group(2)?.trim()
                    }
                    if (streetName.equals("Туран", ignoreCase = true)) {
                        streetType = "проспект"
                        district = "Есиль"
                    }
                }
            }

            if (house == null) {
                val nm = NUM_PATTERN.matcher(sWithoutCommas)
                while (nm.find()) {
                    val candidate = nm.group(1)?.trim()
                    if (candidate != null && (streetName == null || !streetName.contains(candidate))) {
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

            // Preserve original case from rawAddress
            if (streetName != null) {
                val idx = raw.lowercase().indexOf(streetName.lowercase())
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
