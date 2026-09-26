package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InkMuted
import com.example.ui.theme.ParchiPurplePrimary
import com.example.ui.theme.ParchiPurpleSupporting

/**
 * Universal Circular Voice Recording Mic Component with Lucide Icons and Pulse Animations.
 * Shared across both Home screen and Receipt screen for unified visual identity.
 */
@Composable
fun RippleRecordButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    liveTranscript: String = "",
    isProcessing: Boolean = false,
    testTag: String = "ripple_record_button"
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "btn_press_scale"
    )

    // Pulse & Ripple Infinite Transitions
    val infiniteTransition = rememberInfiniteTransition(label = "voice_ripples")

    // Ripple wave 1
    val ripple1Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_1_scale"
    )
    val ripple1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_1_alpha"
    )

    // Ripple wave 2
    val ripple2Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, delayMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_2_scale"
    )
    val ripple2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, delayMillis = 350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_2_alpha"
    )

    // Center button pulse
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "center_pulse"
    )

    val primaryBgColor = ParchiPurpleSupporting // #A500FF

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular Ripple & Button Container
        Box(
            modifier = Modifier
                .size(90.dp)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            // Radiating pulse rings (Visible only during active voice recording)
            if (isListening) {
                // Wave 2
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .scale(ripple2Scale)
                        .clip(CircleShape)
                        .background(ParchiPurpleSupporting.copy(alpha = ripple2Alpha * 0.4f))
                        .border(1.8.dp, ParchiPurplePrimary.copy(alpha = ripple2Alpha), CircleShape)
                )

                // Wave 1
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .scale(ripple1Scale)
                        .clip(CircleShape)
                        .background(ParchiPurpleSupporting.copy(alpha = ripple1Alpha * 0.5f))
                        .border(2.dp, ParchiPurplePrimary.copy(alpha = ripple1Alpha), CircleShape)
                )
            }

            // Tactile shadow under circular button
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .offset { IntOffset(0, 4.dp.roundToPx()) }
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.2f))
            )

            // Core Circular Record Button
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .scale(if (isListening) pulseScale * buttonScale else buttonScale)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                primaryBgColor,
                                Color(0xFF8800D6)
                            )
                        )
                    )
                    .border(
                        width = if (isListening) 3.dp else 2.dp,
                        color = ParchiPurplePrimary,
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) LucideIcons.Pause else LucideIcons.Mic,
                    contentDescription = if (isListening) "Pause Recording" else "Start Voice Dictation",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // Display transcript text in distinct grey (#64748B) ONLY during recording or processing
        if (isListening || isProcessing) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isProcessing) "CREATING RECEIPT..." else if (liveTranscript.isNotBlank()) "Hearing: \"$liveTranscript\"" else "TAP TO PAUSE",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = InkMuted // High visibility grey (#64748B)
            )
        }
    }
}
