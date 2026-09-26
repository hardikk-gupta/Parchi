package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppLogoIcon
import com.example.ui.components.LucideIcons
import com.example.ui.components.MinimalThermalReceiptContent
import com.example.ui.components.ReceiptBottomBar
import com.example.ui.components.ThermalPrinterDispenser
import com.example.ui.theme.InkDark
import com.example.ui.theme.ParchiPurplePrimary
import com.example.ui.theme.ParchiPurpleSupporting
import com.example.util.ReceiptPrintHelper
import com.example.util.ReceiptShareHelper

@Composable
fun ReceiptScreen(
    viewModel: BillViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Audio Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceRecording()
        } else {
            Toast.makeText(
                context,
                "Microphone permission is needed to speak receipt items.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF7FC))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button with Lucide ArrowLeft
                IconButton(
                    onClick = { viewModel.navigateToHome() },
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color(0x14000000))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .testTag("receipt_top_back_button")
                ) {
                    Icon(
                        imageVector = LucideIcons.ArrowLeft,
                        contentDescription = "Back to home",
                        tint = InkDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Brand Logo & Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppLogoIcon(
                        size = 22.dp,
                        fillColor = ParchiPurplePrimary,
                        strokeColor = ParchiPurpleSupporting
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PARCHI RECEIPT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = InkDark
                    )
                }

                Spacer(modifier = Modifier.width(38.dp))
            }

            // Scrollable Content: 3D Thermal Printer Dispenser Slot + Emerging Receipt
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                ThermalPrinterDispenser(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MinimalThermalReceiptContent(
                        storeName = uiState.storeName,
                        dateTime = uiState.formattedDateTime,
                        billNumber = uiState.billNumber,
                        customerName = uiState.customerName,
                        customerPhone = uiState.customerPhone,
                        customerHouseNo = uiState.customerHouseNo,
                        items = uiState.items,
                        totalAmount = uiState.totalAmount,
                        isProcessing = uiState.isProcessing,
                        onEditCustomerClick = { viewModel.openCustomerEdit() },
                        onToggleVerify = { viewModel.toggleItemVerification(it) },
                        onEditItem = { viewModel.openEditItem(it) },
                        onDeleteItem = { viewModel.deleteItem(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Controls: Circular Mic Button (when dictating) or Action Pills (when finished)
            ReceiptBottomBar(
                isReceiptFinished = uiState.isReceiptFinished || uiState.items.isNotEmpty(),
                isListening = uiState.isListening,
                liveTranscript = uiState.liveTranscript,
                isProcessing = uiState.isProcessing,
                onRecordClick = {
                    val permission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    )
                    if (permission == PackageManager.PERMISSION_GRANTED) {
                        viewModel.toggleVoiceRecording()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onBackClick = { viewModel.navigateToHome() },
                onEditClick = { viewModel.openEditOptions() },
                onPrintClick = {
                    if (uiState.items.isEmpty()) {
                        Toast.makeText(context, "No items to print", Toast.LENGTH_SHORT).show()
                    } else {
                        ReceiptPrintHelper.printReceipt(
                            context = context,
                            storeName = uiState.storeName,
                            billNumber = uiState.billNumber,
                            dateTime = uiState.formattedDateTime,
                            customerName = uiState.customerName,
                            customerPhone = uiState.customerPhone,
                            customerHouseNo = uiState.customerHouseNo,
                            items = uiState.items,
                            totalAmount = uiState.totalAmount
                        )
                    }
                },
                onShareClick = {
                    if (uiState.items.isEmpty()) {
                        Toast.makeText(context, "No items to share", Toast.LENGTH_SHORT).show()
                    } else {
                        ReceiptShareHelper.shareReceipt(
                            context = context,
                            storeName = uiState.storeName,
                            billNumber = uiState.billNumber,
                            dateTime = uiState.formattedDateTime,
                            customerName = uiState.customerName,
                            customerPhone = uiState.customerPhone,
                            customerHouseNo = uiState.customerHouseNo,
                            items = uiState.items,
                            totalAmount = uiState.totalAmount
                        )
                    }
                }
            )
        }
    }
}
