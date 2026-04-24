package com.rx.vitreos.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rx.vitreos.ui.components.LiquidBackground
import com.rx.vitreos.ui.components.LiquidChatBubble
import com.rx.vitreos.ui.components.TypingIndicator
import com.rx.vitreos.ui.components.UserOnlineStatus
import com.rx.vitreos.ui.theme.VitreosAccent
import com.rx.vitreos.ui.theme.VitreosGlass
import com.rx.vitreos.ui.theme.VitreosText
import com.rx.vitreos.ui.theme.VitreosTextSecondary
import com.rx.vitreos.domain.model.Message
import com.rx.vitreos.domain.model.MessageStatus
import com.rx.vitreos.domain.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentUserId: String,
    contact: User,
    messages: List<Message>,
    isContactTyping: Boolean,
    isContactOnline: Boolean,
    onSendMessage: (String) -> Unit,
    onTyping: () -> Unit,
    onStopTyping: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var showTypingIndicator by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(isContactTyping) {
        if (isContactTyping) {
            showTypingIndicator = true
        } else {
            delay(2000)
            showTypingIndicator = false
        }
    }

    LiquidBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Custom Glass Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                VitreosGlass.copy(alpha = 0.6f),
                                VitreosGlass.copy(alpha = 0.3f)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = VitreosText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = VitreosAccent.copy(alpha = 0.3f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.username.first().uppercase(),
                            color = VitreosText,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = contact.username,
                            color = VitreosText,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UserOnlineStatus(isOnline = isContactOnline)
                            Text(
                                text = if (isContactOnline) "online" else "offline",
                                color = if (isContactOnline) Color(0xFF10B981) else VitreosTextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                reverseLayout = false
            ) {
                items(messages, key = { it.id }) { message ->
                    val isSender = message.senderId == currentUserId

                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isSender) Alignment.End else Alignment.Start
                        ) {
                            LiquidChatBubble(
                                message = message.content,
                                isSender = isSender,
                                status = message.status
                            )
                        }
                    }
                }

                // Typing indicator
                if (showTypingIndicator) {
                    item {
                        TypingIndicator(
                            modifier = Modifier
                                .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                        )
                    }
                }
            }

            // Input Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                VitreosGlass.copy(alpha = 0.3f),
                                VitreosGlass.copy(alpha = 0.5f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { newText ->
                            messageText = newText
                            if (newText.isNotEmpty()) {
                                onTyping()
                                coroutineScope.launch {
                                    delay(2000)
                                    onStopTyping()
                                }
                            }
                        },
                        placeholder = {
                            Text(
                                "Type a message...",
                                color = VitreosTextSecondary
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = VitreosText,
                            unfocusedTextColor = VitreosText,
                            focusedBorderColor = VitreosAccent.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = VitreosGlass.copy(alpha = 0.3f),
                            unfocusedContainerColor = VitreosGlass.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (messageText.isNotBlank()) {
                                    onSendMessage(messageText)
                                    messageText = ""
                                    focusManager.clearFocus()
                                }
                            }
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSendMessage(messageText)
                                messageText = ""
                                focusManager.clearFocus()
                            }
                        },
                        enabled = messageText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (messageText.isNotBlank()) VitreosAccent else VitreosTextSecondary
                        )
                    }
                }
            }
        }
    }
}