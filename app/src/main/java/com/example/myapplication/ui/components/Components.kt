package com.example.myapplication.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.myapplication.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.AppLang
import com.example.myapplication.data.MainTab
import com.example.myapplication.ui.i18n.Strings
import com.example.myapplication.ui.theme.JanaColors

fun copyText(context: Context, label: String, value: String, toast: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
    }
}

fun showToast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

@Composable
fun JanaLogo(compact: Boolean, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_jana_logo),
        contentDescription = "JAŃA POST",
        modifier = modifier
            .height(if (compact) 32.dp else 48.dp)
            .aspectRatio(1024f / 460f),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun LogoBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, JanaColors.Line, RoundedCornerShape(18.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_jana_logo),
            contentDescription = "JAŃA POST",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun LanguageChip(language: AppLang, onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, JanaColors.Line, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(language.flag, fontSize = 16.sp)
        Spacer(Modifier.width(6.dp))
        Text(language.nativeName, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = JanaColors.Text)
        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = JanaColors.Muted)
    }
}

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = JanaColors.Green,
            contentColor = Color.White,
            disabledContainerColor = JanaColors.Green.copy(alpha = 0.45f),
            disabledContentColor = Color.White,
        ),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
fun BalanceChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(JanaColors.Green)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        Text("●", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp)
    }
}

@Composable
fun JanaBottomBar(
    tab: MainTab,
    strings: Strings,
    onTab: (MainTab) -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(8.dp)
            .background(Color.White)
            .navigationBarsPadding()
            .height(84.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(84.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TabItem(MainTab.HOME, strings.tabHome, Icons.Outlined.Home, tab, onTab)
            TabItem(MainTab.ORDER, strings.tabOrder, Icons.Outlined.Inventory2, tab, onTab)
            PickupItem(strings.tabPickup, tab == MainTab.PICKUP) { onTab(MainTab.PICKUP) }
            TabItem(MainTab.MESSAGE, strings.tabMessage, Icons.Outlined.ChatBubbleOutline, tab, onTab)
            TabItem(MainTab.PROFILE, strings.tabProfile, Icons.Outlined.Person, tab, onTab)
        }
    }
}

@Composable
private fun TabItem(
    value: MainTab,
    label: String,
    icon: ImageVector,
    selected: MainTab,
    onTab: (MainTab) -> Unit,
) {
    val active = value == selected
    val tint = if (active) JanaColors.Green else JanaColors.Muted
    Column(
        Modifier
            .clickable(role = Role.Tab, onClick = { onTab(value) })
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, color = tint, fontSize = 11.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun PickupItem(label: String, active: Boolean, onClick: () -> Unit) {
    Column(
        Modifier
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(58.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(JanaColors.Green),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.QrCode2, contentDescription = label, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            color = if (active) JanaColors.Green else JanaColors.Muted,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ChoiceSheet(
    visible: Boolean,
    title: String,
    options: List<T>,
    isSelected: (T) -> Boolean,
    label: (T) -> String,
    leading: @Composable (T) -> Unit,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.navigationBarsPadding()) {
            Text(
                title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp, bottom = 12.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = JanaColors.Text,
            )
            options.forEachIndexed { index, option ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(option)
                            onDismiss()
                        }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    leading(option)
                    Spacer(Modifier.width(14.dp))
                    Text(label(option), modifier = Modifier.weight(1f), fontSize = 16.sp, color = JanaColors.Text)
                    if (isSelected(option)) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = JanaColors.Green)
                    }
                }
                if (index != options.lastIndex) {
                    HorizontalDivider(color = JanaColors.Line, modifier = Modifier.padding(start = 68.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun AgreeBox(checked: Boolean, onChecked: (Boolean) -> Unit) {
    Box(
        Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (checked) JanaColors.Green else Color.Transparent)
            .border(1.5.dp, if (checked) JanaColors.Green else JanaColors.Line, RoundedCornerShape(6.dp))
            .clickable { onChecked(!checked) },
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}
