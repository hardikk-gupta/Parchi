package com.example.ai

import android.util.Log
import com.example.model.BillItem
import com.example.parser.ParseResult
import com.example.parser.ParsedCustomerInfo
import com.example.parser.VoiceBillParser
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/**
 * Defensive parser for Gemini LLM output that strictly validates JSON schema,
 * eliminates garbled phonetic text, normalizes units via fuzzy matching,
 * and extracts clean, production-grade BillItems.
 */
object GeminiOutputDefensiveParser {

    private const val TAG = "GeminiDefensiveParser"

    // Canonical units recognized in Indian Kirana & Grocery stores
    val CANONICAL_UNITS = setOf(
        "kg", "g", "L", "ml", "pkts", "pcs", "boxes", "bottles", "bags", "tins", "dozen"
    )

    private val UNIT_ALIASES = mapOf(
        "kg" to listOf("kg", "kgs", "kilo", "kilos", "kilogram", "kilograms", "k.g", "k.g.", "k g", "किलो", "किग्रा"),
        "g" to listOf("g", "gm", "gms", "gram", "grams", "grm", "grms", "g.", "ग्राम", "ग्रा"),
        "L" to listOf("l", "lt", "ltr", "ltrs", "litre", "litres", "liter", "liters", "l.", "लीटर", "ली"),
        "ml" to listOf("ml", "mls", "milli", "millilitre", "milliliter", "मिली"),
        "pkts" to listOf("pkt", "pkts", "packet", "packets", "pack", "packs", "pouch", "pouches", "pudhiya", "पैकेट", "पुड़िया", "थैली"),
        "pcs" to listOf("pc", "pcs", "piece", "pieces", "peice", "peices", "nag", "dane", "unit", "units", "पीस", "नग", "दाने"),
        "boxes" to listOf("box", "boxes", "dabba", "dibba", "bax", "डिब्बा", "डब्बा"),
        "bottles" to listOf("bottle", "bottles", "botal", "बॉटल", "बोतल"),
        "bags" to listOf("bag", "bags", "bori", "katta", "sack", "sacks", "बोरी", "कट्टा"),
        "tins" to listOf("tin", "tins", "can", "cans", "टिन", "कैन"),
        "dozen" to listOf("dozen", "dozens", "darjan", "दर्जन")
    )

    // Common filler words and conjunction artifacts to strictly reject
    private val REJECTED_WORDS = setOf(
        "core", "aur", "and", "plus", "with", "sath me", "saath mein",
        "ok", "okay", "alright", "wait", "ek second", "1 second", "ruko",
        "hmmm", "hmm", "umm", "um", "uhh", "uh", "achha", "acha",
        "theek hai", "thik hai", "bas", "bas itna hi", "itna hi", "done", "khatam",
        "item", "items", "null", "undefined", "none", "unknown", "grocery", "something",
        "etc", "sample", "test", "na", "n/a", "yes", "no"
    )

