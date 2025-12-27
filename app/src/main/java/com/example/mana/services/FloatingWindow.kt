package com.example.mana.services

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mana.ui.chat.ChatScreen
import com.example.mana.ui.clipboard.ClipboardViewModel
import com.example.mana.ui.home.widgets.DailyOmenCard
import com.example.mana.ui.home.widgets.DailyOmenViewModel
import com.example.mana.ui.home.widgets.HafezOmenCard
import com.example.mana.ui.home.widgets.QuoteCard
import com.example.mana.ui.tasks.TasksTab
import com.example.mana.ui.theme.ManaTheme

@Composable
fun FloatingWindow(
    onClose: () -> Unit,
    copiedText: String?,
    onCopiedTextConsumed: () -> Unit
) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("داشبورد", "چت", "وظایف")

    Card(
        modifier = Modifier.fillMaxSize().padding(vertical = 48.dp, horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(visible = copiedText != null) {
                if (copiedText != null) {
                     val clipboardViewModel: ClipboardViewModel = viewModel()
                     val aiState by clipboardViewModel.uiState.collectAsState()

                    ClipboardSuggestionCard(
                        text = copiedText,
                        aiState = aiState,
                        onProcess = { clipboardViewModel.processText(copiedText) },
                        onDismiss = onCopiedTextConsumed
                    )
                }
            }

            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(text = { Text(title) },
                        selected = tabIndex == index,
                        onClick = { tabIndex = index })
                }
            }
            when (tabIndex) {
                0 -> DashboardTab()
                1 -> ChatScreen()
                2 -> TasksTab() // New Tasks Tab
            }
        }
    }
}

@Composable
fun ClipboardSuggestionCard(
    text: String,
    aiState: com.example.mana.ui.clipboard.ClipboardAiState,
    onProcess: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("متن کپی شده:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(text, maxLines = 2, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))

            if (aiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (aiState.suggestion.isNotBlank()) {
                 Text("پیشنهاد مانا: ${aiState.suggestion}", style = MaterialTheme.typography.bodyLarge)
            } else {
                 Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("نادیده گرفتن") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onProcess) { Text("پردازش کن") }
                }
            }
        }
    }
}

@Composable
fun DashboardTab() {
    val dailyOmenViewModel: DailyOmenViewModel = viewModel()
    val dailyOmenState by dailyOmenViewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { 
            DailyOmenCard(
                uiState = dailyOmenState,
                onUpdate = { dailyOmenViewModel.fetchDailyOmen() }
            ) 
        }
        item { HafezOmenCard() }
        item { QuoteCard() }
    }
}

@Preview
@Composable
fun FloatingWindowPreview() {
    ManaTheme {
        FloatingWindow(onClose = {}, copiedText = null, onCopiedTextConsumed = {})
    }
}
