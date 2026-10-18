package com.example.myapplication.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppLang
import com.example.myapplication.data.Branch
import com.example.myapplication.data.ChatLine
import com.example.myapplication.data.Direction
import com.example.myapplication.data.MainTab
import com.example.myapplication.data.Parcel
import com.example.myapplication.data.SupabaseAccount
import com.example.myapplication.data.ParsedDeliveryAddress
import com.example.myapplication.data.SavedDeliveryAddress
import com.example.myapplication.data.SmartAddressParser
import com.example.myapplication.data.UserSession
import com.example.myapplication.data.Warehouse
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class JanaViewModel(private val saved: SavedStateHandle) : ViewModel() {
    private val account = SupabaseAccount()

    var language by mutableStateOf(
        saved.get<String>(KEY_LANG)
            ?.let { runCatching { AppLang.valueOf(it) }.getOrNull() }
            ?: AppLang.EN,
    )
        private set

    var branch by mutableStateOf(
        saved.get<String>(KEY_BRANCH)
            ?.let { runCatching { Branch.valueOf(it) }.getOrNull() }
            ?: Branch.NONE,
    )
        private set

    var session by mutableStateOf<UserSession?>(null)
        private set

    var tab by mutableStateOf(MainTab.HOME)
        private set

    var chat by mutableStateOf<List<ChatLine>>(emptyList())
        private set

    var warehouse by mutableStateOf(EMPTY_WAREHOUSE)
        private set

    var loadedParcels by mutableStateOf<List<Parcel>>(emptyList())
        private set

    var savedAddresses by mutableStateOf<List<SavedDeliveryAddress>>(
        listOf(
            SavedDeliveryAddress(
                id = "sample_default",
                rawAddress = "город Алматы/Алматы қаласы, Мкр Аксай 3а, 59, кв 70",
                parsed = ParsedDeliveryAddress(
                    rawAddress = "город Алматы/Алматы қаласы, Мкр Аксай 3а, 59, кв 70",
                    city = "Алматы",
                    province = "Алматы",
                    district = "Ауэзовский",
                    streetType = "микрорайон",
                    streetName = "Аксай 3а",
                    house = "59",
                    apartment = "70",
                    confidence = 0.95
                ),
                isDefault = true,
                label = "Основной адрес"
            )
        )
    )
        private set

    var ready by mutableStateOf(false)
        private set

    fun addDeliveryAddress(raw: String, label: String = "Адрес доставки"): SavedDeliveryAddress {
        val parsed = SmartAddressParser.parse(raw)
        val isFirst = savedAddresses.isEmpty()
        val newAddr = SavedDeliveryAddress(
            rawAddress = raw,
            parsed = parsed,
            isDefault = isFirst,
            label = label
        )
        savedAddresses = listOf(newAddr) + savedAddresses
        return newAddr
    }

    fun removeDeliveryAddress(id: String) {
        val updated = savedAddresses.filterNot { it.id == id }
        if (updated.isNotEmpty() && updated.none { it.isDefault }) {
            savedAddresses = updated.mapIndexed { idx, addr -> if (idx == 0) addr.copy(isDefault = true) else addr }
        } else {
            savedAddresses = updated
        }
    }

    fun setDefaultDeliveryAddress(id: String) {
        savedAddresses = savedAddresses.map { addr ->
            addr.copy(isDefault = addr.id == id)
        }
    }

    init {
        viewModelScope.launch {
            try {
                withTimeoutOrNull(2500L) {
                    account.restore()
                }?.let(::applyAccount)
            } catch (e: Throwable) {
                android.util.Log.e("JanaViewModel", "Account restore skipped or timed out", e)
            } finally {
                ready = true
            }
        }
    }

    fun chooseLanguage(value: AppLang) {
        language = value
        saved[KEY_LANG] = value.name
    }

    fun chooseBranch(value: Branch) {
        branch = value
        saved[KEY_BRANCH] = value.name
        viewModelScope.launch {
            runCatching { account.updateBranch(value) }
        }
    }

    fun selectTab(value: MainTab) {
        tab = value
    }

    fun requestOtp(dial: String, national: String, onDone: () -> Unit) {
        onDone()
    }

    fun verifyOtp(dial: String, national: String, code: String, onResult: (Boolean) -> Unit) {
        onResult(code.length == 6 && code.all(Char::isDigit))
    }

    fun login(dial: String, national: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val next = runCatching { account.login(dial, national, password) }.getOrNull()
            if (next == null) {
                onResult(false)
            } else {
                applyAccount(next)
                tab = MainTab.HOME
                onResult(true)
            }
        }
    }

    fun logout(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { account.logout() }
            clearSession()
            onDone()
        }
    }

    fun parcels(direction: Direction, statusId: String): List<Parcel> {
        return loadedParcels.filter { it.direction == direction && it.statusId == statusId }
    }

    fun ensureGreeting(text: String) {
        if (chat.isNotEmpty()) return
        viewModelScope.launch {
            val lines = runCatching { account.ensureGreeting(text) }.getOrNull()
            if (lines != null) chat = lines
        }
    }

    fun changePassword(next: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            onDone(runCatching { account.changePassword(next) }.getOrDefault(false))
        }
    }

    fun deleteAccount(onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = runCatching { account.deleteAccount() }.getOrDefault(false)
            if (ok) clearSession()
            onDone(ok)
        }
    }

    fun sendChat(text: String, reply: String, onDone: (Boolean) -> Unit) {
        val trimmed = text.trim().take(MAX_MESSAGE)
        if (trimmed.isEmpty()) {
            onDone(false)
            return
        }
        viewModelScope.launch {
            val lines = runCatching { account.sendChat(trimmed, reply) }.getOrNull()
            if (lines == null) {
                onDone(false)
            } else {
                chat = lines
                onDone(true)
            }
        }
    }

    private fun clearSession() {
        session = null
        chat = emptyList()
        loadedParcels = emptyList()
        warehouse = EMPTY_WAREHOUSE
        branch = Branch.NONE
        tab = MainTab.HOME
        saved.remove<String>(KEY_DIAL)
        saved.remove<String>(KEY_NATIONAL)
        saved.remove<String>(KEY_BRANCH)
    }

    private fun applyAccount(snapshot: com.example.myapplication.data.AccountSnapshot) {
        session = snapshot.session
        branch = snapshot.branch
        warehouse = snapshot.warehouse
        loadedParcels = snapshot.parcels
        chat = snapshot.chat
        saved[KEY_DIAL] = snapshot.session.dial
        saved[KEY_NATIONAL] = snapshot.session.national
        saved[KEY_BRANCH] = snapshot.branch.name
    }

    private companion object {
        const val KEY_LANG = "lang"
        const val KEY_BRANCH = "branch"
        const val KEY_DIAL = "dial"
        const val KEY_NATIONAL = "national"
        const val MAX_MESSAGE = 2000
        val EMPTY_WAREHOUSE = Warehouse("", "", "", "", "")
    }
}
