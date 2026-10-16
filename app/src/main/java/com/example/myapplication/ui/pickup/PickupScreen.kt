package com.example.myapplication.ui.pickup

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.JanaViewModel
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.i18n.Strings
import com.example.myapplication.ui.theme.JanaColors

@Composable
fun PickupScreen(vm: JanaViewModel, strings: Strings) {
    var shown by remember { mutableStateOf(false) }
    val code = vm.warehouse.userId
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Text(strings.pickupTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(28.dp))
        if (shown) {
            CodePattern(code, Modifier.size(220.dp))
            Spacer(Modifier.height(12.dp))
            Text(code, fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = 1.sp)
        } else {
            Text(
                strings.pickupBody,
                textAlign = TextAlign.Center,
                color = JanaColors.Muted,
                fontSize = 15.sp,
                lineHeight = 21.sp,
            )
        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton(if (shown) strings.pickupHide else strings.pickupShow, onClick = { shown = !shown })
    }
}

@Composable
private fun CodePattern(seed: String, modifier: Modifier) {
    val bits = remember(seed) {
        val hash = seed.fold(17) { acc, c -> acc * 31 + c.code }
        List(21 * 21) { index ->
            val mixed = hash xor (index * 997)
            mixed % 3 != 0
        }
    }
    Canvas(modifier) {
        val cells = 21
        val cell = size.width / cells
        drawRect(Color.White, size = size)
        bits.forEachIndexed { index, on ->
            if (!on) return@forEachIndexed
            val x = index % cells
            val y = index / cells
            drawRect(
                color = Color(0xFF1A1A1A),
                topLeft = Offset(x * cell, y * cell),
                size = Size(cell, cell),
            )
        }
    }
}
