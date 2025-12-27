package com.example.mana.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.mana.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ThemeSetting { LIGHT, DARK, SYSTEM }
data class FloatingIconSetting(val name: String, @androidx.annotation.RawRes val resId: Int)
enum class AiModel(val readableName: String) { FREE("رایگان (Gemini)"), PRO("پیشرفته (Pro)"), CUSTOM("شخصی") }

data class SettingsUiState(
    val isAssistantEnabled: Boolean = true,
    val theme: ThemeSetting = ThemeSetting.SYSTEM,
    val availableIcons: List<FloatingIconSetting> = emptyList(),
    val selectedIcon: FloatingIconSetting? = null,
    val selectedAiModel: AiModel = AiModel.FREE,
    val customApiKey: String = ""
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val sharedPref = application.getSharedPreferences("mana_settings", Context.MODE_PRIVATE)

    init { loadSettings() }

    private fun loadSettings() {
        val isAssistantEnabled = sharedPref.getBoolean("ASSISTANT_ENABLED", true)
        val theme = ThemeSetting.valueOf(sharedPref.getString("THEME", ThemeSetting.SYSTEM.name)!!)
        
        val availableIcons = listOf(FloatingIconSetting("گربه", R.raw.cat_icon)) // Add more icons later
        val selectedIconResId = sharedPref.getInt("ICON_RES_ID", R.raw.cat_icon)
        val selectedIcon = availableIcons.find { it.resId == selectedIconResId }

        val model = AiModel.valueOf(sharedPref.getString("AI_MODEL", AiModel.FREE.name)!!)
        val customApiKey = sharedPref.getString("CUSTOM_API_KEY", "")!!

        _uiState.update { it.copy(
            isAssistantEnabled = isAssistantEnabled,
            theme = theme,
            availableIcons = availableIcons,
            selectedIcon = selectedIcon,
            selectedAiModel = model,
            customApiKey = customApiKey
        ) }
    }

    fun setAssistantEnabled(isEnabled: Boolean) {
        _uiState.update { it.copy(isAssistantEnabled = isEnabled) }
        sharedPref.edit().putBoolean("ASSISTANT_ENABLED", isEnabled).apply()
    }

    fun setTheme(theme: ThemeSetting) {
        _uiState.update { it.copy(theme = theme) }
        sharedPref.edit().putString("THEME", theme.name).apply()
    }

    fun setIcon(icon: FloatingIconSetting) {
        _uiState.update { it.copy(selectedIcon = icon) }
        sharedPref.edit().putInt("ICON_RES_ID", icon.resId).apply()
    }
    
    fun setAiModel(model: AiModel) {
        _uiState.update { it.copy(selectedAiModel = model) }
        sharedPref.edit().putString("AI_MODEL", model.name).apply()
    }

    fun setCustomApiKey(apiKey: String) {
        _uiState.update { it.copy(customApiKey = apiKey) }
        sharedPref.edit().putString("CUSTOM_API_KEY", apiKey).apply()
    }
}
