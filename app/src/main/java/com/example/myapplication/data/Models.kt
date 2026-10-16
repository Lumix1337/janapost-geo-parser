package com.example.myapplication.data

enum class AppLang(val nativeName: String, val flag: String) {
    KK("Қазақша", "🇰🇿"),
    ZH("中文", "🇨🇳"),
    RU("Русский", "🇷🇺"),
    EN("English", "🇺🇸"),
    UZ("O'zbek", "🇺🇿"),
    KY("Кыргызча", "🇰🇬"),
    TG("Тоҷикӣ", "🇹🇯"),
    MN("Монгол хэл", "🇲🇳"),
    ;

    fun chip(): String = when (this) {
        EN -> "EN"
        RU -> "RU"
        KK -> "KK"
        ZH -> "ZH"
        UZ -> "UZ"
        KY -> "KY"
        TG -> "TG"
        MN -> "MN"
    }

    /** Russian and English have full copy. Other picker rows stay visible and fall back. */
    fun resolved(): AppLang = when (this) {
        EN, ZH -> EN
        else -> RU
    }
}

enum class Branch {
    NONE,
    ALMATY,
    ASTANA,
    SHYMKENT,
    AKTOBE,
}

enum class Direction { RECEIVE, SEND }

enum class MainTab { HOME, ORDER, PICKUP, MESSAGE, PROFILE }

data class Country(
    val id: String,
    val flag: String,
    val dial: String,
    val digits: Int,
)

val Countries = listOf(
    Country("kz", "🇰🇿", "+7", 10),
    Country("ru", "🇷🇺", "+7", 10),
    Country("kg", "🇰🇬", "+996", 9),
    Country("uz", "🇺🇿", "+998", 9),
    Country("cn", "🇨🇳", "+86", 11),
)

data class UserSession(val dial: String, val national: String) {
    val formatted: String get() = formatSent(dial, national)
    val routeKey: String get() = "${dial.removePrefix("+")}-$national"
}

data class Warehouse(
    val userId: String,
    val phone: String,
    val addressEn: String,
    val addressRu: String,
    val addressLocal: String,
)

data class Parcel(
    val id: String,
    val tracking: String,
    val titleEn: String,
    val titleRu: String,
    val direction: Direction,
    val statusId: String,
    val updatedEn: String,
    val updatedRu: String,
    val weightEn: String,
    val weightRu: String,
    val marketplace: String,
)

data class ChatLine(val fromSupport: Boolean, val text: String)

fun formatSent(dial: String, digits: String): String {
    if (dial == "+7" && digits.length >= 10) {
        val d = digits.take(10)
        return "+7(${d.take(3)})${d.substring(3, 6)}-${d.substring(6, 8)}-${d.substring(8, 10)}"
    }
    return "$dial $digits"
}

fun formatPartial(dial: String, digits: String): String {
    if (dial != "+7") return digits
    val d = digits.take(10)
    return buildString {
        if (d.isNotEmpty()) append("(")
        append(d.take(3))
        if (d.length >= 3) append(") ")
        if (d.length > 3) append(d.substring(3, minOf(6, d.length)))
        if (d.length > 6) {
            append("-")
            append(d.substring(6, minOf(8, d.length)))
        }
        if (d.length > 8) {
            append("-")
            append(d.substring(8))
        }
    }
}

fun parseRoutePhone(raw: String): UserSession {
    val dialDigits = raw.substringBefore("-")
    val national = raw.substringAfter("-")
    return UserSession("+$dialDigits", national)
}
