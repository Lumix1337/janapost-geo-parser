package com.example.myapplication.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.AppLang
import com.example.myapplication.data.Countries
import com.example.myapplication.data.formatPartial
import com.example.myapplication.data.formatSent
import com.example.myapplication.data.parseRoutePhone
import com.example.myapplication.ui.JanaViewModel
import com.example.myapplication.ui.components.AgreeBox
import com.example.myapplication.ui.components.ChoiceSheet
import com.example.myapplication.ui.components.JanaLogo
import com.example.myapplication.ui.components.LanguageChip
import com.example.myapplication.ui.components.LogoBadge
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.showToast
import com.example.myapplication.ui.i18n.Strings
import com.example.myapplication.ui.i18n.countryName
import com.example.myapplication.ui.theme.JanaColors
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    vm: JanaViewModel,
    strings: Strings,
    onContinue: (dial: String, national: String) -> Unit,
    onTerms: () -> Unit,
) {
    val context = LocalContext.current
    var country by remember { mutableStateOf(Countries.first()) }
    var digits by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(true) }
    var languageOpen by remember { mutableStateOf(false) }
    var countryOpen by remember { mutableStateOf(false) }
    val complete = digits.length == country.digits

    fun submit() {
        when {
            !complete -> showToast(context, strings.needPhone)
            !agreed -> showToast(context, strings.needTerms)
            else -> onContinue(country.dial, digits)
        }
    }

    Scaffold(containerColor = JanaColors.AuthBackground) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                JanaLogo(compact = true)
                LanguageChip(vm.language) { languageOpen = true }
            }
            Spacer(Modifier.weight(1f))
            Text(
                strings.welcome,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = JanaColors.Text,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                strings.enterPhone,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = JanaColors.Text,
            )
            Spacer(Modifier.height(28.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(JanaColors.Chip)
                        .clickable { countryOpen = true }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(country.flag, fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(country.dial, fontWeight = FontWeight.SemiBold, color = JanaColors.Text)
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = JanaColors.Muted)
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(JanaColors.Line),
                )
                Spacer(Modifier.width(12.dp))
                BasicTextField(
                    value = digits,
                    onValueChange = { digits = it.filter(Char::isDigit).take(country.digits) },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 16.sp,
                        color = JanaColors.Text,
                    ),
                    cursorBrush = SolidColor(JanaColors.Green),
                    visualTransformation = phoneMask(country.dial),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        Box {
                            if (digits.isEmpty()) {
                                Text(strings.phoneHint, color = JanaColors.Muted, fontSize = 16.sp)
                            }
                            inner()
                        }
                    },
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(JanaColors.Line),
            )
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.Top) {
                AgreeBox(agreed) { agreed = it }
                Spacer(Modifier.width(10.dp))
                Text(
                    buildAnnotatedString {
                        append(strings.agreePrefix)
                        withStyle(SpanStyle(color = JanaColors.Green, fontWeight = FontWeight.SemiBold)) {
                            append(strings.agreeTerms)
                        }
                        append(strings.agreeMid)
                        withStyle(SpanStyle(color = JanaColors.Green, fontWeight = FontWeight.SemiBold)) {
                            append(strings.agreeConditions)
                        }
                        append(strings.agreeEnd)
                    },
                    modifier = Modifier.clickable(onClick = onTerms),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = JanaColors.Text,
                )
            }
            AnimatedVisibility(visible = complete) {
                Column {
                    Spacer(Modifier.height(22.dp))
                    PrimaryButton(strings.continueNext, onClick = { submit() })
                }
            }
            Spacer(Modifier.weight(1.15f))
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
        visible = countryOpen,
        title = strings.phone,
        options = Countries,
        isSelected = { it.id == country.id },
        label = { "${strings.countryName(it.id)}  ${it.dial}" },
        leading = { Text(it.flag, fontSize = 22.sp) },
        onSelect = {
            country = it
            digits = digits.take(it.digits)
        },
        onDismiss = { countryOpen = false },
    )
}

