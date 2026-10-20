package com.example.ui.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Book
import com.example.data.repository.BookRepository
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CairoFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookLockScreen(
    bookId: Int,
    repository: BookRepository,
    onUnlocked: (Book) -> Unit,
    onNavigateBack: () -> Unit,
    onBookDeleted: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var pin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showRedWarningDialog by remember { mutableStateOf(false) }
    var isLockedOut by remember { mutableStateOf(false) }
    var book by remember { mutableStateOf<Book?>(null) }
    var remainingTime by remember { mutableStateOf(0L) }

    LaunchedEffect(bookId) {
        book = repository.getBookById(bookId)
        if (book?.lockoutEndTime ?: 0L > System.currentTimeMillis()) {
            isLockedOut = true
            remainingTime = (book!!.lockoutEndTime - System.currentTimeMillis()) / 1000
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("قفل الكتاب 🔐", fontFamily = AmiriFontFamily) }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLockedOut) {
                Text("الكتاب مقفل مؤقتاً. الرجاء المحاولة بعد:", fontFamily = CairoFontFamily)
                Text("$remainingTime ثانية", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Red)
            } else {
                Text("أدخل الرمز السري لفتح الكتاب", fontFamily = CairoFontFamily)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = errorMsg != null
                )
                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    scope.launch {
                        val currentBook = repository.getBookById(bookId) ?: return@launch
                        if (pin == currentBook.pinCode) {
                            repository.resetFailedAttempts(currentBook)
                            onUnlocked(currentBook)
                        } else {
                            val newAttempts = currentBook.failedAttempts + 1
                            if (newAttempts == 4) {
                                showRedWarningDialog = true
                                repository.updateFailedAttempts(currentBook, newAttempts)
                            } else if (newAttempts >= 5) {
                                repository.deleteBook(currentBook)
                                onBookDeleted()
                            } else {
                                errorMsg = "رمز خاطئ. المحاولة $newAttempts من 5"
                                repository.updateFailedAttempts(currentBook, newAttempts)
                            }
                        }
                    }
                }) {
                    Text("فتح", fontFamily = CairoFontFamily)
                }
            }
        }
    }

    if (showRedWarningDialog) {
        AlertDialog(
            onDismissRequest = { showRedWarningDialog = false },
            containerColor = Color(0xFFB71C1C),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFFFEBEE),
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Yellow) },
            title = { Text("تحذير ⚠️", fontFamily = AmiriFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
            text = {
                Text(
                    text = "تحذير ⚠️: محاولة أخرى خاطئة وسيتم مسح الكتاب نهائياً!",
                    fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showRedWarningDialog = false; errorMsg = "محاولة أخيرة!" },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFB71C1C))
                ) { Text("فهمت التحذير", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) }
            }
        )
    }
}
