package com.example.parser

import android.util.Log
import com.example.model.BillItem
import java.util.Locale

data class ParsedCustomerInfo(
    val name: String? = null,
    val phone: String? = null,
    val houseNo: String? = null
)

data class ParseResult(
    val items: List<BillItem> = emptyList(),
    val customerInfo: ParsedCustomerInfo = ParsedCustomerInfo()
)

object VoiceBillParser {

    private const val TAG = "VoiceBillParser"

    // Recognized units for quantities (supports ASCII and Devanagari Unicode word boundaries)
    val UNIT_KEYWORD_PATTERN =
        """(?iu)(?:^|\b)(\d+(?:\.\d+)?)\s*(kg|kgs|kilo|kilos|kilogram|kilograms|gm|gms|gram|grams|g|litre|litres|liter|liters|lt|l|ml|packet|packets|pkt|pkts|pouch|pouches|pudhiya|piece|pieces|pc|pcs|nag|dane|dozen|dozens|darjan|bottle|bottles|botal|box|boxes|dabba|dibba|tin|tins|can|cans|bori|katta|bag|bags|किलो|किग्रा|ग्राम|ग्रा|लीटर|ली|मिली|पैकेट|पुड़िया|पीस|नग|दाने|दर्जन|बोतल|डिब्बा|डब्बा|टिन|कैन|बोरी|कट्टा|थैली)(?:\b|(?<=[\u0900-\u097F])|(?=[\u0900-\u097F]))"""

    private val UNIT_REGEX = UNIT_KEYWORD_PATTERN.toRegex()

    // Pattern for "50 rupaye ka dahi" or "10 rs wala biscuit" or "50 रुपये का दही"
    private val HINDI_PRICE_LEAD_REGEX =
        """(?i)^(\d+(?:\.\d+)?)\s*(?:rupaye|rupee|rupees|rs|inr|₹|रुपये|रुपए|रुपिया|रु)\s*(?:ka|ki|ke|wala|wali|wale|का|की|के|वाला|वाली|वाले)\s+(.+)$""".toRegex()

    // Standalone count at start, e.g. "2 brush" or "3 soaps" or "2 bread"
    private val LEADING_COUNT_REGEX = """(?i)^(\d+)\s+([a-zA-Z\u0900-\u097F\s\-]+)""".toRegex()

    // Standalone count at end, e.g. "brush 2" or "brush 2 pcs"
    private val TRAILING_COUNT_REGEX = """(?i)^([a-zA-Z\u0900-\u097F\s\-]+?)\s+(\d+)\s*(?:piece|pieces|pc|pcs|nag|dane|पीस|नग)?$""".toRegex()

    // Explicit price pattern: trailing currency or rate keywords
    private val EXPLICIT_PRICE_REGEX =
        """(?i)(?:(?:rs\.?|rupees|rupaye|inr|₹|रुपये|रुपए|रुपिया|रु|price|rate|bhav|भाव|दाम|दर)\s*(\d+(?:\.\d+)?)|(\d+(?:\.\d+)?)\s*(?:rs\.?|rupees|rupaye|inr|₹|रुपये|रुपए|रुपिया|रु|ka|ki|का|की|\/-))\s*$""".toRegex()

    // Trailing bare price, e.g. "10 kg atta 356"
    private val TRAILING_BARE_PRICE_REGEX = """(?i)\s+(\d+(?:\.\d+)?)\s*$""".toRegex()

    private val THINKING_FILLER_WORDS = setOf(
        "okay", "ok", "alright", "all right",
        "hmmm", "hmm", "ummm", "umm", "um", "uhh", "uh",
        "wait", "wait a second", "wait a minute", "let me see", "let's see", "let me think", "what else",
        "second", "seconds", "minute", "minutes",
        "achha", "acha", "theek hai", "thik hai", "haan", "haa", "ha",
        "matlab", "matlab ki", "ek second", "1 second", "ek minute", "1 minute", "ruko", "rukiye", "socho", "dekhne do", "dekho",
        "aur kya tha", "kya tha", "aur kya", "aur kya chahiye", "kya bole",
        "bhaiya", "bhai", "sir", "madam", "sunno", "suno", "listen",
        "ek kaam karo", "ek kaam kijiye", "aur batao",
        "bas itna hi", "itna hi", "bas", "ho gaya", "done", "that's it", "that is all", "khatam"
    )

