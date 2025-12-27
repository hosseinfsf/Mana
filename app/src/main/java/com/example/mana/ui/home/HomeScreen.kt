package com.example.mana.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mana.services.FloatingIconService
import com.example.mana.ui.theme.ManaTheme
import saman.zamani.persiandate.PersianDate

@Composable
fun HomeScreen(homeViewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by homeViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        homeViewModel.loadAllData()
    }
    
    if (uiState.nightSummary != null) {
        AlertDialog(
            onDismissRequest = { homeViewModel.clearNightSummary() },
            title = { Text("🌙 شب‌نامه مانا 🌙") },
            text = { Text(uiState.nightSummary!!) },
            confirmButton = {
                Button(onClick = { homeViewModel.clearNightSummary() }) {
                    Text("باشه")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val persianDate = PersianDate()
        val currentDate = "${persianDate.dayName()}، ${persianDate.shDay} ${persianDate.monthName} ${persianDate.shYear}"

        Text(text = "روزنامه مانا", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = currentDate, style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "سلام ${uiState.userName}! הנה خلاصه امروزت:", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Start))
        
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("کارهای باقی مانده امروز:", style = MaterialTheme.typography.titleMedium)
                val uncompletedTasks = uiState.tasks.filter { !it.isCompleted }
                if (uncompletedTasks.isEmpty()) {
                    Text("آفرین! کار مهمی برای امروز نداری.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    uncompletedTasks.take(3).forEach {
                        Text("- ${it.title}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("شما ${uiState.shoppingItemCount} مورد در لیست خرید خود دارید.", fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isGeneratingSummary) {
            CircularProgressIndicator()
        } else {
            Button(onClick = { homeViewModel.generateNightSummary() }) {
                Text("دریافت شب نامه")
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ManaTheme {
        HomeScreen()
    }
}
