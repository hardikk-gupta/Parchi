package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkMuted
import com.example.ui.theme.MicPulsingRed
import com.example.ui.theme.MicRecordRed
import com.example.ui.theme.ParchiPurpleDark

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceRecordingOverlay(
    isListening: Boolean,
    liveTranscript: String,
    onStopListening: () -> Unit,
    onManualPhraseSubmit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var manualInput by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("voice_recording_dialog"),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isListening) "🎙️ Listening to Voice..." else "Speak or Type Grocery Items",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Close,
                            contentDescription = "Close",
                            tint = InkMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Pulsing Mic visualizer
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(MicRecordRed.copy(alpha = 0.2f))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(MicRecordRed, MicPulsingRed)
                                )
                            )
                            .clickable {
                                if (isListening) onStopListening()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) LucideIcons.Pause else LucideIcons.Mic,
                            contentDescription = "Microphone",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Transcript box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF7F5F0))
                        .border(1.dp, Color(0xFFE2DED4), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (liveTranscript.isNotBlank()) liveTranscript else "Say e.g.: \"10 kg atta 356, 5 kg sugar 280, 2 kg moong dal\"",
                        fontSize = 14.sp,
                        fontWeight = if (liveTranscript.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                        color = if (liveTranscript.isNotBlank()) InkBlack else InkMuted,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Quick Demo Voice Phrases (Tap to parse):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                val samplePhrases = listOf(
                    "10 kg atta 356",
                    "5 kg sugar 280 rupees",
                    "2 kg moong dal",
                    "1 litre mustard oil 145",
                    "3 packets maggi 42"
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (phrase in samplePhrases) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFF0EBE1))
                                .border(1.dp, Color(0xFFDDD6C7), RoundedCornerShape(20.dp))
                                .clickable {
                                    onManualPhraseSubmit(phrase)
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "🗣️ \"$phrase\"",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = InkBlack
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Manual input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = { manualInput = it },
                        placeholder = { Text("Or type phrase here...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_voice_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MicRecordRed,
                            focusedLabelColor = MicRecordRed
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (manualInput.isNotBlank()) {
                                onManualPhraseSubmit(manualInput.trim())
                                manualInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ParchiPurpleDark)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Send,
                            contentDescription = "Send Phrase",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Stop / Done Button
                TactileButton(
                    text = if (isListening) "Stop & Add to Bill" else "Done",
                    onClick = {
                        if (isListening) onStopListening() else onDismiss()
                    },
                    icon = if (isListening) LucideIcons.Pause else LucideIcons.Close,
                    backgroundColor = if (isListening) MicRecordRed else ParchiPurpleDark,
                    modifier = Modifier.fillMaxWidth(),
                    height = 50.dp,
                    testTag = "stop_voice_recording_button"
                )
            }
        }
    }
}
