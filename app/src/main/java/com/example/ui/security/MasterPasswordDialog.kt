package com.example.ui.security

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CairoFontFamily

@Composable
fun MasterPasswordDialog(
    hasExistingMaster: Boolean,
    onDismissRequest: () -> Unit,
    onSaveMasterPassword: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (hasExistingMaster) "تعديل الرمز الموحد" else "تعيين الرمز الموحد", fontFamily = CairoFontFamily) },
        text = {
            Column {
                Text("أدخل رمزاً مكوناً من 1 إلى 6 أرقام لفتح جميع الكتب المقفلة:", fontFamily = CairoFontFamily)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (pin.isNotBlank()) onSaveMasterPassword(pin) }) {
                Text("حفظ", fontFamily = CairoFontFamily)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("إلغاء", fontFamily = CairoFontFamily) }
        }
    )
}
