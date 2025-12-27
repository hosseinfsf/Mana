package com.example.mana.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mana.billing.BillingManager
import com.example.mana.services.FloatingIconService
import com.example.mana.ui.theme.ManaTheme

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel = viewModel(), billingManager: BillingManager = viewModel()) {
    val uiState by settingsViewModel.uiState.collectAsState()
    val billingState by billingManager.uiState.collectAsState()
    val context = LocalContext.current
    val activity = (context as? Activity)

    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true
    }

    fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
        context.startActivity(intent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        SettingsGroup(title = "تنظیمات عمومی") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("فعال بودن دستیار مانا", modifier = Modifier.weight(1f))
                Switch(checked = uiState.isAssistantEnabled, onCheckedChange = { isEnabled ->
                    if (isEnabled) {
                        if (hasOverlayPermission()) {
                            context.startService(Intent(context, FloatingIconService::class.java))
                            settingsViewModel.setAssistantEnabled(true)
                        } else {
                            requestOverlayPermission()
                        }
                    } else {
                        context.stopService(Intent(context, FloatingIconService::class.java))
                        settingsViewModel.setAssistantEnabled(false)
                    }
                })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsGroup(title = "شخصی سازی") {
            Text("پوسته اپلیکیشن:", style = MaterialTheme.typography.labelLarge)
            ThemeSelector(selectedTheme = uiState.theme, onThemeSelected = { settingsViewModel.setTheme(it) })
            Spacer(modifier = Modifier.height(16.dp))
            Text("آیکون شناور:", style = MaterialTheme.typography.labelLarge)
            IconSelector(uiState.availableIcons, uiState.selectedIcon) { settingsViewModel.setIcon(it) }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsGroup(title = "هوش مصنوعی") {
            AiModelSelector(selectedModel = uiState.selectedAiModel) { model ->
                if (model == AiModel.PRO && !billingState.isProUser) {
                    Toast.makeText(context, "برای استفاده از مدل Pro، لطفاً اشتراک تهیه کنید.", Toast.LENGTH_SHORT).show()
                } else {
                    settingsViewModel.setAiModel(model)
                }
            }
            AnimatedVisibility(visible = uiState.selectedAiModel == AiModel.CUSTOM) {
                OutlinedTextField(
                    value = uiState.customApiKey,
                    onValueChange = { settingsViewModel.setCustomApiKey(it) },
                    label = { Text("کلید API شخصی") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsGroup(title = "حساب کاربری و اشتراک") {
            if (billingState.isProUser) {
                Text("شما کاربر حرفه‌ای مانا هستید! ✨")
            } else {
                val price = billingState.proSubscriptionDetails?.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice
                Button(
                    onClick = { if (activity != null) billingManager.launchPurchaseFlow(activity) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = billingState.proSubscriptionDetails != null
                ) {
                    Text(if (price != null) "ارتقا به نسخه Pro ($price)" else "در حال بارگذاری اشتراک...")
                }
                 Text("با ارتقا به نسخه Pro، به قابلیت‌های زیر دسترسی خواهید داشت:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top=8.dp))
                 Text("- اتوماسیون و راه‌اندازهای هوشمند", style = MaterialTheme.typography.bodySmall)
                 Text("- مدل‌های هوش مصنوعی پیشرفته‌تر", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.padding(start = 8.dp), content = content)
    }
}

@Composable
fun ThemeSelector(selectedTheme: ThemeSetting, onThemeSelected: (ThemeSetting) -> Unit) {
    val themes = listOf(ThemeSetting.LIGHT to "روشن", ThemeSetting.DARK to "تیره", ThemeSetting.SYSTEM to "پیش فرض سیستم")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        themes.forEach { (theme, name) ->
             Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.selectable(selected = (theme == selectedTheme), onClick = { onThemeSelected(theme) })) {
                Text(name)
                RadioButton(selected = (theme == selectedTeam), onClick = null)
            }
        }
    }
}

@Composable
fun IconSelector(availableIcons: List<FloatingIconSetting>, selectedIcon: FloatingIconSetting?, onIconSelected: (FloatingIconSetting) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        availableIcons.forEach { icon ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.selectable(selected = (icon == selectedIcon), onClick = { onIconSelected(icon) })) {
                Text(icon.name)
                RadioButton(selected = (icon == selectedIcon), onClick = null)
            }
        }
    }
}

@Composable
fun AiModelSelector(selectedModel: AiModel, onModelSelected: (AiModel) -> Unit) {
    val models = listOf(AiModel.FREE, AiModel.PRO, AiModel.CUSTOM)
     Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        models.forEach { model ->
             Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.selectable(selected = (model == selectedModel), onClick = { onModelSelected(model) })) {
                Text(model.readableName)
                RadioButton(selected = (model == selectedModel), onClick = null)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ManaTheme {
        SettingsScreen()
    }
}