    // Phonetic STT mishearing dictionary for Indian Kirana grocery items
    private val PHONETIC_KIRANA_MAP = mapOf(
        "aa" to "Atta",
        "aata" to "Atta",
        "ata" to "Atta",
        "aataa" to "Atta",
        "atta" to "Atta",
        "आटा" to "Atta",

        "tama" to "Rajma",
        "ragma" to "Rajma",
        "rajma" to "Rajma",
        "raj ma" to "Rajma",
        "razma" to "Rajma",
        "rajmah" to "Rajma",
        "राजमा" to "Rajma",

        "ida" to "Maida",
        "maida" to "Maida",
        "meda" to "Maida",
        "mayda" to "Maida",
        "mida" to "Maida",
        "मैदा" to "Maida",

        "sugar" to "Sugar",
        "cheeni" to "Sugar",
        "chini" to "Sugar",
        "shakar" to "Sugar",
        "sakkar" to "Sugar",
        "चीनी" to "Sugar",
        "शक्कर" to "Sugar",

        "sooji" to "Suji",
        "suji" to "Suji",
        "rawa" to "Suji",
        "rava" to "Suji",
        "सूजी" to "Suji",
        "रवा" to "Suji",

        "besan" to "Besan",
        "baisan" to "Besan",
        "basan" to "Besan",
        "बेसन" to "Besan",

        "chawal" to "Rice",
        "rice" to "Rice",
        "chawl" to "Rice",
        "basmati" to "Basmati Rice",
        "चावल" to "Rice",

        "poha" to "Poha",
        "pooha" to "Poha",
        "पोहा" to "Poha",

        "moong dal" to "Moong Dal",
        "mung dal" to "Moong Dal",
        "toor dal" to "Toor Dal",
        "arhar dal" to "Arhar Dal",
        "chana dal" to "Chana Dal",
        "urad dal" to "Urad Dal",
        "masoor dal" to "Masoor Dal",
        "daal" to "Dal",
        "dal" to "Dal",
        "दाल" to "Dal",

        "sarson tel" to "Mustard Oil",
        "sarso tel" to "Mustard Oil",
        "mustard oil" to "Mustard Oil",
        "sarson" to "Mustard Oil",
        "sarso" to "Mustard Oil",
        "refine oil" to "Refined Oil",
        "refined oil" to "Refined Oil",
        "refine" to "Refined Oil",
        "tel" to "Oil",
        "oil" to "Oil",
        "ghee" to "Ghee",
        "desi ghee" to "Desi Ghee",
        "तेल" to "Oil",
        "घी" to "Ghee",

        "namak" to "Salt",
        "salt" to "Salt",
        "tata namak" to "Tata Salt",
        "नमक" to "Salt",

        "haldi" to "Haldi",
        "turmeric" to "Haldi",
        "हल्दी" to "Haldi",

        "mirch" to "Mirch Powder",
        "chilli" to "Chilli Powder",
        "mirchi" to "Mirch Powder",
        "मिर्च" to "Mirch Powder",

        "dhaniya" to "Dhaniya Powder",
        "coriander" to "Dhaniya Powder",
        "धनिया" to "Dhaniya Powder",

        "jeera" to "Jeera",
        "zeera" to "Jeera",
        "cumin" to "Jeera",
        "जीरा" to "Jeera",

        "garam masala" to "Garam Masala",
        "masala" to "Masala",
        "मसाला" to "Masala",

        "sabun" to "Soap",
        "soap" to "Soap",
        "lux" to "Lux Soap",
        "dettol" to "Dettol Soap",
        "lifebuoy" to "Lifebuoy Soap",
        "surf" to "Surf Excel",
        "surf excel" to "Surf Excel",
        "tide" to "Tide",
        "wheel" to "Wheel",
        "ariel" to "Ariel",
        "vim bar" to "Vim Bar",
        "vim" to "Vim",
        "साबुन" to "Soap",

        "doodh" to "Milk",
        "milk" to "Milk",
        "dahi" to "Curd",
        "curd" to "Curd",
        "paneer" to "Paneer",
        "butter" to "Butter",
        "makhan" to "Butter",
        "दूध" to "Milk",
        "दही" to "Curd",
        "पनीर" to "Paneer",

        "chai" to "Tea",
        "chai patti" to "Tea",
        "tea" to "Tea",
        "red label" to "Red Label Tea",
        "tata tea" to "Tata Tea",
        "चाय" to "Tea",

        "biscuit" to "Biscuits",
        "biscuits" to "Biscuits",
        "parle g" to "Parle-G",
        "good day" to "Good Day Biscuit",
        "marie" to "Marie Gold",
        "oreo" to "Oreo",
        "rusk" to "Rusk",
        "toast" to "Toast",
        "bread" to "Bread",
        "ande" to "Eggs",
        "eggs" to "Eggs",
        "egg" to "Eggs",
        "maggi" to "Maggi",
        "मैगी" to "Maggi"
    )

