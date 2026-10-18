package com.example.myapplication.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.AppLang
import com.example.myapplication.data.Branch
import com.example.myapplication.ui.JanaViewModel
import com.example.myapplication.ui.components.ChoiceSheet
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.i18n.Strings
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.ui.components.copyText
import com.example.myapplication.ui.components.showToast
import com.example.myapplication.ui.theme.JanaColors

@Composable
fun SettingsScreen(
    vm: JanaViewModel,
    strings: Strings,
    onBack: () -> Unit,
    onChangeAccount: () -> Unit,
    onProhibited: () -> Unit,
    onContact: () -> Unit,
    onPassword: () -> Unit,
    onAbout: () -> Unit,
    onDelete: () -> Unit,
) {
    var languageOpen by remember { mutableStateOf(false) }
    var branchOpen by remember { mutableStateOf(false) }
    Scaffold(containerColor = JanaColors.Background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                }
                Text(
                    strings.settingsTitle,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Spacer(Modifier.width(48.dp))
            }
            SettingsCard(
                listOf(
                    SettingsRow(Icons.Outlined.Person, strings.changeAccount, onChangeAccount),
                    SettingsRow(Icons.Outlined.Home, strings.openBranch, onClick = { branchOpen = true }),
                    SettingsRow(Icons.Outlined.RemoveCircleOutline, strings.prohibited, onProhibited),
                ),
            )
            Spacer(Modifier.height(12.dp))
            SettingsCard(
                listOf(
                    SettingsRow(Icons.Outlined.Call, strings.contact, onContact),
                    SettingsRow(Icons.Outlined.Key, strings.changePassword, onPassword),
                    SettingsRow(Icons.Outlined.Language, strings.selectLanguage, { languageOpen = true }, vm.language.chip()),
                    SettingsRow(Icons.Outlined.Info, strings.about, onAbout),
                ),
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .clickable(onClick = onChangeAccount)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = JanaColors.Danger)
                Spacer(Modifier.width(12.dp))
                Text(strings.logout, color = JanaColors.Danger, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(18.dp))
            Text(
                buildAnnotatedString {
                    append(strings.deletePrefix)
                    withStyle(SpanStyle(color = JanaColors.Green, fontWeight = FontWeight.SemiBold)) {
                        append(strings.deleteHere)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDelete),
                textAlign = TextAlign.Center,
                color = JanaColors.Muted,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
    ChoiceSheet(
        visible = languageOpen,
        title = strings.chooseLanguage,
        options = AppLang.entries,
        isSelected = { it == vm.language },
        label = { it.nativeName },
        leading = { Text(it.flag, fontSize = 22.sp) },
        onSelect = vm::chooseLanguage,
        onDismiss = { languageOpen = false },
    )
    ChoiceSheet(
        visible = branchOpen,
        title = strings.branchLabel,
        options = Branch.entries,
        isSelected = { it == vm.branch },
        label = { it.label(strings) },
        leading = {
            Spacer(
                Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(JanaColors.GreenSoft),
            )
        },
        onSelect = vm::chooseBranch,
        onDismiss = { branchOpen = false },
    )
}

private data class SettingsRow(
    val icon: ImageVector,
    val title: String,
    val onClick: () -> Unit,
    val value: String? = null,
)

@Composable
private fun SettingsCard(rows: List<SettingsRow>) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White),
    ) {
        rows.forEachIndexed { index, row ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = row.onClick)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(row.icon, contentDescription = null, tint = JanaColors.Text, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(row.title, modifier = Modifier.weight(1f), fontSize = 16.sp)
                if (row.value != null) {
                    Text(row.value, color = JanaColors.Green, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = JanaColors.Muted)
            }
            if (index != rows.lastIndex) {
                HorizontalDivider(color = JanaColors.Line, modifier = Modifier.padding(start = 50.dp))
            }
        }
    }
}

