package com.example.myapplication.ui.messages

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.JanaViewModel
import com.example.myapplication.ui.i18n.Strings
import com.example.myapplication.ui.theme.JanaColors

@Composable
fun MessagesScreen(strings: Strings, onOpenChat: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Box(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp), contentAlignment = Alignment.Center) {
            Text(strings.message, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .clickable(onClick = onOpenChat)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(JanaColors.Yellow),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(strings.support, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(strings.supportSub, color = JanaColors.Muted, fontSize = 13.sp)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = JanaColors.Muted)
        }
    }
}

@Composable
fun ChatScreen(vm: JanaViewModel, strings: Strings, onBack: () -> Unit) {
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) { vm.ensureGreeting(strings.greeting) }
    LaunchedEffect(vm.chat.size) {
        if (vm.chat.isNotEmpty()) listState.animateScrollToItem(vm.chat.lastIndex)
    }

    fun send() {
        val text = draft
        vm.sendChat(text, strings.reply) { ok ->
            if (ok) draft = ""
        }
    }

    Scaffold(containerColor = JanaColors.Background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                }
                Column {
                    Text(strings.support, fontWeight = FontWeight.Bold)
                    Text(strings.supportSub, color = JanaColors.Muted, fontSize = 12.sp)
                }
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(vm.chat) { line ->
                    val mine = !line.fromSupport
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                        Text(
                            line.text,
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (mine) JanaColors.Green else Color.White)
                                .padding(12.dp),
                            color = if (mine) Color.White else JanaColors.Text,
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                        )
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    textStyle = TextStyle(fontSize = 15.sp, color = JanaColors.Text),
                    cursorBrush = SolidColor(JanaColors.Green),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { send() }),
                    decorationBox = { inner ->
                        Box {
                            if (draft.isEmpty()) {
                                Text(strings.messageHint, color = JanaColors.Muted)
                            }
                            inner()
                        }
                    },
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { send() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(JanaColors.Green),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = strings.lostSend, tint = Color.White)
                }
            }
        }
    }
}