    /**
     * Parses raw spoken transcript into grocery bill items and optional customer info.
     * Accurately segments multi-item utterances without splitting or cutting words in half.
     */
    fun parseTranscript(transcript: String, startingSerial: Int = 1): ParseResult {
        if (transcript.isBlank()) return ParseResult()

        return try {
            val normalized = normalizeSpeechText(transcript.trim())
            val (customerInfo, cleanItemSpeech) = extractAndRemoveCustomerInfo(normalized)

            if (cleanItemSpeech.isBlank()) {
                return ParseResult(items = emptyList(), customerInfo = customerInfo)
            }

            // Remove terminal fillers and trailing "core" / "aur" / "and"
            var speech = cleanItemSpeech
                .replace(Regex("""(?i)\b(?:bas\s+itna\s+hi|itna\s+hi|bas|ho\s+gaya|done|that's\s+it|that\s+is\s+all|khatam|bas\s+yehi|core|aur|and)\s*$"""), "")
                .trim()

            // 1. Initial split on commas, newlines, explicit conjunctions
            val splitRegex = Regex("""(?i),\s*|\n+|\s+and\s+|\s+aur\s+|\s+core\s+|\s+और\s+|\s+तथा\s+|\s+एवं\s+|\s+plus\s+|\s+then\s+|\s+phir\s+|\s+फिर\s+|\s+saath\s+mein\s+|\s+sath\s+me\s+|\s+bhi\s+|\s+(?:okay|ok|wait|ek\s+second|1\s+second|achha|acha|theek\s+hai)\s+""")
            val primaryClauses = speech.split(splitRegex)
                .map { it.trim() }
                .filter { it.isNotBlank() }

            val itemClauses = mutableListOf<String>()

            // 2. Further segment clauses containing multiple items without conjunctions
            for (clause in primaryClauses) {
                val subItems = segmentConsecutiveItems(clause)
                itemClauses.addAll(subItems)
            }

            val parsedItems = mutableListOf<BillItem>()
            var currentSerial = startingSerial

            for (clause in itemClauses) {
                try {
                    val item = parseSingleItemClause(clause, currentSerial)
                    if (item != null) {
                        val lastItem = parsedItems.lastOrNull()
                        val isDuplicate = lastItem != null &&
                                lastItem.itemName.equals(item.itemName, ignoreCase = true) &&
                                lastItem.weightOrQuantity == item.weightOrQuantity

                        if (!isDuplicate) {
                            parsedItems.add(item)
                            currentSerial++
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing clause: $clause", e)
                }
            }

            // Fallback for single phrase with no unit
            if (parsedItems.isEmpty() && speech.isNotBlank()) {
                val cleaned = cleanItemName(speech)
                if (cleaned.isNotBlank()) {
                    parsedItems.add(
                        BillItem(
                            serialNumber = startingSerial,
                            itemName = cleaned,
                            weightOrQuantity = "1 item",
                            price = null,
                            isVerified = false
                        )
                    )
                }
            }

            ParseResult(items = parsedItems, customerInfo = customerInfo)
        } catch (e: Throwable) {
            Log.e(TAG, "Fatal parsing exception caught safely", e)
            ParseResult(
                items = listOf(
                    BillItem(
                        serialNumber = startingSerial,
                        itemName = cleanItemName(transcript).ifEmpty { "Grocery Item" },
                        weightOrQuantity = "1 item",
                        price = null,
                        isVerified = false
                    )
                )
            )
        }
    }

    /**
     * Accurately segments consecutive grocery items spoken in a single sentence without conjunctions.
     * e.g. "10 kg atta 5 kg rajma 2 kg maida 3 kg sugar"
     * -> ["10 kg atta", "5 kg rajma", "2 kg maida", "3 kg sugar"]
     */
    fun segmentConsecutiveItems(clause: String): List<String> {
        val trimmed = clause.trim()
        val unitMatches = UNIT_REGEX.findAll(trimmed).toList()

        if (unitMatches.size <= 1) {
            // Check count-based consecutive items: e.g. "2 brush 3 sabun 5 bread"
            val countPattern = Regex("""(?i)(?:^|\s+)(\d+)\s+([a-zA-Z\u0900-\u097F]+)""")
            val countMatches = countPattern.findAll(trimmed).toList()
            if (countMatches.size > 1 && unitMatches.isEmpty()) {
                val result = mutableListOf<String>()
                for (i in 0 until countMatches.size) {
                    val start = countMatches[i].range.first
                    val end = if (i + 1 < countMatches.size) countMatches[i + 1].range.first else trimmed.length
                    val sub = trimmed.substring(start, end).trim()
                    if (sub.isNotBlank()) result.add(sub)
                }
                return result
            }
            return listOf(trimmed)
        }

        // Determine if quantities are leading (e.g. "10 kg atta 5 kg rajma")
        // or trailing (e.g. "atta 10 kg rajma 5 kg")
        val firstMatch = unitMatches.first()
        val textBeforeFirst = trimmed.substring(0, firstMatch.range.first).trim()
        val hasLeadingItemName = textBeforeFirst.any { it.isLetter() }

        val segments = mutableListOf<String>()

        if (!hasLeadingItemName) {
            // Case A: Leading Quantities ("10 kg atta 5 kg rajma 2 kg maida")
            // Each item starts at the beginning of its unit match (e.g. "10 kg", "5 kg")
            // and ends right before the start of the NEXT unit match!
            for (i in 0 until unitMatches.size) {
                val start = unitMatches[i].range.first
                val end = if (i + 1 < unitMatches.size) unitMatches[i + 1].range.first else trimmed.length
                val sub = trimmed.substring(start, end).trim()
                if (sub.isNotBlank()) {
                    segments.add(sub)
                }
            }
        } else {
            // Case B: Trailing Quantities ("atta 10 kg rajma 5 kg maida 2 kg")
            // Each item ends after its unit match (plus optional price number immediately following it)
            var currentStart = 0
            for (i in 0 until unitMatches.size) {
                val currentUnit = unitMatches[i]
                var itemEnd = currentUnit.range.last + 1

                // Check if an immediate price number follows the unit: e.g. "atta 10 kg 350 rajma 5 kg"
                val remainder = trimmed.substring(itemEnd).trimStart()
                val pricePrefixMatch = Regex("""^(\d+(?:\.\d+)?)\s*(?:rupaye|rupees|rs|₹|/-)?(?:\s+|$)""").find(remainder)
                if (pricePrefixMatch != null) {
                    val priceOffset = trimmed.indexOf(pricePrefixMatch.value, itemEnd)
                    if (priceOffset != -1) {
                        itemEnd = priceOffset + pricePrefixMatch.value.length
                    }
                }

                val sub = trimmed.substring(currentStart, itemEnd).trim()
                if (sub.isNotBlank()) {
                    segments.add(sub)
                }
                currentStart = itemEnd
            }
            if (currentStart < trimmed.length) {
                val leftover = trimmed.substring(currentStart).trim()
                if (leftover.isNotBlank()) {
                    segments.add(leftover)
                }
            }
        }

        return segments
    }

    /**
     * Normalizes numbers and Hindi/English quantities.
     */
    fun normalizeSpeechText(text: String): String {
        var s = text

        // Convert Devanagari numerals (०, १, २, ३, ४, ५, ६, ७, ८, ९) to ASCII digits (0-9)
        s = buildString(s.length) {
            for (ch in s) {
                if (ch in '\u0966'..'\u096F') {
                    append((ch - '\u0966' + '0'.code).toChar())
                } else {
                    append(ch)
                }
            }
        }

        // Fractional quantities (English + Hindi)
        s = s.replace(Regex("""(?i)\b(?:half|aadha|adha|आधा)\s*(?:kg|kilo|किलो|किग्रा)\b"""), "500 g")
        s = s.replace(Regex("""(?i)\b(?:half|aadha|adha|आधा)\s*(?:litre|liter|l|लीटर)\b"""), "500 ml")
        s = s.replace(Regex("""(?i)\b(?:quarter|paav|pao|paw|पाव|एक\s+पाव)\s*(?:kg|kilo|किलो)?\b"""), "250 g")
        s = s.replace(Regex("""(?i)\b(?:one\s+and\s+a\s+half|dedh|derh|डेढ़)\s*(?:kg|kilo|किलो)\b"""), "1.5 kg")
        s = s.replace(Regex("""(?i)\b(?:one\s+and\s+a\s+half|dedh|derh|डेढ़)\s*(?:litre|liter|l|लीटर)\b"""), "1.5 L")
        s = s.replace(Regex("""(?i)\b(?:two\s+and\s+a\s+half|dhai|ढाई)\s*(?:kg|kilo|किलो)\b"""), "2.5 kg")
        s = s.replace(Regex("""(?i)\b(?:dhai\s+sau|dhaisau|ढाई\s*सौ)\s*(?:gram|gm|g|ग्राम)?\b"""), "250 g")
        s = s.replace(Regex("""(?i)\b(?:saadhe\s+teen|sadhe\s+teen|साढ़े\s*तीन)\s*(?:kg|kg|किलो)\b"""), "3.5 kg")

        // Compound numbers
        s = s.replace(Regex("""(?i)\btwenty[\s\-]five\b"""), "25")
        s = s.replace(Regex("""(?i)\btwenty[\s\-]two\b"""), "22")
        s = s.replace(Regex("""(?i)\bthirty[\s\-]five\b"""), "35")

        // English and Hindi numbers dictionary
        val numberWords = listOf(
            "one" to "1", "ek" to "1", "एक" to "1",
            "two" to "2", "do" to "2", "दो" to "2",
            "three" to "3", "teen" to "3", "तीन" to "3",
            "four" to "4", "chaar" to "4", "char" to "4", "चार" to "4",
            "five" to "5", "paanch" to "5", "panch" to "5", "पांच" to "5",
            "six" to "6", "chhe" to "6", "che" to "6", "छह" to "6",
            "seven" to "7", "saat" to "7", "सात" to "7",
            "eight" to "8", "aath" to "8", "आठ" to "8",
            "nine" to "9", "nau" to "9", "नौ" to "9",
            "ten" to "10", "das" to "10", "दस" to "10",
            "eleven" to "11", "gyarah" to "11", "ग्यारह" to "11",
            "twelve" to "12", "barah" to "12", "बारह" to "12",
            "twenty" to "20", "bees" to "20", "बीस" to "20",
            "twenty-five" to "25", "pachees" to "25", "पच्चीस" to "25",
            "thirty" to "30", "tees" to "30", "तीस" to "30",
            "fifty" to "50", "pachaas" to "50", "पचास" to "50",
            "hundred" to "100", "sau" to "100", "सौ" to "100"
        )

        for ((word, digit) in numberWords) {
            s = s.replace(
                Regex("""(?i)\b$word\b(?=\s+(?:kg|kgs|kilo|gram|gm|g|packet|pkt|pkts|pouch|litre|liter|l|ml|piece|pieces|pc|pcs|botal|bottle|dabba|box|bori|rupaye|rs|rupees|₹|किलो|किग्रा|ग्राम|लीटर|पैकेट|पीस|बोतल|डिब्बा|बोरी|रुपये|रुपए|[a-zA-Z\u0900-\u097F]))"""),
                digit
            )
        }

        return s
    }

    /**
     * Extracts customer info safely.
     */
    private fun extractAndRemoveCustomerInfo(text: String): Pair<ParsedCustomerInfo, String> {
        var remaining = text
        var name: String? = null
        var phone: String? = null
        var houseNo: String? = null

        try {
            // 1. Phone / Mobile (10-digit Indian mobile number)
            val phonePattern = """(?i)\b(?:mobile|phone|contact|number|ph|telephone|telephon|call)\s*(?:is|no|number|:)?\s*(?:\+?91[\-\s]?)?([6-9]\d{9})\b""".toRegex()
            val phoneMatch = phonePattern.find(remaining)
            if (phoneMatch != null) {
                phone = phoneMatch.groupValues[1]
                remaining = remaining.removeRange(phoneMatch.range).trim()
            } else {
                val barePhone = """\b([6-9]\d{9})\b""".toRegex().find(remaining)
                if (barePhone != null) {
                    phone = barePhone.groupValues[1]
                    remaining = remaining.removeRange(barePhone.range).trim()
                }
            }

            // 2. House / Flat / Address
            val housePattern = """(?i)\b(?:house|flat|room|door|makan)\s*(?:number|no|#)?\s*([a-zA-Z0-9\-\/]+)""".toRegex()
            val houseMatch = housePattern.find(remaining)
            if (houseMatch != null) {
                val candidateHouse = houseMatch.groupValues[1]
                if (candidateHouse.isNotBlank() && !candidateHouse.equals("is", ignoreCase = true) && !candidateHouse.equals("ka", ignoreCase = true)) {
                    houseNo = candidateHouse
                    remaining = remaining.removeRange(houseMatch.range).trim()
                }
            }

            // 3. Customer Name
            val namePattern = """(?i)\b(?:grahak\s+ka\s+naam|grahak\s+naam|grahak|customer\s+name|customer's\s+name|customer|naam\s+hai|naam)\s*[:\-]?\s*([a-zA-Z\u0900-\u0963\u0972-\u097F]+(?:\s+[a-zA-Z\u0900-\u0963\u0972-\u097F]+)?)""".toRegex()
            val nameMatch = namePattern.find(remaining)
            if (nameMatch != null) {
                val candidate = nameMatch.groupValues[1].trim()
                val reservedGroceryTerms = setOf(
                    "atta", "sugar", "cheeni", "rice", "chawal", "dal", "daal", "oil", "tel", "sarson",
                    "milk", "doodh", "tea", "chai", "soap", "sabun", "biscuit", "namak", "salt", "brush",
                    "rajma", "maida", "suji", "besan", "poha",
                    "kg", "kilo", "gram", "gm", "packet", "pkt", "rupaye", "rs", "rupees", "bhav", "rate",
                    "hai", "ka", "ki", "ke", "ko"
                )
                val words = candidate.split(Regex("""\s+""")).filter { it.isNotBlank() && it.all { ch -> ch.isLetter() } }
                val isReserved = words.any { reservedGroceryTerms.contains(it.lowercase(Locale.ROOT)) }
                if (!isReserved && words.isNotEmpty()) {
                    name = words.joinToString(" ") { word ->
                        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                    }
                    remaining = remaining.removeRange(nameMatch.range).trim()
                }
            }

            remaining = remaining.replace(Regex("""^[,\s\-\–]+"""), "")
                .replace(Regex("""[,\s\-\–]+$"""), "")
                .trim()
        } catch (e: Exception) {
            Log.w(TAG, "Error in extractAndRemoveCustomerInfo", e)
        }

        return Pair(ParsedCustomerInfo(name = name, phone = phone, houseNo = houseNo), remaining)
    }

    /**
     * Parses a single item clause safely.
     */
    fun parseSingleItemClause(clause: String, serialNumber: Int): BillItem? {
        var text = clause.trim()
        if (text.isBlank()) return null

        text = stripThinkingFillers(text)
        if (text.isBlank()) return null

        // Check Pattern A: "50 rupaye ka dahi" or "10 rs wala biscuit"
        val hindiPriceLeadMatch = HINDI_PRICE_LEAD_REGEX.find(text)
        if (hindiPriceLeadMatch != null) {
            val priceVal = hindiPriceLeadMatch.groupValues[1].toDoubleOrNull()
            val itemNameRaw = cleanItemName(hindiPriceLeadMatch.groupValues[2])
            if (itemNameRaw.isNotBlank()) {
                return BillItem(
                    serialNumber = serialNumber,
                    itemName = itemNameRaw,
                    weightOrQuantity = "1 item",
                    price = priceVal,
                    isVerified = false
                )
            }
        }

        var price: Double? = null

        // Explicit price ("280 rupees", "280 rupaye", "rs 356", "₹45")
        val explicitMatch = EXPLICIT_PRICE_REGEX.find(text)
        if (explicitMatch != null) {
            val numStr = explicitMatch.groupValues[1].ifEmpty { explicitMatch.groupValues[2] }
            price = numStr.toDoubleOrNull()
            text = text.removeRange(explicitMatch.range).trim()
        } else {
            // Trailing bare price, e.g. "10 kg atta 356"
            val trailingMatch = TRAILING_BARE_PRICE_REGEX.find(text)
            if (trailingMatch != null) {
                val candidatePrice = trailingMatch.groupValues[1]
                val remainingBeforePrice = text.substring(0, trailingMatch.range.first).trim()
                if (remainingBeforePrice.any { it.isLetter() }) {
                    price = candidatePrice.toDoubleOrNull()
                    text = remainingBeforePrice
                }
            }
        }

        var weightOrQuantity = ""
        var itemName = ""

        // Check for standard unit match (e.g. "10 kg", "500 g", "2 litre", "2 piece")
        val unitMatch = UNIT_REGEX.find(text)
        if (unitMatch != null) {
            val amount = unitMatch.groupValues[1]
            val rawUnit = unitMatch.groupValues[2].lowercase(Locale.ROOT)
            val standardizedUnit = standardizeUnit(rawUnit)
            weightOrQuantity = "$amount $standardizedUnit"

            val beforeUnit = text.substring(0, unitMatch.range.first).trim()
            val afterUnit = text.substring(unitMatch.range.last + 1).trim()
            val combined = ("$beforeUnit $afterUnit").trim()
            itemName = cleanItemName(combined)
        } else {
            // Check for leading count e.g. "2 brush", "3 soaps", "2 bread"
            val leadingCountMatch = LEADING_COUNT_REGEX.find(text)
            if (leadingCountMatch != null) {
                val count = leadingCountMatch.groupValues[1]
                val remainder = leadingCountMatch.groupValues[2].trim()
                val cleanRemainder = cleanItemName(remainder)
                if (cleanRemainder.isNotBlank()) {
                    weightOrQuantity = "$count pcs"
                    itemName = cleanRemainder
                }
            } else {
                // Check for trailing count e.g. "brush 2" or "brush 2 pcs"
                val trailingCountMatch = TRAILING_COUNT_REGEX.find(text)
                if (trailingCountMatch != null) {
                    val remainder = trailingCountMatch.groupValues[1].trim()
                    val count = trailingCountMatch.groupValues[2]
                    val cleanRemainder = cleanItemName(remainder)
                    if (cleanRemainder.isNotBlank()) {
                        weightOrQuantity = "$count pcs"
                        itemName = cleanRemainder
                    }
                }
            }

            // Standalone item name fallback
            if (itemName.isBlank()) {
                val cleanFallback = cleanItemName(text)
                if (cleanFallback.isNotBlank()) {
                    itemName = cleanFallback
                    weightOrQuantity = "1 item"
                }
            }
        }

        if (itemName.isBlank()) {
            return null
        }

        return BillItem(
            serialNumber = serialNumber,
            itemName = itemName,
            weightOrQuantity = weightOrQuantity.ifBlank { "1 item" },
            price = price,
            isVerified = false
        )
    }

    private fun stripThinkingFillers(input: String): String {
        var s = input.trim()
        val fillerPattern = Regex("""(?i)^(?:okay|ok|wait\s+a\s+second|wait\s+1\s+second|wait\s+ek\s+second|wait\s+a\s+minute|wait|achha|acha|theek\s+hai|thik\s+hai|haan|haa|ha|hmmm?|ummm?|uhh?|ek\s+second|1\s+second|ek\s+minute|1\s+minute|ruko|socho|bhaiya|suno|listen|ek\s+kaam\s+karo)\s+""")
        var safetyCounter = 0
        while (fillerPattern.containsMatchIn(s) && safetyCounter++ < 5) {
            s = s.replace(fillerPattern, "").trim()
        }

        val trailingFillerPattern = Regex("""(?i)\s+(?:okay|ok|bas|ho\s+gaya|done|that's\s+it|khatam|bas\s+itna\s+hi|itna\s+hi|core|aur|and)$""")
        safetyCounter = 0
        while (trailingFillerPattern.containsMatchIn(s) && safetyCounter++ < 5) {
            s = s.replace(trailingFillerPattern, "").trim()
        }

        val lower = s.lowercase(Locale.ROOT)
        if (THINKING_FILLER_WORDS.contains(lower)) {
            return ""
        }

        return s
    }

    private fun standardizeUnit(unit: String): String {
        return when (unit) {
            "kg", "kgs", "kilo", "kilos", "kilogram", "kilograms", "किलो", "किग्रा" -> "kg"
            "gm", "gms", "gram", "grams", "g", "ग्राम", "ग्रा" -> "g"
            "litre", "litres", "liter", "liters", "lt", "l", "लीटर", "ली" -> "L"
            "ml", "मिली" -> "ml"
            "packet", "packets", "pkt", "pkts", "pouch", "pouches", "pudhiya", "पैकेट", "पुड़िया", "थैली" -> "pkts"
            "piece", "pieces", "pc", "pcs", "nag", "dane", "पीस", "नग", "दाने" -> "pcs"
            "dozen", "dozens", "darjan", "दर्जन" -> "dozen"
            "bottle", "bottles", "botal", "बोतल" -> "bottles"
            "box", "boxes", "dabba", "dibba", "डिब्बा", "डब्बा" -> "boxes"
            "tin", "tins", "can", "cans", "टिन", "कैन" -> "tins"
            "bori", "katta", "bag", "bags", "बोरी", "कट्टा" -> "bags"
            else -> unit
        }
    }

    fun cleanItemName(rawName: String): String {
        var clean = rawName
            .replace(Regex("""(?i)\b(of|ka|ki|ke|walay|wala|wali|wale|item|chahiye|de\s+do|dena|aur|and|core|piece|pieces|pc|pcs|second|seconds|minute|minutes|wait|ruko)\b"""), " ")
            .replace(Regex("""[^\w\s\-\u0900-\u097F]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        if (clean.isBlank()) return ""

        val lowerClean = clean.lowercase(Locale.ROOT)
        // Direct dictionary match for known phonetic mishearings
        PHONETIC_KIRANA_MAP[lowerClean]?.let { return it }

        val words = clean.split(" ").filter { word ->
            val w = word.lowercase(Locale.ROOT)
            !THINKING_FILLER_WORDS.contains(w) && w != "piece" && w != "pieces" && w != "pc" && w != "pcs" && w != "core"
        }

        if (words.isEmpty()) return ""

        // Try single word phonetic map
        if (words.size == 1) {
            val singleKey = words[0].lowercase(Locale.ROOT)
            PHONETIC_KIRANA_MAP[singleKey]?.let { return it }
        }

        val mappedWords = words.map { word ->
            val wLower = word.lowercase(Locale.ROOT)
            PHONETIC_KIRANA_MAP[wLower] ?: if (word.any { it in 'a'..'z' || it in 'A'..'Z' }) {
                word.lowercase(Locale.ROOT)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            } else {
                word
            }
        }

        return mappedWords.joinToString(" ")
    }
}
