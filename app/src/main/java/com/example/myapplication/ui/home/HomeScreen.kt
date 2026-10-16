package com.example.myapplication.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.AppLang
import com.example.myapplication.data.Branch
import com.example.myapplication.ui.JanaViewModel
import com.example.myapplication.ui.components.BalanceChip
import com.example.myapplication.ui.components.ChoiceSheet
import com.example.myapplication.ui.components.JanaLogo
import com.example.myapplication.ui.components.copyText
import com.example.myapplication.ui.i18n.Strings
import com.example.myapplication.ui.theme.JanaColors

private data class Market(val id: String, val name: String, val mark: String, val color: Color, val top: Boolean)

private val Markets = listOf(
    Market("pdd", "Pinduoduo", "拼", Color(0xFFE23A2E), true),
    Market("temu", "Temu", "T", Color(0xFFF57224), true),
    Market("jd", "JD.com", "JD", Color(0xFFE1251B), false),
    Market("taobao", "Taobao", "淘", Color(0xFFFF6A00), false),
)

@Composable
fun HomeScreen(
    vm: JanaViewModel,
    strings: Strings,
    language: AppLang,
    onWallet: () -> Unit,
    onTutorial: () -> Unit,
    onReferral: () -> Unit,
    onMarket: (String) -> Unit,
) {
    val context = LocalContext.current
    var branchOpen by remember { mutableStateOf(false) }
    var addressOpen by remember { mutableStateOf(false) }
    val warehouse = vm.warehouse
    val address = if (language.resolved() == AppLang.EN) warehouse.addressEn else warehouse.addressRu
    val pager = rememberPagerState(pageCount = { 2 })

    fun branchName(branch: Branch) = when (branch) {
        Branch.NONE -> strings.branchNone
        Branch.ALMATY -> strings.almaty
        Branch.ASTANA -> strings.astana
        Branch.SHYMKENT -> strings.shymkent
        Branch.AKTOBE -> strings.aktobe
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            JanaLogo(compact = true)
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(strings.branchLabel, fontSize = 12.sp, color = JanaColors.Muted)
                Row(
                    Modifier.clickable { branchOpen = true },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(branchName(vm.branch), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = JanaColors.Muted)
                }
            }
            BalanceChip(strings.balance, onWallet)
        }
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(strings.deliveryTitle, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 26.sp)
                Spacer(Modifier.height(4.dp))
                Text(strings.deliverySubtitle, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            DeliveryArt(Modifier.size(116.dp))
        }
        Spacer(Modifier.height(8.dp))
        Box {
            Row(
                Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(JanaColors.GreenMint)
                    .clickable(onClick = onTutorial)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(JanaColors.Green),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    strings.howTo,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                strings.recommended,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(12.dp))
                    .background(JanaColors.Green)
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(strings.warehouse, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(JanaColors.Blue)
                        .clickable {
                            copyText(
                                context,
                                "warehouse",
                                "ID: ${warehouse.userId}\n${strings.phone}: ${warehouse.phone}\n$address\n${warehouse.addressLocal}",
                                strings.copied,
                            )
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(strings.copyAll, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            CopyRow(strings.yourId, warehouse.userId) {
                copyText(context, "id", warehouse.userId, strings.copied)
            }
            Spacer(Modifier.height(10.dp))
            CopyRow(strings.phone, warehouse.phone) {
                copyText(context, "phone", warehouse.phone, strings.copied)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { addressOpen = !addressOpen },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (addressOpen) strings.hideAddress else warehouse.addressLocal,
                    modifier = Modifier.weight(1f),
                    color = JanaColors.Muted,
                    fontSize = 13.sp,
                    maxLines = if (addressOpen) 4 else 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    if (addressOpen) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = JanaColors.Muted,
                )
            }
            if (addressOpen) {
                Spacer(Modifier.height(6.dp))
                Text(address, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Markets.forEach { market ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onMarket(market.id) }) {
                    Box {
                        Box(
                            Modifier
                                .size(62.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(market.color),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(market.mark, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                        if (market.top) {
                            Text(
                                "TOP",
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = (-8).dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFF4D3A))
                                    .padding(horizontal = 6.dp, vertical = 1.dp),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(market.name, fontSize = 12.sp, color = JanaColors.Text)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        HorizontalPager(state = pager, contentPadding = PaddingValues(end = 28.dp), pageSpacing = 10.dp) { page ->
            if (page == 0) {
                InviteBanner(strings, onReferral)
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(JanaColors.Green)
                        .clickable(onClick = onTutorial)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(strings.bannerHint, color = Color.White, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(2) { index ->
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (pager.currentPage == index) 7.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (pager.currentPage == index) JanaColors.Green else JanaColors.Line),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(strings.tutorials, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(listOf(strings.tutAddress to JanaColors.Green, strings.tutTrack to Color(0xFFF5A524))) { (title, color) ->
                Box(
                    Modifier
                        .size(width = 150.dp, height = 92.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(color)
                        .clickable(onClick = onTutorial)
                        .padding(12.dp),
                ) {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }

    ChoiceSheet(
        visible = branchOpen,
        title = strings.branchLabel,
        options = Branch.entries,
        isSelected = { it == vm.branch },
        label = { branchName(it) },
        leading = {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(JanaColors.GreenSoft),
            )
        },
        onSelect = vm::chooseBranch,
        onDismiss = { branchOpen = false },
    )
}

@Composable
private fun CopyRow(label: String, value: String, onCopy: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = JanaColors.Muted, modifier = Modifier.weight(1f), fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.Filled.ContentCopy,
            contentDescription = label,
            tint = JanaColors.Muted,
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onCopy),
        )
    }
}

@Composable
private fun InviteBanner(strings: Strings, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(112.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFF6EE))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            strings.invite,
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp, end = 6.dp),
            color = JanaColors.Green,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            lineHeight = 19.sp,
        )
        PeopleArt(Modifier.size(78.dp))
        Box(
            Modifier
                .padding(8.dp)
                .width(78.dp)
                .height(96.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(JanaColors.Green)
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(strings.inviteCta, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun DeliveryArt(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawCircle(Color(0xFFF3D2B5), radius = w * 0.12f, center = Offset(w * 0.62f, h * 0.72f))
        drawRoundRect(
            color = Color(0xFFF3D2B5),
            topLeft = Offset(w * 0.5f, h * 0.72f),
            size = Size(w * 0.28f, h * 0.16f),
            cornerRadius = CornerRadius(18f, 18f),
        )
        val lid = Path().apply {
            moveTo(w * 0.18f, h * 0.48f)
            lineTo(w * 0.55f, h * 0.32f)
            lineTo(w * 0.86f, h * 0.46f)
            lineTo(w * 0.48f, h * 0.62f)
            close()
        }
        drawPath(lid, JanaColors.Green)
        val face = Path().apply {
            moveTo(w * 0.18f, h * 0.48f)
            lineTo(w * 0.48f, h * 0.62f)
            lineTo(w * 0.48f, h * 0.86f)
            lineTo(w * 0.18f, h * 0.72f)
            close()
        }
        drawPath(face, Color(0xFF127214))
        val side = Path().apply {
            moveTo(w * 0.48f, h * 0.62f)
            lineTo(w * 0.86f, h * 0.46f)
            lineTo(w * 0.86f, h * 0.7f)
            lineTo(w * 0.48f, h * 0.86f)
            close()
        }
        drawPath(side, Color(0xFF3CB44A))
        drawRoundRect(Color(0xFFF6E7C1), Offset(w * 0.22f, h * 0.18f), Size(w * 0.22f, h * 0.2f), CornerRadius(12f, 12f))
        drawRoundRect(Color(0xFF222222), Offset(w * 0.48f, h * 0.12f), Size(w * 0.12f, h * 0.2f), CornerRadius(8f, 8f))
        drawCircle(Color(0xFF5C6BC0), radius = w * 0.07f, center = Offset(w * 0.7f, h * 0.2f))
    }
}

@Composable
private fun PeopleArt(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawCircle(Color(0xFFF0B429), radius = w * 0.16f, center = Offset(w * 0.32f, h * 0.28f))
        drawCircle(Color(0xFF188A18), radius = w * 0.16f, center = Offset(w * 0.68f, h * 0.3f))
        drawRoundRect(Color(0xFFF5A524), Offset(w * 0.12f, h * 0.42f), Size(w * 0.4f, h * 0.48f), CornerRadius(30f, 30f))
        drawRoundRect(Color(0xFF188A18), Offset(w * 0.48f, h * 0.44f), Size(w * 0.4f, h * 0.48f), CornerRadius(30f, 30f))
    }
}
