package com.rx.vitreos.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rx.vitreos.domain.model.MessageStatus
import com.rx.vitreos.ui.theme.VitreosAccent
import com.rx.vitreos.ui.theme.VitreosAccentSecondary
import com.rx.vitreos.ui.theme.VitreosGlass
import com.rx.vitreos.ui.theme.VitreosSuccess
import com.rx.vitreos.ui.theme.VitreosTextSecondary

@Composable
fun LiquidChatBubble(
    message: String,
    isSender: Boolean,
    status: MessageStatus = MessageStatus.SENT,
    timestamp: Long = System.currentTimeMillis(),
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "SquishyScale"
    )

    val bubbleShape = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = if (isSender) 28.dp else 4.dp,
        bottomEnd = if (isSender) 4.dp else 28.dp
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .scale(animatedScale)
            .graphicsLayer {
                // Note: RenderEffect blur would require Android 12+
                // For backward compatibility, we use alpha and gradient
                this.alpha = 1f
            }
            .clip(bubbleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isSender) {
                        listOf(
                            VitreosAccent.copy(alpha = 0.4f),
                            VitreosAccent.copy(alpha = 0.2f)
                        )
                    } else {
                        listOf(
                            VitreosGlass.copy(alpha = 0.6f),
                            VitreosGlass.copy(alpha = 0.3f)
                        )
                    }
                )
            )
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                shape = bubbleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = message,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
fun MessageStatusIndicator(
    status: MessageStatus,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (status) {
        MessageStatus.SENDING -> "○" to VitreosTextSecondary
        MessageStatus.SENT -> "✓" to VitreosTextSecondary
        MessageStatus.DELIVERED -> "✓✓" to VitreosAccentSecondary
        MessageStatus.READ -> "✓✓" to VitreosSuccess
    }

    Text(
        text = icon,
        color = color,
        fontSize = 12.sp,
        modifier = modifier
    )
}

@Composable
fun LiquidBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1B4B),
                        Color(0xFF0A0E1A),
                        Color(0xFF0F172A)
                    )
                )
            )
    ) {
        content()
    }
}