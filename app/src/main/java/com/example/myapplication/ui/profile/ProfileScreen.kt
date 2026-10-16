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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Branch
import com.example.myapplication.ui.JanaViewModel
import com.example.myapplication.ui.i18n.Strings
import com.example.myapplication.ui.theme.JanaColors

@Composable
fun ProfileScreen(
    vm: JanaViewModel,
    strings: Strings,
    onSettings: () -> Unit,
    onWallet: () -> Unit,
    onAddress: () -> Unit,
    onCollection: () -> Unit,
    onInvite: () -> Unit,
    onPromo: () -> Unit,
    onTariffs: () -> Unit,
    onLessons: () -> Unit,
    onFaq: () -> Unit,
) {
    var branchOpen by remember { mutableStateOf(false) }
    val branchLabel = if (vm.branch == Branch.NONE) strings.selectBranch else vm.branch.label(strings)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier
                    .weight(1f)
                    .clickable { branchOpen = true },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    vm.session?.formatted ?: "—",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                )
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = JanaColors.Muted)
            }
            LanguageChip(vm.language.chip(), vm::chooseLanguage, strings)
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable(onClick = onSettings),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Settings, contentDescription = strings.settingsTitle, tint = JanaColors.Text)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryCard(
                title = strings.walletCard,
                value = strings.zeroDollar,
                caption = strings.zeroTenge,
                color = Color(0xFF2F6BFF),
                icon = Icons.Filled.AccountBalanceWallet,
                modifier = Modifier.weight(1f),
                onClick = onWallet,
            )
            SummaryCard(
                title = strings.bonuses,
                value = strings.bonusValue,
                caption = strings.zeroTenge,
                color = Color(0xFF7A3FF2),
                icon = Icons.Outlined.Payments,
                modifier = Modifier.weight(1f),
                rate = strings.bonusRate,
                onClick = onWallet,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFFE2B340), Color(0xFF8C6A1E))))
                .clickable { branchOpen = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(strings.personalData, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(branchLabel, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        MenuCard(
            listOf(
                MenuItem(Icons.Outlined.Place, strings.myAddress, onAddress),
                MenuItem(Icons.Outlined.FavoriteBorder, strings.collection, onCollection),
            ),
        )
        Spacer(Modifier.height(12.dp))
        MenuCard(
            listOf(
                MenuItem(Icons.Outlined.Groups, strings.inviteFriend, onInvite),
                MenuItem(Icons.Outlined.CardGiftcard, strings.promo, onPromo),
            ),
        )
        Spacer(Modifier.height(12.dp))
        MenuCard(
            listOf(
                MenuItem(Icons.Outlined.Payments, strings.tariffs, onTariffs),
                MenuItem(Icons.Outlined.MenuBook, strings.lessons, onLessons),
                MenuItem(Icons.Outlined.Article, strings.faq, onFaq),
            ),
        )
        Spacer(Modifier.height(12.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(strings.socialTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                SocialDot(Color(0xFFE1306C), "IG")
                SocialDot(Color(0xFF111111), "♪")
                SocialDot(Color(0xFFFF0000), "▶")
                SocialDot(Color(0xFF2AABEE), "✈")
            }
        }
        Spacer(Modifier.height(20.dp))
    }
    com.example.myapplication.ui.components.ChoiceSheet(
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

@Composable
private fun LanguageChip(label: String, onPick: (com.example.myapplication.data.AppLang) -> Unit, strings: Strings) {
    var open by remember { mutableStateOf(false) }
    Box(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { open = true }
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
    com.example.myapplication.ui.components.ChoiceSheet(
        visible = open,
        title = strings.chooseLanguage,
        options = com.example.myapplication.data.AppLang.entries,
        isSelected = { it.chip() == label },
        label = { it.nativeName },
        leading = { Text(it.flag, fontSize = 22.sp) },
        onSelect = onPick,
        onDismiss = { open = false },
    )
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    caption: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier,
    rate: String? = null,
    onClick: () -> Unit,
) {
    Column(
        modifier
            .height(108.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), fontSize = 14.sp)
            if (rate != null) {
                Text(rate, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(caption, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
    }
}

private data class MenuItem(val icon: ImageVector, val title: String, val onClick: () -> Unit)

@Composable
private fun MenuCard(items: List<MenuItem>) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White),
    ) {
        items.forEachIndexed { index, item ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = item.onClick)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(item.icon, contentDescription = null, tint = JanaColors.Green, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(item.title, modifier = Modifier.weight(1f), fontSize = 16.sp)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = JanaColors.Muted)
            }
            if (index != items.lastIndex) {
                HorizontalDivider(color = JanaColors.Line, modifier = Modifier.padding(start = 50.dp))
            }
        }
    }
}

@Composable
private fun SocialDot(color: Color, mark: String) {
    Box(
        Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(mark, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
    }
}

internal fun Branch.label(strings: Strings): String = when (this) {
    Branch.NONE -> strings.branchNone
    Branch.ALMATY -> strings.almaty
    Branch.ASTANA -> strings.astana
    Branch.SHYMKENT -> strings.shymkent
    Branch.AKTOBE -> strings.aktobe
}
