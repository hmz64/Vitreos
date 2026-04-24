package com.rx.vitreos.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rx.vitreos.ui.components.LiquidBackground
import com.rx.vitreos.ui.theme.VitreosAccent
import com.rx.vitreos.ui.theme.VitreosGlass
import com.rx.vitreos.ui.theme.VitreosText
import com.rx.vitreos.ui.theme.VitreosTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun AuthScreen(
    onLoginSuccess: (userId: String, username: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    val focusManager = LocalFocusManager.current

    LiquidBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "VITREOS",
                color = VitreosText,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp
            )

            Text(
                text = "Liquid Glass Messaging",
                color = VitreosTextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Glass input fields
            GlassTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                icon = Icons.Default.Person,
                enabled = !isLoading,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlassTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Phone Number",
                icon = Icons.Default.Phone,
                enabled = !isLoading,
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (username.isNotBlank() && phone.isNotBlank()) {
                            isLoginMode = true; isLoading = true
                        }
                    }
                )
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    isLoading = true
                },
                enabled = username.isNotBlank() && phone.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VitreosAccent,
                    contentColor = VitreosText,
                    disabledContainerColor = VitreosGlass,
                    disabledContentColor = VitreosTextSecondary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = VitreosText,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isLoginMode) " Login" else " Register",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { isLoginMode = !isLoginMode }
            ) {
                Text(
                    text = if (isLoginMode) "Need an account? Register" else "Have an account? Login",
                    color = VitreosTextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        VitreosGlass.copy(alpha = 0.4f),
                        VitreosGlass.copy(alpha = 0.2f)
                    )
                )
            )
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = VitreosTextSecondary) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VitreosTextSecondary
                )
            },
            enabled = enabled,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = VitreosText,
                unfocusedTextColor = VitreosText,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

suspend fun authenticateUser(
    username: String,
    phone: String,
    isLogin: Boolean
): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
    try {
        val url = URL("http://10.0.2.2:3001/auth/${if (isLogin) "login" else "register"}")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val body = """{"username":"$username","phone":"$phone"}"""
        connection.outputStream.use { it.write(body.toByteArray()) }

        val response = connection.inputStream.bufferedReader().readText()
        
        if (connection.responseCode in 200..299) {
            if (response.contains("\"success\":true")) {
                val userId = response.substringAfter("\"userId\":\"").substringBefore("\"")
                Result.success(Pair(userId, username))
            } else {
                Result.failure(Exception(response))
            }
        } else {
            Result.failure(Exception("Server error: ${connection.responseCode}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}