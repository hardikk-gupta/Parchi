package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BillItem

/**
 * Clean, modern thermal paper receipt content matching the reference image.
 * Starts strictly with Date, Time, compact icon-only customer spaces,
 * followed by the thermal receipt table and total as the final row.
 */
@Composable
fun MinimalThermalReceiptContent(
    storeName: String = "MANMOHANI HATTI",
    dateTime: String,
    billNumber: String,
    customerName: String,
    customerPhone: String,
    customerHouseNo: String,
    items: List<BillItem>,
    totalAmount: Double,
    isProcessing: Boolean = false,
    onEditCustomerClick: () -> Unit,
    onToggleVerify: (BillItem) -> Unit,
    onEditItem: (BillItem) -> Unit,
    onDeleteItem: (BillItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Store Name Header
        Text(
            text = storeName.uppercase(java.util.Locale.getDefault()),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp,
            color = Color(0xFF18181B),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Date & Time line (compact & clean)
        Text(
            text = dateTime,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.8.sp,
            color = Color(0xFF27272A),
            textAlign = TextAlign.Center
        )

        // Customer details: Only displayed when the user specifies name or telephone number
        if (customerName.isNotBlank() || customerPhone.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditCustomerClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (customerName.isNotBlank()) {
                    Text(
                        text = "Customer: $customerName",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF18181B)
                    )
                }
                if (customerPhone.isNotBlank()) {
                    Text(
                        text = "Tel: $customerPhone",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF18181B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dashed receipt divider line
        Text(
            text = "--------------------------------------------------------",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFD4D4D8),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Table Column Header (Clean thermal printer aesthetic)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF71717A),
                modifier = Modifier.width(22.dp)
            )
            Text(
                text = "ITEM",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF71717A),
                modifier = Modifier.weight(1.2f)
            )
            Text(
                text = "QTY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF71717A),
                modifier = Modifier.weight(0.9f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "PRICE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF71717A),
                modifier = Modifier.width(62.dp),
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Items List (Emerges as spoken)
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        color = Color(0xFF18181B),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "CREATING RECEIPT...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = Color(0xFF18181B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Structuring items & quantities...",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF71717A)
                    )
                }
            }
        } else if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "READY FOR DICTATION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFA1A1AA)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Speak items: e.g. Aata 10 kg, two brush",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF71717A)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.forEach { item ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // S.No bullet
                            Text(
                                text = "${item.serialNumber}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF71717A),
                                modifier = Modifier.width(22.dp)
                            )

                            // Item Name
                            Text(
                                text = item.itemName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF18181B),
                                modifier = Modifier.weight(1.2f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Quantity / Weight
                            Text(
                                text = item.weightOrQuantity,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF27272A),
                                modifier = Modifier.weight(0.9f),
                                textAlign = TextAlign.Center
                            )

                            // Price (Leave blank if unpriced so shopkeeper can write with pen)
                            Text(
                                text = if (item.price != null) "₹${if (item.price % 1.0 == 0.0) item.price.toInt() else item.price}" else "      ",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF18181B),
                                modifier = Modifier.width(62.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dashed line before total
        Text(
            text = "--------------------------------------------------------",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFD4D4D8),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Total Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TOTAL",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = Color(0xFF18181B)
            )

            val totalText = if (totalAmount > 0.0) {
                "₹ ${if (totalAmount % 1.0 == 0.0) totalAmount.toInt() else String.format("%.2f", totalAmount)}"
            } else {
                "₹ _______"
            }

            Text(
                text = totalText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF18181B)
            )
        }
    }
}