@Composable
fun CodeScreen(
    vm: JanaViewModel,
    strings: Strings,
    routePhone: String,
    onBack: () -> Unit,
    onVerified: () -> Unit,
) {
    val context = LocalContext.current
    val session = remember(routePhone) { parseRoutePhone(routePhone) }
    var code by remember { mutableStateOf("") }
    var seconds by remember { mutableIntStateOf(59) }
    var submitting by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }

    LaunchedEffect(Unit) { focus.requestFocus() }
    LaunchedEffect(seconds) {
        if (seconds > 0) {
            delay(1000)
            seconds -= 1
        }
    }
    LaunchedEffect(code) {
        if (code.length == 6 && !submitting) {
            submitting = true
            vm.verifyOtp(session.dial, session.national, code) { ok ->
                if (ok) onVerified() else {
                    submitting = false
                    code = ""
                }
            }
        }
    }

    Scaffold(containerColor = JanaColors.AuthBackground) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 24.dp),
        ) {
            Box(Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                }
                LogoBadge(Modifier.align(Alignment.Center))
            }
            Spacer(Modifier.height(36.dp))
            Text(
                strings.codeTitle,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                strings.codeBody.format(formatSent(session.dial, session.national)),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                color = JanaColors.Text,
            )
            Spacer(Modifier.height(28.dp))
            BasicTextField(
                value = code,
                onValueChange = { code = it.filter(Char::isDigit).take(6) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focus),
                decorationBox = { inner ->
                    Box(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            repeat(6) { index ->
                                val char = code.getOrNull(index)?.toString().orEmpty()
                                val active = index == code.length.coerceAtMost(5)
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (active) Color.White else JanaColors.Field)
                                        .border(
                                            width = if (active) 1.5.dp else 0.dp,
                                            color = if (active) JanaColors.Green else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp),
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(char, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Box(Modifier.alpha(0f)) { inner() }
                    }
                },
            )
            Spacer(Modifier.height(22.dp))
            if (seconds > 0) {
                val clock = "%02d:%02d".format(seconds / 60, seconds % 60)
                Text(
                    strings.resendIn.format(clock),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = JanaColors.Muted,
                    fontSize = 14.sp,
                )
            } else {
                Text(
                    strings.resend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            seconds = 59
                            code = ""
                            submitting = false
                            vm.requestOtp(session.dial, session.national) {
                                showToast(context, strings.codeResent)
                            }
                        },
                    textAlign = TextAlign.Center,
                    color = JanaColors.Green,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
fun PasswordScreen(
    vm: JanaViewModel,
    strings: Strings,
    routePhone: String,
    onBack: () -> Unit,
    onLoggedIn: () -> Unit,
) {
    val session = remember(routePhone) { parseRoutePhone(routePhone) }
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    fun submit() {
        when {
            busy -> return
            password.length < 6 -> errorText = strings.passwordTooShort
            else -> {
                errorText = null
                busy = true
                vm.login(session.dial, session.national, password) { ok ->
                    busy = false
                    if (ok) onLoggedIn() else errorText = strings.loginFailed
                }
            }
        }
    }

    Scaffold(containerColor = JanaColors.AuthBackground) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 24.dp),
        ) {
            Box(Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                }
                JanaLogo(compact = false, modifier = Modifier.align(Alignment.Center))
            }
            Spacer(Modifier.weight(1f))
            Text(
                strings.passwordTitle,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
            )
            Spacer(Modifier.height(28.dp))
            Text(strings.passwordLabel, fontSize = 14.sp, color = JanaColors.Text)
            Spacer(Modifier.height(6.dp))
            BasicTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorText = null
                },
                singleLine = true,
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, color = JanaColors.Text),
                cursorBrush = SolidColor(JanaColors.Green),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) { inner() }
                        if (password.isNotEmpty()) {
                            IconButton(onClick = { password = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = null, tint = JanaColors.Muted)
                            }
                        }
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = JanaColors.Muted,
                            )
                        }
                    }
                },
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(if (errorText != null) JanaColors.Danger else JanaColors.Line),
            )
            if (errorText != null) {
                Spacer(Modifier.height(6.dp))
                Text(errorText.orEmpty(), color = JanaColors.Danger, fontSize = 13.sp)
            }
            Spacer(Modifier.height(22.dp))
            PrimaryButton(strings.login, onClick = { submit() }, enabled = !busy)
            Spacer(Modifier.weight(1.2f))
        }
    }
}

@Composable
fun TermsScreen(strings: Strings, onBack: () -> Unit) {
    Scaffold(containerColor = JanaColors.AuthBackground) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
            }
            Text(strings.termsTitle, fontWeight = FontWeight.Bold, fontSize = 26.sp)
            Spacer(Modifier.height(16.dp))
            Text(strings.termsBody, fontSize = 15.sp, lineHeight = 22.sp, color = JanaColors.Text)
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun phoneMask(dial: String): VisualTransformation {
    return VisualTransformation { text ->
        val formatted = formatPartial(dial, text.text)
        TransformedText(
            AnnotatedString(formatted),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = formatted.length
                override fun transformedToOriginal(offset: Int) = text.text.length
            },
        )
    }
}
