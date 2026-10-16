package com.example.myapplication.data

import com.example.myapplication.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class AccountSnapshot(
    val session: UserSession,
    val branch: Branch,
    val warehouse: Warehouse,
    val parcels: List<Parcel>,
    val chat: List<ChatLine>,
)

class SupabaseAccount {
    private val loginLock = Mutex()
    private val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {
        defaultSerializer = KotlinXSerializer(Json { ignoreUnknownKeys = true })
        install(Auth)
        install(Postgrest)
        install(Functions)
    }

    suspend fun restore(): AccountSnapshot? {
        client.auth.sessionStatus.first { it !is SessionStatus.Initializing }
        if (client.auth.currentSessionOrNull() == null) return null
        return loadCurrent()
    }

    suspend fun login(dial: String, national: String, password: String): AccountSnapshot? = loginLock.withLock {
        if (password.length < 6) return null
        val email = accountEmail(dial, national)
        if (!signIn(email, password)) {
            if (!register(dial, national, password) || !signIn(email, password)) return null
        }
        loadCurrent() ?: createAccount(dial, national)
    }

    suspend fun logout() {
        client.auth.signOut()
    }

    suspend fun changePassword(next: String): Boolean {
        if (next.length < 6) return false
        return try {
            client.auth.updateUser { password = next }
            true
        } catch (_: RestException) {
            false
        }
    }

    suspend fun deleteAccount(): Boolean {
        return try {
            client.functions.invoke("delete-account")
            client.auth.signOut()
            true
        } catch (_: RestException) {
            false
        }
    }

    suspend fun updateBranch(branch: Branch) {
        val userId = client.auth.currentUserOrNull()?.id ?: return
        client.from("profiles").update(BranchUpdate(branch.name)) {
            filter { eq("id", userId) }
        }
    }

    suspend fun sendChat(text: String, reply: String): List<ChatLine>? {
        val userId = client.auth.currentUserOrNull()?.id ?: return null
        client.from("messages").insert(
            listOf(
                MessageInsert(userId, false, text),
                MessageInsert(userId, true, reply),
            ),
        )
        return loadMessages(userId)
    }

    suspend fun ensureGreeting(text: String): List<ChatLine>? {
        val userId = client.auth.currentUserOrNull()?.id ?: return null
        val existing = loadMessages(userId)
        if (existing.isNotEmpty()) return existing
        client.from("messages").insert(MessageInsert(userId, true, text))
        return loadMessages(userId)
    }

    private suspend fun loadCurrent(): AccountSnapshot? {
        val userId = client.auth.currentUserOrNull()?.id ?: return null
        val profile = client.from("profiles").select(Columns.ALL) {
            filter { eq("id", userId) }
        }.decodeSingleOrNull<ProfileRow>() ?: return null
        return snapshot(profile, loadParcels(userId), loadMessages(userId))
    }

    private suspend fun createAccount(dial: String, national: String): AccountSnapshot? {
        val userId = client.auth.currentUserOrNull()?.id ?: return null
        val warehouseId = "JP" + randomDigits(8)
        val warehousePhone = "1" + randomDigits(10)
        val profile = ProfileInsert(
            id = userId,
            dial = dial,
            national = national,
            warehouseId = warehouseId,
            warehousePhone = warehousePhone,
            branch = Branch.NONE.name,
            addressEn = "Recipient $warehouseId. $ADDRESS_EN",
            addressRu = "Получатель $warehouseId. $ADDRESS_RU",
            addressLocal = "收件人 $warehouseId $ADDRESS_LOCAL",
        )
        try {
            client.from("profiles").insert(profile)
        } catch (_: RestException) {
            return loadCurrent()
        }
        try {
            client.from("parcels").insert(demoParcels(userId))
        } catch (_: RestException) {
            // Profile already exists from a previous attempt. Keep the account usable.
        }
        return loadCurrent()
    }

    private suspend fun signIn(email: String, password: String): Boolean {
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (_: RestException) {
            false
        }
    }

