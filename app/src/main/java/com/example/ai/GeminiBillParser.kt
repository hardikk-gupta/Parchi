package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.model.BillItem
import com.example.parser.ParseResult
import com.example.parser.ParsedCustomerInfo
import com.example.parser.VoiceBillParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

object GeminiBillParser {
    private const val TAG = "GeminiBillParser"
    // Using recommended gemini-3.5-flash model as mandated by gemini-api skill
    private const val MODEL_NAME = "gemini-3.5-flash"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun parseWithGeminiOrFallback(transcript: String, startingSerial: Int = 1): ParseResult {
        if (transcript.isBlank()) return ParseResult()

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val geminiResult = withContext(Dispatchers.IO) {
                    callGeminiApi(transcript, apiKey, startingSerial)
                }
                if (geminiResult != null && geminiResult.items.isNotEmpty()) {
                    Log.d(TAG, "Gemini AI parsed ${geminiResult.items.size} items successfully")
                    return geminiResult
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini call error or network unavailable, switching to smart local parser", e)
            }
        }

        // Fast, accurate local Kirana & phonetic parser fallback
        return VoiceBillParser.parseTranscript(transcript, startingSerial)
    }

    private fun callGeminiApi(transcript: String, apiKey: String, startingSerial: Int): ParseResult? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"

        val prompt = """
            You are an expert AI Voice Billing Engine for Indian Kirana & Grocery stores (PARCHI POS).
            Your task is to convert raw, fast, continuous voice dictations in Hindi, Hinglish, or English into clean, structured grocery bill line items and optional customer details.

            CRITICAL PHONETIC CORRECTION & NORMALIZATION RULES:
            1. Speech-To-Text (STT) models frequently mishear Indian Kirana terms into garbled English words or phonetics. You MUST phonetically correct and normalize them:
               - "aa", "aata", "ata", "aataa", "आटा" -> name: "Atta"
               - "tama", "ragma", "raj ma", "razma", "राजमा" -> name: "Rajma"
               - "ida", "meda", "mayda", "mida", "मैदा" -> name: "Maida"
               - "sugar core", "shakar", "cheeni", "chini", "sakkar", "चीनी" -> name: "Sugar"
               - "sooji", "suji", "rawa", "rava" -> name: "Suji"
               - "baisan", "basan", "बेसन" -> name: "Besan"
               - "sarso", "sarson tel", "mustard" -> name: "Mustard Oil"
               - "refine", "refined" -> name: "Refined Oil"
               - "toor dal", "arhar dal", "chana dal", "moong dal", "urad dal", "masoor dal" -> standard Dal name
               - "chawal", "chawl", "rice" -> name: "Rice"
               - "doodh", "milk" -> name: "Milk"
               - "dahi", "curd" -> name: "Curd"
               - "sabun", "soap" -> name: "Soap"
               - "chai", "tea" -> name: "Tea"
               - "namak", "salt" -> name: "Salt"

            2. CONJUNCTIONS & FILLERS REJECTION:
               - Words like "core", "aur", "and", "plus", "sath me", "with" are conjunctions or artifacts. NEVER output "core", "aur", or "and" as an item name!
               - E.g. "3 kg sugar core" -> name: "Sugar", quantity: "3", unit: "kg"
               - Ignore thinking pauses, stammers, and filler words ("okay", "wait", "ek second", "hmmm", "umm", "ruko", "achha", "theek hai", "bas itna hi", "khatam").
               - Discard completely unintelligible noise or garbled gibberish tokens.

            3. QUANTITIES & UNITS:
               - Return explicit structured fields for `name`, `quantity`, `unit`, and `price`.
               - Extract quantity into `quantity` (e.g. "10", "5", "0.5", "250", "2", "1") and unit into `unit` (e.g. "kg", "g", "L", "ml", "pcs", "pkts", "bags", "bottles", "boxes").
               - Convert Hindi fractions: "aadha kilo" -> quantity: "500", unit: "g"; "paav" -> quantity: "250", unit: "g"; "dedh kilo" -> quantity: "1.5", unit: "kg"; "dhai kilo" -> quantity: "2.5", unit: "kg".
               - Default count items without weight to unit: "pcs" or "pkts".

            4. PRICE:
               - If price is stated (e.g. "50 rupaye ka dahi" -> price: 50.0; "10 kg atta 350" -> price: 350.0), set `price`.
               - If no price is mentioned, set `price` to null.

            5. CUSTOMER INFORMATION:
               - Extract customerName, customerPhone (10-digit mobile), customerHouseNo if spoken in the transcript. Otherwise return null for these fields.

            Spoken Transcript to Parse:
            "$transcript"
        """.trimIndent()

        val responseSchema = JSONObject().apply {
            put("type", "OBJECT")
            val properties = JSONObject().apply {
                put("customerName", JSONObject().apply {
                    put("type", "STRING")
                    put("nullable", true)
                })
                put("customerPhone", JSONObject().apply {
                    put("type", "STRING")
                    put("nullable", true)
                })
                put("customerHouseNo", JSONObject().apply {
                    put("type", "STRING")
                    put("nullable", true)
                })
                put("items", JSONObject().apply {
                    put("type", "ARRAY")
                    put("items", JSONObject().apply {
                        put("type", "OBJECT")
                        val itemProps = JSONObject().apply {
                            put("name", JSONObject().apply {
                                put("type", "STRING")
                                put("description", "Clean, normalized Kirana grocery item name in English (e.g., Atta, Rajma, Sugar, Curd)")
                            })
                            put("quantity", JSONObject().apply {
                                put("type", "STRING")
                                put("description", "Quantity value (e.g. 10, 5, 0.5, 250, 2, 1)")
                            })
                            put("unit", JSONObject().apply {
                                put("type", "STRING")
                                put("description", "Standard unit (e.g. kg, g, L, ml, pcs, pkts, bags, bottles)")
                            })
                            put("price", JSONObject().apply {
                                put("type", "NUMBER")
                                put("nullable", true)
                                put("description", "Explicit price in INR or null if not spoken")
                            })
                        }
                        put("properties", itemProps)
                        put("required", JSONArray().apply { put("name") })
                    })
                })
            }
            put("properties", properties)
            put("required", JSONArray().apply { put("items") })
        }

        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)

            val generationConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("responseSchema", responseSchema)
                put("temperature", 0.1)
            }
            put("generationConfig", generationConfig)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini API error code: ${response.code}")
                return null
            }

            val responseBody = response.body?.string() ?: return null
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null

            val text = parts.getJSONObject(0).optString("text")
            if (text.isBlank()) return null

            // Apply defensive parser with strict schema validation, fuzzy unit matching, and garbage elimination
            val defensiveResult = GeminiOutputDefensiveParser.parseDefensively(text, startingSerial)
            if (defensiveResult != null && (defensiveResult.items.isNotEmpty() || defensiveResult.customerInfo.name != null)) {
                return defensiveResult
            }

            Log.w(TAG, "Defensive parser could not extract valid items from: $text")
            return null
        }
    }
}