    /**
     * Defensively parses raw text response from Gemini into a validated ParseResult.
     */
    fun parseDefensively(rawText: String, startingSerial: Int = 1): ParseResult? {
        if (rawText.isBlank()) return null

        val cleanJson = extractJsonPayload(rawText) ?: run {
            Log.w(TAG, "No valid JSON payload found in Gemini response")
            return null
        }

        return try {
            if (cleanJson.startsWith("[")) {
                // LLM returned a bare JSON Array of items
                val jsonArray = JSONArray(cleanJson)
                val items = parseItemsArray(jsonArray, startingSerial)
                if (items.isEmpty()) null else ParseResult(items = items)
            } else {
                // LLM returned a JSON Object matching our requested schema
                val jsonObject = JSONObject(cleanJson)
                val itemsArray = jsonObject.optJSONArray("items") ?: JSONArray()
                val items = parseItemsArray(itemsArray, startingSerial)

                val customerName = sanitizeCustomerField(jsonObject.optString("customerName"))
                val customerPhone = sanitizeCustomerPhone(jsonObject.optString("customerPhone"))
                val customerHouseNo = sanitizeCustomerField(jsonObject.optString("customerHouseNo"))

                if (items.isEmpty() && customerName == null && customerPhone == null && customerHouseNo == null) {
                    null
                } else {
                    ParseResult(
                        items = items,
                        customerInfo = ParsedCustomerInfo(
                            name = customerName,
                            phone = customerPhone,
                            houseNo = customerHouseNo
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in defensive parsing of Gemini JSON: ${e.message}", e)
            null
        }
    }

    /**
     * Safely locates and extracts the JSON block from LLM output,
     * stripping markdown fences and pre/post explanatory chatter.
     */
    fun extractJsonPayload(raw: String): String? {
        var text = raw.trim()

        // Remove markdown code fences if present
        if (text.contains("```")) {
            val fenceMatch = Regex("""```(?:json)?\s*([\s\S]*?)\s*```""").find(text)
            if (fenceMatch != null) {
                text = fenceMatch.groupValues[1].trim()
            } else {
                text = text.replace("```json", "").replace("```", "").trim()
            }
        }

        // Locate outermost matching JSON bounds
        val firstBrace = text.indexOf('{')
        val firstBracket = text.indexOf('[')

        val startIndex = when {
            firstBrace != -1 && firstBracket != -1 -> minOf(firstBrace, firstBracket)
            firstBrace != -1 -> firstBrace
            firstBracket != -1 -> firstBracket
            else -> -1
        }

        if (startIndex == -1) return null

        val isObject = text[startIndex] == '{'
        val endIndex = if (isObject) text.lastIndexOf('}') else text.lastIndexOf(']')

        if (endIndex == -1 || endIndex <= startIndex) return null

        return text.substring(startIndex, endIndex + 1).trim()
    }

    /**
     * Iterates over JSON items array and validates each item defensively.
     */
    private fun parseItemsArray(itemsArray: JSONArray, startingSerial: Int): List<BillItem> {
        val validItems = mutableListOf<BillItem>()

        for (i in 0 until itemsArray.length()) {
            val itemObj = itemsArray.optJSONObject(i) ?: continue
            val validatedItem = validateAndSanitizeItem(itemObj, startingSerial + validItems.size)
            if (validatedItem != null) {
                validItems.add(validatedItem)
            }
        }

        return validItems
    }

    /**
     * Validates an individual item object against defensive sanity and schema checks.
     */
    fun validateAndSanitizeItem(itemObj: JSONObject, serialNumber: Int): BillItem? {
        val rawName = (itemObj.optString("name").ifBlank {
            itemObj.optString("itemName").ifBlank {
                itemObj.optString("item")
            }
        }).trim()

        if (isGarbledOrFiller(rawName)) {
            Log.d(TAG, "Rejected garbled/filler item name: '$rawName'")
            return null
        }

        val rawQty = itemObj.optString("quantity").ifBlank {
            itemObj.optString("qty").ifBlank {
                itemObj.optString("count")
            }
        }.trim()

        val rawUnit = itemObj.optString("unit").ifBlank {
            itemObj.optString("uom")
        }.trim()

        val rawWeightOrQty = itemObj.optString("weightOrQuantity").trim()

        // Normalize quantity and unit defensively with fuzzy matching
        val (finalQty, finalUnit) = resolveQuantityAndUnit(rawQty, rawUnit, rawWeightOrQty, rawName)

        val formattedWeightOrQty = formatWeightOrQuantity(finalQty, finalUnit)

        // Parse price safely
        val price = parsePrice(itemObj)

        // Normalize name using Kirana dictionary and phonetic cleanups
        val normalizedName = cleanAndNormalizeItemName(rawName)
        if (normalizedName.isBlank() || isGarbledOrFiller(normalizedName)) {
            return null
        }

        return BillItem(
            serialNumber = serialNumber,
            itemName = normalizedName,
            weightOrQuantity = formattedWeightOrQty,
            price = price,
            isVerified = false
        )
    }

    /**
     * Defensively resolves and sanitizes quantity and unit pairs,
     * applying fuzzy matching for units and handling embedded numbers.
     */
    fun resolveQuantityAndUnit(
        rawQty: String?,
        rawUnit: String?,
        rawWeightOrQty: String?,
        itemName: String
    ): Pair<String, String> {
        var qty = rawQty?.trim() ?: ""
        var unit = rawUnit?.trim() ?: ""

        // Case 1: Quantity has embedded unit e.g. "10kg", "500g", "2pkts", "1.5L"
        if (qty.isNotBlank() && unit.isBlank()) {
            val splitRegex = Regex("""^(\d+(?:\.\d+)?)\s*([a-zA-Z\u0900-\u097F\.]+)$""")
            val match = splitRegex.find(qty)
            if (match != null) {
                qty = match.groupValues[1]
                unit = match.groupValues[2]
            }
        }

        // Case 2: Use rawWeightOrQty fallback if both are blank
        if (qty.isBlank() && unit.isBlank() && !rawWeightOrQty.isNullOrBlank()) {
            val weightMatch = Regex("""^(\d+(?:\.\d+)?)\s*(.*)$""").find(rawWeightOrQty.trim())
            if (weightMatch != null) {
                qty = weightMatch.groupValues[1]
                unit = weightMatch.groupValues[2]
            } else {
                qty = "1"
                unit = rawWeightOrQty.trim()
            }
        }

        // Case 3: Quantity contains words like "half", "quarter", "aadha", "paav"
        when (qty.lowercase(Locale.ROOT)) {
            "half", "aadha", "adha", "आधा" -> {
                qty = "500"
                if (unit.isBlank() || unit.equals("kg", ignoreCase = true) || unit.equals("kilo", ignoreCase = true)) {
                    unit = "g"
                }
            }
            "quarter", "paav", "pao", "पाव" -> {
                qty = "250"
                if (unit.isBlank() || unit.equals("kg", ignoreCase = true) || unit.equals("kilo", ignoreCase = true)) {
                    unit = "g"
                }
            }
            "dedh", "derh", "डेढ़" -> {
                qty = "1.5"
                if (unit.isBlank()) unit = "kg"
            }
            "dhai", "ढाई" -> {
                qty = "2.5"
                if (unit.isBlank()) unit = "kg"
            }
        }

        // Fuzzy match and standardize unit
        val matchedUnit = fuzzyMatchUnit(unit)

        // Sanitize numeric quantity
        val cleanQty = qty.filter { it.isDigit() || it == '.' }.let {
            if (it.isBlank() || it == "." || it == "0") "1" else it
        }

        val finalUnit = matchedUnit ?: if (unit.isNotBlank()) unit else "pcs"

        return Pair(cleanQty, finalUnit)
    }

    /**
     * Formats normalized quantity and unit into clean display text.
     */
    fun formatWeightOrQuantity(quantity: String, unit: String): String {
        val q = quantity.ifBlank { "1" }
        val u = unit.trim()

        return when {
            u.isBlank() -> "$q pcs"
            q.endsWith(u, ignoreCase = true) -> q
            u.equals("pcs", ignoreCase = true) || u.equals("pkts", ignoreCase = true) -> "$q $u"
            else -> "$q $u"
        }
    }

    /**
     * Fuzzy matches a candidate unit string against canonical units.
     * Handles aliases, typos (Levenshtein distance <= 1), and Devanagari units.
     */
    fun fuzzyMatchUnit(rawUnit: String): String? {
        val clean = rawUnit.lowercase(Locale.ROOT)
            .replace(".", "")
            .replace("-", "")
            .trim()

        if (clean.isBlank()) return null

        // 1. Exact match with canonical units
        if (CANONICAL_UNITS.contains(clean)) return clean

        // 2. Exact match with alias dictionary
        for ((canonical, aliases) in UNIT_ALIASES) {
            if (aliases.any { it.equals(clean, ignoreCase = true) }) {
                return canonical
            }
        }

        // 3. Prefix / Substring checks
        when {
            clean.startsWith("kilo") || clean.startsWith("kg") -> return "kg"
            clean.startsWith("gram") || clean.startsWith("gm") -> return "g"
            clean.startsWith("liter") || clean.startsWith("litre") || clean.startsWith("ltr") -> return "L"
            clean.startsWith("milli") || clean.startsWith("ml") -> return "ml"
            clean.startsWith("pack") || clean.startsWith("pkt") || clean.startsWith("pouch") -> return "pkts"
            clean.startsWith("piece") || clean.startsWith("pc") -> return "pcs"
            clean.startsWith("bott") -> return "bottles"
            clean.startsWith("box") || clean.startsWith("dabb") -> return "boxes"
            clean.startsWith("doz") || clean.startsWith("darj") -> return "dozen"
        }

        // 4. Levenshtein fuzzy distance matching (distance <= 1)
        for ((canonical, aliases) in UNIT_ALIASES) {
            for (alias in aliases) {
                if (alias.length >= 3 && levenshteinDistance(clean, alias) <= 1) {
                    return canonical
                }
            }
        }

        return null
    }

    /**
     * Detects if an item name or string is garbled noise, pure symbols, or a filler word.
     */
    fun isGarbledOrFiller(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.length < 2) return true

        val lower = trimmed.lowercase(Locale.ROOT)

        // Exact match with blacklist
        if (REJECTED_WORDS.contains(lower)) return true

        // Reject if pure symbols, numbers, or whitespace
        val letterCount = trimmed.count { it.isLetter() }
        if (letterCount < 2) return true

        // Reject if symbol density is unusually high (> 40% symbols)
        val symbolCount = trimmed.count { !it.isLetterOrDigit() && !it.isWhitespace() && it != '-' }
        if (symbolCount.toFloat() / trimmed.length > 0.40f) return true

        // Reject excessive character repetition (e.g. "aaaaa", "zzzzz", "----")
        if (Regex("""(.)\1{3,}""").containsMatchIn(trimmed)) return true

        // Reject consonant-only gibberish of length >= 5 (e.g. "bcdfgh", "qwrtyp")
        val isEnglishOnly = trimmed.all { it in 'a'..'z' || it in 'A'..'Z' || it.isWhitespace() }
        if (isEnglishOnly && trimmed.length >= 5) {
            val vowels = setOf('a', 'e', 'i', 'o', 'u', 'y')
            val hasVowel = trimmed.any { vowels.contains(it.lowercaseChar()) }
            if (!hasVowel) return true
        }

        return false
    }

    /**
     * Cleans and normalizes item name using Kirana dictionary and phonetic normalization.
     */
    fun cleanAndNormalizeItemName(rawName: String): String {
        val cleaned = VoiceBillParser.cleanItemName(rawName)
        if (cleaned.isNotBlank()) {
            return cleaned.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
            }
        }

        // Fallback title-casing
        return rawName.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }
    }

    /**
     * Computes classic Levenshtein distance between two strings.
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        val a = s1.lowercase(Locale.ROOT)
        val b = s2.lowercase(Locale.ROOT)

        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j

        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,       // deletion
                    dp[i][j - 1] + 1,       // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dp[a.length][b.length]
    }

    private fun parsePrice(itemObj: JSONObject): Double? {
        if (itemObj.isNull("price") || !itemObj.has("price")) return null
        val p = itemObj.optDouble("price")
        return if (p.isNaN() || p <= 0.0) null else p
    }

    private fun sanitizeCustomerField(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isBlank() || trimmed.equals("null", ignoreCase = true) || isGarbledOrFiller(trimmed)) {
            return null
        }
        return trimmed
    }

    private fun sanitizeCustomerPhone(raw: String): String? {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.length == 10 && digits.first() in '6'..'9' -> digits
            digits.length > 10 && digits.takeLast(10).first() in '6'..'9' -> digits.takeLast(10)
            else -> null
        }
    }
}