    private suspend fun register(dial: String, national: String, password: String): Boolean {
        return try {
            client.functions.invoke(
                "register-phone",
                RegisterBody(dial, national, password),
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
            true
        } catch (_: RestException) {
            false
        }
    }

    private suspend fun loadParcels(userId: String): List<Parcel> {
        return client.from("parcels").select(Columns.ALL) {
            filter { eq("user_id", userId) }
        }.decodeList<ParcelRow>().mapNotNull { row -> runCatching { row.toParcel() }.getOrNull() }
    }

    private suspend fun loadMessages(userId: String): List<ChatLine> {
        return client.from("messages").select(Columns.ALL) {
            filter { eq("user_id", userId) }
        }.decodeList<MessageRow>()
            .sortedBy { it.createdAt }
            .map { ChatLine(fromSupport = it.fromSupport, text = it.body) }
    }

    private fun snapshot(profile: ProfileRow, parcels: List<Parcel>, chat: List<ChatLine>): AccountSnapshot {
        return AccountSnapshot(
            session = UserSession(profile.dial, profile.national),
            branch = runCatching { Branch.valueOf(profile.branch) }.getOrDefault(Branch.NONE),
            warehouse = Warehouse(
                userId = profile.warehouseId,
                phone = profile.warehousePhone,
                addressEn = profile.addressEn,
                addressRu = profile.addressRu,
                addressLocal = profile.addressLocal,
            ),
            parcels = parcels,
            chat = chat,
        )
    }

    private fun accountEmail(dial: String, national: String): String {
        return "${dial.removePrefix("+")}.$national@users.janapost.app"
    }

    private fun randomDigits(count: Int): String = buildString(count) {
        repeat(count) { append(('0'..'9').random()) }
    }

    private fun demoParcels(userId: String): List<ParcelInsert> = listOf(
        ParcelInsert(userId, tracking(), "Sneakers", "Кроссовки", "RECEIVE", "in_stock", "28 Sep", "28 сен", "1.2 kg", "1,2 кг", "Temu"),
        ParcelInsert(userId, tracking(), "Phone case", "Чехол", "RECEIVE", "on_road", "27 Sep", "27 сен", "0.2 kg", "0,2 кг", "Pinduoduo"),
        ParcelInsert(userId, tracking(), "Headphones", "Наушники", "RECEIVE", "pending", "29 Sep", "29 сен", "0.4 kg", "0,4 кг", "Taobao"),
        ParcelInsert(userId, tracking(), "Cable", "Кабель", "RECEIVE", "received", "20 Sep", "20 сен", "0.1 kg", "0,1 кг", "Temu"),
        ParcelInsert(userId, tracking(), "Hoodie", "Худи", "SEND", "sorting", "26 Sep", "26 сен", "0.8 kg", "0,8 кг", "JD.com"),
        ParcelInsert(userId, tracking(), "Books", "Книги", "SEND", "accepted", "25 Sep", "25 сен", "2.0 kg", "2,0 кг", "Pinduoduo"),
    )

    private fun tracking(): String = "JP" + randomDigits(10)

    private companion object {
        const val ADDRESS_EN = "JanaPost Demo Warehouse, 88 Chouzhou North Road, Yiwu, Zhejiang, China"
        const val ADDRESS_RU = "Демо-склад JanaPost, Chouzhou North Road, 88, Иу, Чжэцзян, Китай"
        const val ADDRESS_LOCAL = "浙江省义乌市稠州北路88号 JanaPost Demo"
    }
}

@Serializable
private data class RegisterBody(
    val dial: String,
    val national: String,
    val password: String,
)

@Serializable
private data class ProfileInsert(
    val id: String,
    val dial: String,
    val national: String,
    @SerialName("warehouse_id") val warehouseId: String,
    @SerialName("warehouse_phone") val warehousePhone: String,
    val branch: String,
    @SerialName("address_en") val addressEn: String,
    @SerialName("address_ru") val addressRu: String,
    @SerialName("address_local") val addressLocal: String,
)

@Serializable
private data class ProfileRow(
    val id: String,
    val dial: String,
    val national: String,
    @SerialName("warehouse_id") val warehouseId: String,
    @SerialName("warehouse_phone") val warehousePhone: String,
    val branch: String,
    @SerialName("address_en") val addressEn: String,
    @SerialName("address_ru") val addressRu: String,
    @SerialName("address_local") val addressLocal: String,
)

@Serializable
private data class BranchUpdate(val branch: String)

@Serializable
private data class ParcelInsert(
    @SerialName("user_id") val userId: String,
    val tracking: String,
    @SerialName("title_en") val titleEn: String,
    @SerialName("title_ru") val titleRu: String,
    val direction: String,
    @SerialName("status_id") val statusId: String,
    @SerialName("updated_en") val updatedEn: String,
    @SerialName("updated_ru") val updatedRu: String,
    @SerialName("weight_en") val weightEn: String,
    @SerialName("weight_ru") val weightRu: String,
    val marketplace: String,
)

@Serializable
private data class ParcelRow(
    val id: String,
    val tracking: String,
    @SerialName("title_en") val titleEn: String,
    @SerialName("title_ru") val titleRu: String,
    val direction: String,
    @SerialName("status_id") val statusId: String,
    @SerialName("updated_en") val updatedEn: String,
    @SerialName("updated_ru") val updatedRu: String,
    @SerialName("weight_en") val weightEn: String,
    @SerialName("weight_ru") val weightRu: String,
    val marketplace: String,
) {
    fun toParcel(): Parcel = Parcel(
        id = id,
        tracking = tracking,
        titleEn = titleEn,
        titleRu = titleRu,
        direction = Direction.valueOf(direction),
        statusId = statusId,
        updatedEn = updatedEn,
        updatedRu = updatedRu,
        weightEn = weightEn,
        weightRu = weightRu,
        marketplace = marketplace,
    )
}

@Serializable
private data class MessageInsert(
    @SerialName("user_id") val userId: String,
    @SerialName("from_support") val fromSupport: Boolean,
    val body: String,
)

@Serializable
private data class MessageRow(
    val id: String,
    @SerialName("from_support") val fromSupport: Boolean,
    val body: String,
    @SerialName("created_at") val createdAt: String = "",
)
