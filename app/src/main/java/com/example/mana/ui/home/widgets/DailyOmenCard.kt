package com.example.mana.ui.home.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mana.ui.theme.ManaTheme

@Composable
fun DailyOmenCard(
    uiState: DailyOmenUiState,
    onUpdate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔮 فال روزانه شما 🔮",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            } else {
                Text(
                    text = uiState.omen,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.heightIn(min = 60.dp) // Ensure minimum height
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آخرین بروزرسانی: ${uiState.lastUpdated}",
                    style = MaterialTheme.typography.labelSmall
                )
                TextButton(onClick = onUpdate, enabled = !uiState.isLoading) {
                    Text("بروزرسانی")
                }
            }
        }
    }
}

data class DailyOmenUiState(
    val omen: String = "",
    val isLoading: Boolean = false,
    val lastUpdated: String = "هنوز بروز نشده"
)

@Preview
@Composable
fun DailyOmenCardPreview() {
    ManaTheme {
        DailyOmenCard(uiState = DailyOmenUiState(omen = "اینجا فال روزانه شما نمایش داده می‌شود...", isLoading = false, lastUpdated = "همین الان"), onUpdate = {})
    }
}
