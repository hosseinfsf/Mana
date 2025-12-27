package com.example.mana.ai

import android.content.Context
import com.example.mana.BuildConfig
import com.example.mana.ui.settings.AiModel
import com.google.ai.client.generativeai.GenerativeModel

object GenerativeAiManager {

    fun getActiveModel(context: Context): GenerativeModel {
        val settingsPrefs = context.getSharedPreferences("mana_settings", Context.MODE_PRIVATE)
        val selectedModel = AiModel.valueOf(settingsPrefs.getString("AI_MODEL", AiModel.FREE.name)!!)

        val apiKey = when (selectedModel) {
            AiModel.FREE -> BuildConfig.API_KEY // Use the default key for the free model
            AiModel.CUSTOM -> settingsPrefs.getString("CUSTOM_API_KEY", "") ?: ""
            AiModel.PRO -> "" // Pro model requires subscription, handle this later
        }

        val modelName = when (selectedModel) {
            AiModel.FREE, AiModel.CUSTOM -> "gemini-pro"
            AiModel.PRO -> "gemini-pro-vision" // Example of a different model for Pro
        }

        return GenerativeModel(modelName = modelName, apiKey = apiKey)
    }
}
