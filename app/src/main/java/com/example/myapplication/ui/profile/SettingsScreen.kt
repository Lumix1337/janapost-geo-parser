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
    val warehouse = vm.warehouse
    val address = if (english) warehouse.addressEn else warehouse.addressRu

    var rawInput by remember { mutableStateOf("город Алматы/Алматы қаласы, Мкр Аксай 3а, 59, кв 70") }
    var parsedResult by remember { mutableStateOf<com.example.myapplication.data.ParsedDeliveryAddress?>(null) }
    var addressSaved by remember { mutableStateOf(false) }

    Page(strings.myAddress, strings.back, onBack) {
        Labeled(strings.yourId, warehouse.userId)
        Labeled(strings.phone, warehouse.phone)
        Labeled(strings.showAddress, address)
        Spacer(Modifier.height(4.dp))
        Text(warehouse.addressLocal, color = JanaColors.Muted, fontSize = 14.sp)

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = JanaColors.Line)
        Spacer(Modifier.height(18.dp))

        Text("⚡ Умный адрес доставки (Jana Address)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(
            "Вставьте адрес одной строкой в свободной форме:",
            color = JanaColors.Muted,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(8.dp))

        Field("Адрес одной строкой", rawInput) { rawInput = it }
        Spacer(Modifier.height(12.dp))

        PrimaryButton("Разобрать адрес", enabled = rawInput.isNotBlank()) {
            try {
                parsedResult = com.example.myapplication.data.SmartAddressParser.parse(rawInput)
                addressSaved = false
            } catch (e: Throwable) {
                android.util.Log.e("AddressScreen", "Parsing crash prevented", e)
            }
        }

        parsedResult?.let { res ->
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(16.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Результат разбора", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(JanaColors.GreenSoft)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            "Уверенность: ${(res.confidence * 100).toInt()}%",
                            color = JanaColors.Green,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Labeled("Город / Область", "${res.city ?: "—"} (${res.province ?: "—"})")
                res.district?.let { Labeled("Район города (из КАТО)", it) }
                Labeled("Улица / Микрорайон", "${res.streetType ?: ""} ${res.streetName ?: "—"}")
                Labeled("Дом / Квартира", "дом: ${res.house ?: "—"}, кв: ${res.apartment ?: "—"}")
                if (res.entrance != null || res.floor != null) {
                    Labeled("Подъезд / Этаж", "подъезд: ${res.entrance ?: "—"}, этаж: ${res.floor ?: "—"}")
                }
                res.landmark?.let { Labeled("Ориентир", it) }

                Spacer(Modifier.height(8.dp))
                PrimaryButton(if (addressSaved) "✓ Адрес сохранен курьеру" else "Сохранить адрес для доставки") {
                    addressSaved = true
                }
            }
        }
    }
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
