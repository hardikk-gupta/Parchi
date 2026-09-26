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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkFaint
import com.example.ui.theme.InkMuted
import com.example.ui.theme.ReceiptDashedLine

@Composable
fun CustomerInfoSection(
    storeName: String,
    billNumber: String,
    formattedDateTime: String,
    customerName: String,
    customerPhone: String,
    customerHouseNo: String,
    onEditCustomerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Top Store Branding & Bill Number
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = storeName.uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = InkBlack,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "GROCERY BILL / CASH MEMO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkMuted,
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1ECE4))
                    .border(1.dp, Color(0xFFDDD5C7), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Bill Number",
                        tint = InkBlack,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "#$billNumber",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Date and Time Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFFAF7F2))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Date and Time",
                    tint = InkMuted,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedDateTime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = InkBlack
                )
            }

            Text(
                text = "Voice kirana billing",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkFaint
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Customer details block (tap to edit details, preserves empty spaces as mandated)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFAF9F6))
                .border(1.dp, Color(0xFFE8E4DA), RoundedCornerShape(12.dp))
                .clickable { onEditCustomerClick() }
                .padding(10.dp)
                .testTag("customer_info_card")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CUSTOMER DETAILS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = InkMuted
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Customer Details",
                            tint = InkMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Tap to edit",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = InkMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Render present customer fields without empty dashes
                var hasAnyField = false

                if (customerName.isNotBlank()) {
                    hasAnyField = true
                    CustomerDetailLine(
                        icon = Icons.Default.Person,
                        label = "Name",
                        value = customerName
                    )
                }

                if (customerPhone.isNotBlank()) {
                    if (hasAnyField) Spacer(modifier = Modifier.height(4.dp))
                    hasAnyField = true
                    CustomerDetailLine(
                        icon = Icons.Default.Call,
                        label = "Number",
                        value = customerPhone
                    )
                }

                if (customerHouseNo.isNotBlank()) {
                    if (hasAnyField) Spacer(modifier = Modifier.height(4.dp))
                    hasAnyField = true
                    CustomerDetailLine(
                        icon = Icons.Default.Home,
                        label = "House No.",
                        value = customerHouseNo
                    )
                }

                if (!hasAnyField) {
                    Text(
                        text = "+ Add Customer Info (Optional)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = InkMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerDetailLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = InkMuted,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: ",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = InkMuted
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = InkBlack
        )
    }
}
