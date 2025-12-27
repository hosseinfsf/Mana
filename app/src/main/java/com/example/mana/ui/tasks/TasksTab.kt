package com.example.mana.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TasksTab() {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("کارهای روزانه", "لیست خرید", "یادداشت")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index })
            }
        }
        when (tabIndex) {
            0 -> DailyTasksScreen()
            1 -> ShoppingListScreen()
            2 -> NotesScreen()
        }
    }
}

@Composable
fun DailyTasksScreen(viewModel: DailyTasksViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var newTaskTitle by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newTaskTitle,
                onValueChange = { newTaskTitle = it },
                label = { Text("کار جدید...") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if (newTaskTitle.isNotBlank()) {
                    viewModel.addTask(newTaskTitle)
                    newTaskTitle = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "افزودن کار")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(uiState.tasks) { task ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { viewModel.toggleTaskCompletion(task.id) }
                    )
                    Text(text = task.title, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.removeTask(task.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف")
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListScreen(viewModel: ShoppingListViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var newItemName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newItemName,
                onValueChange = { newItemName = it },
                label = { Text("مورد جدید...") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if (newItemName.isNotBlank()) {
                    viewModel.addItem(newItemName)
                    newItemName = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "افزودن مورد")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { viewModel.removeBoughtItems() }) {
            Icon(Icons.Default.RemoveShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("حذف خریده شده ها")
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(uiState.items) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isBought,
                        onCheckedChange = { viewModel.toggleItemBought(item.id) }
                    )
                    Text(text = item.name, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun NotesScreen(viewModel: NotesViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteContent by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = newNoteTitle,
            onValueChange = { newNoteTitle = it },
            label = { Text("عنوان یادداشت") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = newNoteContent,
            onValueChange = { newNoteContent = it },
            label = { Text("متن یادداشت...") },
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            viewModel.addNote(newNoteTitle, newNoteContent)
            newNoteTitle = ""
            newNoteContent = ""
        }) {
            Text("افزودن یادداشت")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.notes) { note ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(note.title, style = MaterialTheme.typography.titleMedium)
                        Text(note.content, maxLines = 3, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (note.summary != null) {
                            Text("خلاصه: ${note.summary}", style = MaterialTheme.typography.bodySmall)
                        } else {
                            TextButton(onClick = { viewModel.summarizeNote(note.id) }) {
                                if (uiState.isSummarizing) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("خلاصه کن")
                            }
                        }
                    }
                }
            }
        }
    }
}
