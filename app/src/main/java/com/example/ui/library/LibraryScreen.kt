package com.example.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Book
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.GoldAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(factory = LibraryViewModel.Factory),
    onBookClick: (Book) -> Unit,
    onOpenSettings: () -> Unit
) {
    val books by viewModel.books.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📖 كاتب محترف", fontFamily = AmiriFontFamily, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "الإعدادات")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة كتاب")
            }
        }
    ) { padding ->
        if (books.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("المكتبة فارغة. اضغط على + لإضافة كتاب.", fontFamily = CairoFontFamily)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(books) { book ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onBookClick(book) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(book.title, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(book.author, fontFamily = CairoFontFamily, fontSize = 14.sp, color = GoldAccent)
                            }
                            IconButton(onClick = { viewModel.deleteBook(book) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBookDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, author ->
                viewModel.addBook(title, author)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddBookDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة كتاب جديد", fontFamily = CairoFontFamily) },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان الكتاب") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("اسم المؤلف") })
            }
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onAdd(title, author) }) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
