package com.example.myapplication.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Warehouse
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.AppLang
import com.example.myapplication.data.Direction
import com.example.myapplication.ui.JanaViewModel
import com.example.myapplication.ui.components.BalanceChip
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.showToast
import com.example.myapplication.ui.i18n.Strings
import com.example.myapplication.ui.i18n.statusName
import com.example.myapplication.ui.theme.JanaColors
import androidx.compose.ui.platform.LocalContext

private data class StatusRow(val id: String, val icon: ImageVector)

@Composable
fun OrdersScreen(
    vm: JanaViewModel,
    strings: Strings,
    onWallet: () -> Unit,
    onStatus: (Direction, String) -> Unit,
    onLost: () -> Unit,
) {
    var direction by remember { mutableStateOf(Direction.RECEIVE) }
    val groups = if (direction == Direction.RECEIVE) receiveGroups() else sendGroups()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        BoxHeader(strings.order, strings, onWallet)
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(4.dp),
        ) {
            Segment(strings.receive, direction == Direction.RECEIVE, Modifier.weight(1f)) {
                direction = Direction.RECEIVE
            }
            Segment(strings.send, direction == Direction.SEND, Modifier.weight(1f)) {
                direction = Direction.SEND
            }
        }
        Spacer(Modifier.height(12.dp))
        groups.forEach { group ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White),
            ) {
                group.forEachIndexed { index, row ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (row.id == "lost") onLost() else onStatus(direction, row.id)
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(row.icon, contentDescription = null, tint = JanaColors.Green, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(strings.statusName(row.id), modifier = Modifier.weight(1f), fontSize = 16.sp)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = JanaColors.Muted)
                    }
                    if (index != group.lastIndex) {
                        HorizontalDivider(color = JanaColors.Line, modifier = Modifier.padding(start = 50.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun OrderListScreen(
    vm: JanaViewModel,
    strings: Strings,
    language: AppLang,
    direction: Direction,
    statusId: String,
    onBack: () -> Unit,
) {
    val english = language.resolved() == AppLang.EN
    val parcels = vm.parcels(direction, statusId)
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
                Text(strings.statusName(statusId), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            if (parcels.isEmpty()) {
                Spacer(Modifier.height(48.dp))
                Text(strings.emptyTitle, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text(strings.emptyBody, color = JanaColors.Muted, fontSize = 15.sp)
            } else {
                parcels.forEach { parcel ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .padding(16.dp),
                    ) {
                        Text(parcel.tracking, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(if (english) parcel.titleEn else parcel.titleRu, fontSize = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${parcel.marketplace} · ${if (english) parcel.weightEn else parcel.weightRu}",
                            color = JanaColors.Muted,
                            fontSize = 13.sp,
                        )
                        Text(
                            if (english) parcel.updatedEn else parcel.updatedRu,
                            color = JanaColors.Muted,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun LostItemScreen(strings: Strings, onBack: () -> Unit) {
    val context = LocalContext.current
    var tracking by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    Scaffold(containerColor = JanaColors.Background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                }
                Text(strings.lostTitle, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(Modifier.height(12.dp))
            Field(strings.lostTracking, tracking) { tracking = it }
            Spacer(Modifier.height(12.dp))
            Field(strings.lostNote, note) { note = it }
            Spacer(Modifier.height(20.dp))
            PrimaryButton(strings.lostSend, enabled = tracking.isNotBlank()) {
                showToast(context, strings.lostDone)
                onBack()
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    Text(label, fontSize = 13.sp, color = JanaColors.Muted)
    Spacer(Modifier.height(6.dp))
    BasicTextField(
        value = value,
        onValueChange = onChange,
        textStyle = TextStyle(fontSize = 16.sp, color = JanaColors.Text),
        cursorBrush = SolidColor(JanaColors.Green),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(14.dp),
    )
}

@Composable
private fun BoxHeader(title: String, strings: Strings, onWallet: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 12.dp),
    ) {
        Text(
            title,
            modifier = Modifier.align(Alignment.Center),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
        BalanceChip(strings.balance, onWallet, Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
private fun Segment(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text,
            color = if (selected) JanaColors.Green else JanaColors.Muted,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        BoxLine(selected)
    }
}

@Composable
private fun BoxLine(selected: Boolean) {
    Spacer(
        Modifier
            .width(36.dp)
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(if (selected) JanaColors.Green else Color.Transparent),
    )
}

private fun receiveGroups() = listOf(
    listOf(
        StatusRow("pending", Icons.Filled.AccessTime),
        StatusRow("in_stock", Icons.Filled.Warehouse),
        StatusRow("on_road", Icons.Filled.RadioButtonChecked),
        StatusRow("sorting", Icons.Filled.Inventory2),
        StatusRow("send_branch", Icons.Filled.LocalShipping),
        StatusRow("branch", Icons.Filled.Apartment),
        StatusRow("at_courier", Icons.Filled.Person),
    ),
    listOf(StatusRow("received", Icons.Filled.Inventory2)),
    listOf(StatusRow("lost", Icons.AutoMirrored.Filled.HelpOutline)),
)

private fun sendGroups() = listOf(
    listOf(
        StatusRow("pending", Icons.Filled.AccessTime),
        StatusRow("accepted", Icons.Filled.Warehouse),
        StatusRow("sorting", Icons.Filled.Inventory2),
        StatusRow("send_branch", Icons.Filled.Autorenew),
        StatusRow("branch", Icons.Filled.Apartment),
    ),
    listOf(StatusRow("received", Icons.Filled.Inventory2)),
    listOf(StatusRow("lost", Icons.AutoMirrored.Filled.HelpOutline)),
)
