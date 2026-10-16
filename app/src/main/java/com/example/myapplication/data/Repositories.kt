package com.example.myapplication.data

/**
 * Sign-in boundary. The mock accepts any 6-digit code and any non-empty password.
 * Swap this for Supabase Auth plus an SMS/WhatsApp provider without touching the screens.
 */
interface AuthRepository {
    suspend fun requestOtp(dial: String, national: String)
    suspend fun verifyOtp(dial: String, national: String, code: String): Boolean
    suspend fun login(dial: String, national: String, password: String): UserSession?
}

interface CatalogRepository {
    val warehouse: Warehouse
    fun parcels(direction: Direction, statusId: String): List<Parcel>
}
