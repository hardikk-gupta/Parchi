package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BillItem
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkFaint
import com.example.ui.theme.InkMuted
import com.example.ui.theme.PriceGreen
import com.example.ui.theme.ReceiptDashedLine
import com.example.ui.theme.VerifiedGreen

@Composable
fun BillItemRow(
    item: BillItem,
    onToggleVerify: (BillItem) -> Unit,
    onEditItem: (BillItem) -> Unit,
    onDeleteItem: (BillItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (item.isVerified) Color(0xFFF0FDF4) else Color.Transparent,
        label = "item_row_bg"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag("bill_item_${item.serialNumber}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Verify Checkbox / Check indicator
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.isVerified) VerifiedGreen else Color(0xFFF1ECE4)
                    )
                    .border(
                        1.dp,
                        if (item.isVerified) VerifiedGreen else Color(0xFFD6CEBF),
                        CircleShape
                    )
                    .clickable { onToggleVerify(item) },
                contentAlignment = Alignment.Center
            ) {
                if (item.isVerified) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Verified",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = "${item.serialNumber}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Item Name & Weight / Qty
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onEditItem(item) }
            ) {
                Text(
                    text = item.itemName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEFECE6))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.weightOrQuantity,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkBlack
                        )
                    }

                    if (item.isVerified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Verified ✓",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = VerifiedGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Price Column (shows formatted rupee price, or empty placeholder dashes if user didn't say price)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (item.price != null) Color(0xFFF3FBF5) else Color(0xFFF8F7F4))
                    .border(1.dp, if (item.price != null) Color(0xFFC7EBD1) else Color(0xFFE5E1D8), RoundedCornerShape(6.dp))
                    .clickable { onEditItem(item) }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                if (item.price != null) {
                    Text(
                        text = item.formattedPrice,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PriceGreen,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Text(
                        text = "      ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkFaint,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Quick Actions: Edit & Delete
            IconButton(
                onClick = { onEditItem(item) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Item",
                    tint = InkMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = { onDeleteItem(item) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Item",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Soft dashed separator
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ReceiptDashedLine.copy(alpha = 0.5f))
        )
    }
}
