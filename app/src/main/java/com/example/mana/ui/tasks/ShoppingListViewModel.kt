package com.example.mana.ui.tasks

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShoppingItem(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    var isBought: Boolean = false
)

data class ShoppingListUiState(
    val items: List<ShoppingItem> = emptyList()
)

class ShoppingListViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState = _uiState.asStateFlow()

    private val sharedPref = application.getSharedPreferences("mana_shopping_list", Context.MODE_PRIVATE)

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            val itemsString = sharedPref.getString("shopping_items", null)
            if (itemsString != null) {
                val items = itemsString.split(";").mapNotNull { 
                    val parts = it.split(",")
                    if (parts.size == 3) ShoppingItem(parts[0].toLong(), parts[1], parts[2].toBoolean())
                    else null
                }
                _uiState.update { it.copy(items = items) }
            }
        }
    }

    private fun saveItems() {
        val itemsString = _uiState.value.items.joinToString(";") { "${it.id},${it.name},${it.isBought}" }
        with(sharedPref.edit()) {
            putString("shopping_items", itemsString)
            apply()
        }
    }

    fun addItem(name: String) {
        val newItem = ShoppingItem(name = name)
        _uiState.update { it.copy(items = it.items + newItem) }
        saveItems()
    }

    fun toggleItemBought(itemId: Long) {
        _uiState.update { currentState ->
            val updatedItems = currentState.items.map {
                if (it.id == itemId) it.copy(isBought = !it.isBought)
                else it
            }
            currentState.copy(items = updatedItems)
        }
        saveItems()
    }

    fun removeBoughtItems() {
        _uiState.update { it.copy(items = it.items.filterNot { item -> item.isBought }) }
        saveItems()
    }
}
