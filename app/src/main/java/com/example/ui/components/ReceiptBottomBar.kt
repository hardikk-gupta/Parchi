package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelMint
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelSky
import com.example.ui.theme.PastelSkyAccent

/**
 * Bottom controls for Receipt Screen.
 * Displays identical circular RippleRecordButton when dictating, or action pill controls when finished.
 */
@Composable
fun ReceiptBottomBar(
    isReceiptFinished: Boolean,
    isListening: Boolean,
    liveTranscript: String,
    onRecordClick: () -> Unit,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onPrintClick: () -> Unit,
    onShareClick: () -> Unit,
    isProcessing: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!isReceiptFinished || isListening || isProcessing) {
            // STATE 1: Dictating or Processing (Uses universal circular mic button with pulse rings)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RippleRecordButton(
                    isListening = isListening,
                    liveTranscript = liveTranscript,
                    isProcessing = isProcessing,
                    onClick = onRecordClick,
                    modifier = Modifier.testTag("receipt_record_button")
                )
            }
        } else {
            // STATE 2: Receipt Completed (Action pills with Lucide Icons)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Edit Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x14000000))
                            .clip(RoundedCornerShape(16.dp))
                            .background(PastelLavender)
                            .border(1.2.dp, PastelLavenderAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable { onEditClick() }
                            .padding(horizontal = 8.dp)
                            .testTag("pill_edit_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = LucideIcons.Edit,
                                contentDescription = "Edit",
                                tint = PastelLavenderAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EDIT",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = PastelLavenderAccent
                            )
                        }
                    }

                    // Print Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x14000000))
                            .clip(RoundedCornerShape(16.dp))
                            .background(PastelSky)
                            .border(1.2.dp, PastelSkyAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable { onPrintClick() }
                            .padding(horizontal = 8.dp)
                            .testTag("pill_print_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = LucideIcons.Print,
                                contentDescription = "Print",
                                tint = PastelSkyAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PRINT",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = PastelSkyAccent
                            )
                        }
                    }

                    // Share Button
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .height(48.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x20000000))
                            .clip(RoundedCornerShape(16.dp))
                            .background(PastelMint)
                            .border(1.2.dp, PastelMintAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { onShareClick() }
                            .padding(horizontal = 10.dp)
                            .testTag("pill_share_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = LucideIcons.Share,
                                contentDescription = "Share",
                                tint = PastelMintAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SHARE",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = PastelMintAccent
                            )
                        }
                    }
                }
            }
        }
    }
}
