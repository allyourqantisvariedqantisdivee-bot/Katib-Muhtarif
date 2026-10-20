package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.BookRepository
import com.example.ui.security.MasterPasswordDialog
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: BookRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appSettings by repository.appSettings.collectAsState(initial = null)

    val currentTheme = appSettings?.themeMode ?: "LIGHT"
    val hasMasterPassword = !appSettings?.masterPasswordHash.isNullOrBlank()

    var showMasterPasswordDialog by remember { mutableStateOf(false) }
    var isPrivacyExpanded by remember { mutableStateOf(false) }

    val developerEmail = "allyourqantisvariedqantisdivee@gmail.com"
    val developerQuote = "تم برمجت وتطوير التطبيق بواسطة اسماعيل رافت، نؤمن بأن التطور وحرية التعبير والحفاظ على خصوصيتكم حق لا يوجد اي نقاش فيها ونحن دائماً نسعى لما يرضيكم. للتواصل مع الدعم الفني: $developerEmail"

    BackHandler { onNavigateBack() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "الإعدادات العامة ⚙️",
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. مظهر التطبيق
            item {
                Text("مظهر التطبيق والقراءة:", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { scope.launch { repository.setThemeMode("LIGHT") } },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentTheme == "LIGHT") GoldAccent else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) { Text("فاتح ☀️", fontFamily = CairoFontFamily) }

                    Button(
                        onClick = { scope.launch { repository.setThemeMode("DARK") } },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentTheme == "DARK") GoldAccent else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) { Text("داكن 🌙", fontFamily = CairoFontFamily) }

                    Button(
                        onClick = { scope.launch { repository.setThemeMode("SEPIA") } },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentTheme == "SEPIA") GoldAccent else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) { Text("سيبيا 📜", fontFamily = CairoFontFamily) }
                }
            }

            // 2. الرمز الموحد
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showMasterPasswordDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("الرمز الموحد (Master Password)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(if (hasMasterPassword) "الرمز مفعّل (انقر للتعديل)" else "غير مفعّل (انقر للتعيين)", fontFamily = CairoFontFamily, fontSize = 12.sp, color = GoldAccent)
                        }
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = GoldAccent)
                    }
                }
            }

            // 3. سياسة الخصوصية
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { isPrivacyExpanded = !isPrivacyExpanded },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🛡️ سياسة الخصوصية وحماية البيانات", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Icon(if (isPrivacyExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                        }

                        AnimatedVisibility(visible = isPrivacyExpanded) {
                            Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("• بيانات محلية 100%: جميع الكتب والمسودات تخزن داخل هاتفك في قاعدة بيانات Room ولا ترسل لأي خادم.", fontFamily = CairoFontFamily, fontSize = 12.sp)
                                Text("• صفر تتبع وصفر تسجيل: لا يتم جمع أي بيانات شخصية أو طلب حساب أو تسجيل دخول.", fontFamily = CairoFontFamily, fontSize = 12.sp)
                                Text("• ملكية فكرية مطلقة: كل ما تكتبه ملك لك وحدك بالكامل.", fontFamily = CairoFontFamily, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 4. عن المطور
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("عن مطور التطبيق", fontFamily = AmiriFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GoldAccent)
                        Text(
                            text = developerQuote,
                            fontFamily = AmiriFontFamily,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$developerEmail")
                                    putExtra(Intent.EXTRA_SUBJECT, "تطبيق كاتب محترف - الدعم الفني")
                                }
                                try { context.startActivity(emailIntent) } catch (_: Exception) {}
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("مراسلة الدعم الفني", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showMasterPasswordDialog) {
        MasterPasswordDialog(
            hasExistingMaster = hasMasterPassword,
            onDismissRequest = { showMasterPasswordDialog = false },
            onSaveMasterPassword = { newPin ->
                scope.launch {
                    repository.setMasterPassword(newPin)
                    showMasterPasswordDialog = false
                    Toast.makeText(context, "تم حفظ الرمز الموحد", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
