package com.example.myapplication.ui.misc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.JanaViewModel
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.copyText
import com.example.myapplication.ui.components.showToast
import com.example.myapplication.ui.i18n.Strings
import com.example.myapplication.ui.theme.JanaColors

@Composable
fun TutorialScreen(strings: Strings, onBack: () -> Unit) {
    InfoColumn(strings.tutorials, strings.back, onBack) {
        Section(strings.tutAddress, strings.shopStep1 + "\n\n" + strings.shopStep3)
        Spacer(Modifier.height(16.dp))
        Section(strings.tutTrack, strings.trackBody)
    }
}

@Composable
fun WalletScreen(strings: Strings, onBack: () -> Unit) {
    val context = LocalContext.current
    InfoColumn(strings.walletTitle, strings.back, onBack) {
        Text(strings.balance, fontWeight = FontWeight.Bold, fontSize = 36.sp, color = JanaColors.Green)
        Spacer(Modifier.height(8.dp))
        Text(strings.walletEmpty, color = JanaColors.Muted)
        Spacer(Modifier.height(20.dp))
        PrimaryButton(strings.topUp) { showToast(context, strings.topUpLater) }
    }
}

@Composable
fun ReferralScreen(vm: JanaViewModel, strings: Strings, onBack: () -> Unit) {
    val context = LocalContext.current
    InfoColumn(strings.referralTitle, strings.back, onBack) {
        Text(strings.referralBody, fontSize = 15.sp, lineHeight = 21.sp)
        Spacer(Modifier.height(16.dp))
        Text(strings.yourCode, color = JanaColors.Muted, fontSize = 13.sp)
        Text(vm.warehouse.userId, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = JanaColors.Green)
        Spacer(Modifier.height(16.dp))
        PrimaryButton(strings.copyAll) {
            copyText(context, "code", vm.warehouse.userId, strings.copied)
        }
    }
}

@Composable
fun ShopScreen(marketId: String, strings: Strings, onBack: () -> Unit) {
    val name = when (marketId) {
        "pdd" -> "Pinduoduo"
        "temu" -> "Temu"
        "jd" -> "JD.com"
        "taobao" -> "Taobao"
        else -> marketId
    }
    InfoColumn(strings.shopTitle.format(name), strings.back, onBack) {
        Step("1", strings.shopStep1)
        Step("2", strings.shopStep2.format(name))
        Step("3", strings.shopStep3)
    }
}

@Composable
fun TextPage(title: String, body: String, back: String, onBack: () -> Unit) {
    InfoColumn(title, back, onBack) {
        Text(body, fontSize = 15.sp, lineHeight = 21.sp)
    }
}

@Composable
private fun InfoColumn(title: String, back: String, onBack: () -> Unit, body: @Composable () -> Unit) {
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
                Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            body()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Section(title: String, body: String) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(Modifier.height(6.dp))
    Text(body, fontSize = 15.sp, lineHeight = 21.sp)
}

@Composable
private fun Step(index: String, body: String) {
    Text(index, color = JanaColors.Green, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    Text(body, fontSize = 15.sp, lineHeight = 21.sp, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
}
