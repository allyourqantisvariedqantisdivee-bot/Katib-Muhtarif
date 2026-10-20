package com.example.ui.editor

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookPage
import com.example.util.ExportFormat
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAccent
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit
) {
    val pages by viewModel.pages.collectAsState()
    val book by viewModel.book.collectAsState()
    var showExportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // TTS Setup
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("ar")
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book.title, fontFamily = AmiriFontFamily, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        if (pages.isNotEmpty()) tts?.speak(pages.last().content, TextToSpeech.QUEUE_FLUSH, null, null) 
                    }) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "قراءة صوتية")
                    }
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "تصدير")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addPage() }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة صفحة")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            itemsIndexed(pages) { index, page ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    // رقم الصفحة
                    Text(
                        text = "(${page.pageNumber})",
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GoldAccent,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // حقل الكتابة
                    OutlinedTextField(
                        value = page.content,
                        onValueChange = { newText -> viewModel.updatePageContent(page, newText) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = AmiriFontFamily, fontSize = 18.sp),
                        placeholder = { Text("ابدأ الكتابة هنا...") }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // شريط الأدوات السفلي (العدّاد والفواصل)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "الكلمات: ${page.content.split("\\s+".toRegex()).filter { it.isNotBlank() }.size} | الأحرف: ${page.content.length}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "إضافة فاصل ____",
                            fontSize = 12.sp,
                            color = GoldAccent,
                            modifier = Modifier.clickable { 
                                viewModel.updatePageContent(page, page.content + "\n____________________\n") 
                            }
                        )
                    }
                    Divider(modifier = Modifier.padding(top = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { format ->
                viewModel.exportBook(context, format)
                showExportDialog = false
            }
        )
    }
}

@Composable
fun ExportDialog(onDismiss: () -> Unit, onExport: (ExportFormat) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تصدير الكتاب", fontFamily = AmiriFontFamily) },
        text = {
            Column {
                ExportFormat.values().forEach { format ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onExport(format) }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(format.icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(format.displayName, fontFamily = AmiriFontFamily)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
