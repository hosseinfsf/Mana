package com.example.mana.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatScreen(chatViewModel: ChatViewModel = viewModel()) {
    val uiState by chatViewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(16.dp),
            reverseLayout = true
        ) {
            items(uiState.messages.reversed()) { msg ->
                MessageBubble(message = msg)
            }
        }

        ChatInputToolbar(chatViewModel, messageText) { newMessage -> messageText = newMessage }

        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text("پیام شما...") },
                modifier = Modifier.weight(1f),
                enabled = !uiState.isModifyingText
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if (messageText.isNotBlank()) {
                    chatViewModel.sendMessage(messageText)
                    messageText = ""
                }
            }) {
                if(uiState.isModifyingText) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                     Icon(Icons.Default.Send, contentDescription = "ارسال")
                }
            }
        }
    }
}

@Composable
fun ChatInputToolbar(viewModel: ChatViewModel, text: String, onResult: (String) -> Unit) {
    Row(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Button(onClick = { viewModel.modifyText(text, "Improve this text", onResult) }) { Text("بهبود") }
        Button(onClick = { viewModel.modifyText(text, "Make this text shorter", onResult) }) { Text("کوتاه کن") }
        Button(onClick = { viewModel.modifyText(text, "Change the tone to formal", onResult) }) { Text("رسمی کن") }
    }
}


@Composable
fun MessageBubble(message: ChatMessage) {
    val isUserMessage = message.participant == Participant.USER
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isUserMessage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            if (message.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    text = message.text,
                    color = if (isUserMessage) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