@Composable
fun AddressScreen(vm: JanaViewModel, strings: Strings, english: Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    val warehouse = vm.warehouse
    val address = if (english) warehouse.addressEn else warehouse.addressRu

    var selectedTab by remember { mutableStateOf(0) }
    var rawInput by remember { mutableStateOf("") }

    val sampleAddresses = remember {
        listOf(
            "город Алматы/Алматы қаласы, Мкр Аксай 3а, 59, кв 70",
            "г. Астана, пр. Туран 48, блок 2, кв 12, 5 этаж",
            "Караганда 66квартал 12дом 45кв",
            "Шымкент мкр Нурсат 12 рядом с ТРЦ Рахима Плаза"
        )
    }

    Page(strings.myAddress, strings.back, onBack) {
        // Tab switcher
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tab0Selected = selectedTab == 0
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (tab0Selected) JanaColors.Green else Color.Transparent)
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🇰🇿 Доставка в РК",
                    color = if (tab0Selected) Color.White else JanaColors.Text,
                    fontWeight = if (tab0Selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            }

            val tab1Selected = selectedTab == 1
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (tab1Selected) JanaColors.Green else Color.Transparent)
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🇨🇳 Склад в Китае",
                    color = if (tab1Selected) Color.White else JanaColors.Text,
                    fontWeight = if (tab1Selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        if (selectedTab == 0) {
            // KZ DELIVERY ADDRESSES (SMART ADDRESS PARSER)
            Text("⚡ Умный адрес доставки (Jana Address)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                "Введите адрес одной строкой в свободной форме. Система автоматически разберёт его и сохранит для курьера:",
                color = JanaColors.Muted,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(10.dp))

            // Quick sample chips
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sampleAddresses.forEachIndexed { index, sample ->
                    val label = when (index) {
                        0 -> "Алматы (Аксай)"
                        1 -> "Астана (Туран)"
                        2 -> "Слитный (66кв)"
                        else -> "Шымкент (Ориентир)"
                    }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(JanaColors.GreenSoft)
                            .clickable { rawInput = sample }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(label, color = JanaColors.Green, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Input field
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(14.dp)
            ) {
                if (rawInput.isEmpty()) {
                    Text("Например: Алматы, Аксай 3а, 59, кв 70", color = JanaColors.Muted, fontSize = 14.sp)
                }
                BasicTextField(
                    value = rawInput,
                    onValueChange = { rawInput = it },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, color = JanaColors.Text),
                    cursorBrush = SolidColor(JanaColors.Green),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))

            PrimaryButton("+ Разобрать и сохранить адрес", enabled = rawInput.isNotBlank()) {
                val trimmed = rawInput.trim()
                if (trimmed.isNotEmpty()) {
                    vm.addDeliveryAddress(trimmed)
                    rawInput = ""
                    showToast(context, "Адрес успешно разобран и сохранён!")
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = JanaColors.Line)
            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Сохранённые адреса (${vm.savedAddresses.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            if (vm.savedAddresses.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = JanaColors.Muted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Нет сохранённых адресов", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Добавьте адрес доставки выше — он появится здесь с автоматическим разбором по КАТО.",
                            color = JanaColors.Muted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    vm.savedAddresses.forEach { item ->
                        val res = item.parsed
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .border(
                                    width = if (item.isDefault) 1.5.dp else 0.5.dp,
                                    color = if (item.isDefault) JanaColors.Green else JanaColors.Line,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(16.dp)
                        ) {
                            // Header of Address Card: Default Badge & Delete button
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (item.isDefault) {
                                    Box(
                                        Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(JanaColors.GreenSoft)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "✓ Основной адрес",
                                            color = JanaColors.Green,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                } else {
                                    Box(
                                        Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(JanaColors.Background)
                                            .clickable { vm.setDefaultDeliveryAddress(item.id) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "Сделать основным",
                                            color = JanaColors.Muted,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(JanaColors.GreenSoft)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "${(res.confidence * 100).toInt()}%",
                                            color = JanaColors.Green,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { vm.removeDeliveryAddress(item.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = "Удалить адрес",
                                            tint = JanaColors.Muted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // City & KATO District
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    res.city ?: res.province ?: "Казахстан",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                res.district?.let { katoDistrict ->
                                    Box(
                                        Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFE8F1FC))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "$katoDistrict (КАТО)",
                                            color = Color(0xFF1E6FD9),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(6.dp))

                            // Street & House
                            val streetStr = buildString {
                                if (!res.streetType.isNullOrBlank()) append("${res.streetType} ")
                                append(res.streetName ?: "Улица не определена")
                                if (!res.house.isNullOrBlank()) append(", дом ${res.house}")
                            }
                            Text(streetStr, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                            // Apartment, Entrance, Floor row
                            val details = mutableListOf<String>()
                            if (!res.apartment.isNullOrBlank()) details.add("кв. ${res.apartment}")
                            if (!res.entrance.isNullOrBlank()) details.add("подъезд ${res.entrance}")
                            if (!res.floor.isNullOrBlank()) details.add("этаж ${res.floor}")

                            if (details.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    details.joinToString(" • "),
                                    color = JanaColors.Text,
                                    fontSize = 13.sp
                                )
                            }

                            // Landmark
                            if (!res.landmark.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "📍 ${res.landmark}",
                                    color = JanaColors.GreenDark,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = JanaColors.Line)
                            Spacer(Modifier.height(6.dp))

                            // Raw Input preview
                            Text(
                                "Исходный текст: \"${item.rawAddress}\"",
                                color = JanaColors.Muted,
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        } else {
            // CHINA WAREHOUSE TAB
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF9E6))
                    .padding(12.dp)
            ) {
                Text(
                    "ℹ️ Скопируйте эти данные в мобильное приложение Taobao, 1688 или Pinduoduo. Обязательно укажите ваш код клиента в примечании или имени получателя.",
                    color = Color(0xFF8A6A00),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            WarehouseRowWithCopy(
                label = strings.yourId,
                value = warehouse.userId,
                context = context,
                toast = strings.copied
            )
            WarehouseRowWithCopy(
                label = strings.phone,
                value = warehouse.phone,
                context = context,
                toast = strings.copied
            )
            WarehouseRowWithCopy(
                label = strings.showAddress,
                value = address,
                context = context,
                toast = strings.copied
            )
            WarehouseRowWithCopy(
                label = "Оригинальный адрес (中文)",
                value = warehouse.addressLocal,
                context = context,
                toast = strings.copied
            )
        }
    }
}

@Composable
private fun WarehouseRowWithCopy(
    label: String,
    value: String,
    context: android.content.Context,
    toast: String
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = JanaColors.Muted, fontSize = 12.sp)
            IconButton(
                onClick = { copyText(context, label, value, toast) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = "Копировать",
                    tint = JanaColors.Green,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
fun PromoScreen(strings: Strings, onBack: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    Page(strings.promo, strings.back, onBack) {
        Field(strings.promoHint, code) { code = it }
        Spacer(Modifier.height(16.dp))
        PrimaryButton(strings.promoApply, enabled = code.isNotBlank()) { saved = true }
        if (saved) {
            Spacer(Modifier.height(12.dp))
            Text(strings.promoSaved, color = JanaColors.Green)
        }
    }
}

@Composable
fun ChangePasswordScreen(vm: JanaViewModel, strings: Strings, onBack: () -> Unit) {
    var next by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    Page(strings.changePassword, strings.back, onBack) {
        Field(strings.newPassword, next, secret = true) { next = it }
        Spacer(Modifier.height(12.dp))
        Field(strings.repeatPassword, repeat, secret = true) { repeat = it }
        if (message != null) {
            Spacer(Modifier.height(10.dp))
            Text(message.orEmpty(), color = if (error) JanaColors.Danger else JanaColors.Green, fontSize = 14.sp)
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton(strings.save, enabled = !busy) {
            when {
                next.length < 6 -> {
                    error = true
                    message = strings.passwordTooShort
                }
                next != repeat -> {
                    error = true
                    message = strings.passwordMismatch
                }
                else -> {
                    busy = true
                    vm.changePassword(next) { ok ->
                        busy = false
                        error = !ok
                        message = if (ok) strings.passwordSaved else strings.loginFailed
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteAccountScreen(vm: JanaViewModel, strings: Strings, onBack: () -> Unit, onDeleted: () -> Unit) {
    var failed by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    Page(strings.deleteTitle, strings.back, onBack) {
        Text(strings.deleteBody, fontSize = 15.sp, lineHeight = 21.sp)
        if (failed) {
            Spacer(Modifier.height(10.dp))
            Text(strings.deleteFailed, color = JanaColors.Danger)
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton(strings.deleteAction, enabled = !busy) {
            busy = true
            vm.deleteAccount { ok ->
                busy = false
                if (ok) onDeleted() else failed = true
            }
        }
    }
}

@Composable
private fun Page(title: String, back: String, onBack: () -> Unit, body: @Composable () -> Unit) {
    Scaffold(containerColor = JanaColors.Background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = back)
                }
                Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(Modifier.height(8.dp))
            body()
        }
    }
}

@Composable
private fun Labeled(label: String, value: String) {
    Text(label, color = JanaColors.Muted, fontSize = 13.sp)
    Text(value, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun Field(label: String, value: String, secret: Boolean = false, onChange: (String) -> Unit) {
    Text(label, color = JanaColors.Muted, fontSize = 13.sp)
    Spacer(Modifier.height(6.dp))
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        visualTransformation = if (secret) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = JanaColors.Text),
        cursorBrush = SolidColor(JanaColors.Green),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(14.dp),
    )
}
