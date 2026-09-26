package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.model.BillItem

object ReceiptShareHelper {

    fun formatTextReceipt(
        storeName: String,
        billNumber: String,
        dateTime: String,
        customerName: String,
        customerPhone: String,
        customerHouseNo: String,
        items: List<BillItem>,
        totalAmount: Double
    ): String {
        val sb = StringBuilder()
        sb.append("🧾 *${storeName.uppercase()}*\n")
        sb.append("Parchi Cash Receipt\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📅 $dateTime • #$billNumber\n")

        if (customerName.isNotBlank()) {
            sb.append("👤 Customer: $customerName\n")
        }
        if (customerPhone.isNotBlank()) {
            sb.append("📱 Mobile: $customerPhone\n")
        }
        if (customerHouseNo.isNotBlank()) {
            sb.append("🏠 House/Flat: $customerHouseNo\n")
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append(String.format("%-4s %-16s %-8s %s\n", "No.", "Item", "Qty/Wt", "Price"))
        sb.append("─────────────────────────\n")

        for (item in items) {
            val priceStr = item.price?.let { "₹${if (it % 1.0 == 0.0) it.toInt() else String.format("%.2f", it)}" } ?: "   "
            val check = if (item.isVerified) "✓ " else ""
            sb.append("${item.serialNumber}. $check${item.itemName} (${item.weightOrQuantity}) : $priceStr\n")
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        val formattedTotal = if (totalAmount > 0.0) {
            "₹${if (totalAmount % 1.0 == 0.0) totalAmount.toInt().toString() else String.format("%.2f", totalAmount)}"
        } else {
            ""
        }
        sb.append("💰 *TOTAL: $formattedTotal*\n")
        sb.append("✅ PARCHI Verified\n")
        sb.append("🙏 Thank you for shopping with us!\n")

        return sb.toString()
    }

    fun shareReceipt(
        context: Context,
        storeName: String,
        billNumber: String,
        dateTime: String,
        customerName: String,
        customerPhone: String,
        customerHouseNo: String,
        items: List<BillItem>,
        totalAmount: Double
    ) {
        val message = formatTextReceipt(
            storeName = storeName,
            billNumber = billNumber,
            dateTime = dateTime,
            customerName = customerName,
            customerPhone = customerPhone,
            customerHouseNo = customerHouseNo,
            items = items,
            totalAmount = totalAmount
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Grocery Bill #$billNumber")
            putExtra(Intent.EXTRA_TEXT, message)
        }
        val chooser = Intent.createChooser(intent, "Share Bill Receipt")
        context.startActivity(chooser)
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Grocery Bill", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Bill copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
}
