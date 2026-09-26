package com.example

import com.example.ai.GeminiOutputDefensiveParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GeminiOutputDefensiveParserTest {

    @Test
    fun `parse valid JSON wrapped in markdown fences with fuzzy units`() {
        val rawLlmResponse = """
            Here is the parsed billing information:
            ```json
            {
              "customerName": "Ramesh Gupta",
              "customerPhone": "+91 9876543210",
              "customerHouseNo": "B-42",
              "items": [
                {
                  "name": "atta",
                  "quantity": "10",
                  "unit": "k.g.",
                  "price": 350.0
                },
                {
                  "name": "cheeni",
                  "quantity": "500",
                  "unit": "gram",
                  "price": 25.0
                },
                {
                  "name": "mustard oil",
                  "quantity": "1",
                  "unit": "litre",
                  "price": 140.0
                }
              ]
            }
            ```
            Hope this helps!
        """.trimIndent()

        val result = GeminiOutputDefensiveParser.parseDefensively(rawLlmResponse, startingSerial = 1)
        assertNotNull(result)
        assertEquals("Ramesh Gupta", result!!.customerInfo.name)
        assertEquals("9876543210", result.customerInfo.phone)
        assertEquals("B-42", result.customerInfo.houseNo)

        assertEquals(3, result.items.size)

        // Item 1: atta -> Atta, k.g. -> kg
        assertEquals("Atta", result.items[0].itemName)
        assertEquals("10 kg", result.items[0].weightOrQuantity)
        assertEquals(350.0, result.items[0].price)

        // Item 2: cheeni -> Sugar, gram -> g
        assertEquals("Sugar", result.items[1].itemName)
        assertEquals("500 g", result.items[1].weightOrQuantity)
        assertEquals(25.0, result.items[1].price)

        // Item 3: mustard oil -> Mustard Oil, litre -> L
        assertEquals("Mustard Oil", result.items[2].itemName)
        assertEquals("1 L", result.items[2].weightOrQuantity)
        assertEquals(140.0, result.items[2].price)
    }

    @Test
    fun `reject garbled text, conjunctions, and filler items`() {
        val rawLlmResponse = """
            {
              "customerName": null,
              "customerPhone": null,
              "customerHouseNo": null,
              "items": [
                {
                  "name": "Atta",
                  "quantity": "10",
                  "unit": "kg"
                },
                {
                  "name": "core",
                  "quantity": "1",
                  "unit": "pcs"
                },
                {
                  "name": "aur",
                  "quantity": "1",
                  "unit": "pcs"
                },
                {
                  "name": "!!!###$$$",
                  "quantity": "2",
                  "unit": "pcs"
                },
                {
                  "name": "bcdfgh",
                  "quantity": "1",
                  "unit": "pcs"
                },
                {
                  "name": "Maida",
                  "quantity": "2",
                  "unit": "kilo"
                }
              ]
            }
        """.trimIndent()

        val result = GeminiOutputDefensiveParser.parseDefensively(rawLlmResponse, startingSerial = 1)
        assertNotNull(result)

        // Should strictly keep only Atta and Maida, rejecting core, aur, symbols, consonant gibberish
        assertEquals(2, result!!.items.size)
        assertEquals("Atta", result.items[0].itemName)
        assertEquals("10 kg", result.items[0].weightOrQuantity)

        assertEquals("Maida", result.items[1].itemName)
        assertEquals("2 kg", result.items[1].weightOrQuantity)
    }

    @Test
    fun `fallback and normalize embedded unit in quantity string`() {
        val rawLlmResponse = """
            {
              "items": [
                {
                  "name": "Rajma",
                  "quantity": "5kg",
                  "unit": ""
                },
                {
                  "name": "Dahi",
                  "quantity": "aadha kilo",
                  "unit": ""
                },
                {
                  "name": "Soap",
                  "quantity": "3",
                  "unit": "peices"
                }
              ]
            }
        """.trimIndent()

        val result = GeminiOutputDefensiveParser.parseDefensively(rawLlmResponse, startingSerial = 1)
        assertNotNull(result)
        assertEquals(3, result!!.items.size)

        assertEquals("Rajma", result.items[0].itemName)
        assertEquals("5 kg", result.items[0].weightOrQuantity)

        assertEquals("Curd", result.items[1].itemName)
        assertEquals("500 g", result.items[1].weightOrQuantity)

        assertEquals("Soap", result.items[2].itemName)
        assertEquals("3 pcs", result.items[2].weightOrQuantity)
    }

    @Test
    fun `parse bare JSON array of items directly`() {
        val rawLlmResponse = """
            [
              {
                "name": "Suji",
                "quantity": "500",
                "unit": "g"
              },
              {
                "name": "Besan",
                "quantity": "1",
                "unit": "kg"
              }
            ]
        """.trimIndent()

        val result = GeminiOutputDefensiveParser.parseDefensively(rawLlmResponse, startingSerial = 1)
        assertNotNull(result)
        assertEquals(2, result!!.items.size)

        assertEquals("Suji", result.items[0].itemName)
        assertEquals("500 g", result.items[0].weightOrQuantity)

        assertEquals("Besan", result.items[1].itemName)
        assertEquals("1 kg", result.items[1].weightOrQuantity)
    }

    @Test
    fun `fuzzyMatchUnit correctly standardizes various unit typos and representations`() {
        assertEquals("kg", GeminiOutputDefensiveParser.fuzzyMatchUnit("k.g."))
        assertEquals("kg", GeminiOutputDefensiveParser.fuzzyMatchUnit("kilos"))
        assertEquals("kg", GeminiOutputDefensiveParser.fuzzyMatchUnit("kilogram"))
        assertEquals("kg", GeminiOutputDefensiveParser.fuzzyMatchUnit("किलो"))

        assertEquals("g", GeminiOutputDefensiveParser.fuzzyMatchUnit("gms"))
        assertEquals("g", GeminiOutputDefensiveParser.fuzzyMatchUnit("grams"))
        assertEquals("g", GeminiOutputDefensiveParser.fuzzyMatchUnit("ग्राम"))

        assertEquals("L", GeminiOutputDefensiveParser.fuzzyMatchUnit("ltrs"))
        assertEquals("L", GeminiOutputDefensiveParser.fuzzyMatchUnit("litres"))

        assertEquals("pcs", GeminiOutputDefensiveParser.fuzzyMatchUnit("pieces"))
        assertEquals("pcs", GeminiOutputDefensiveParser.fuzzyMatchUnit("peices"))
        assertEquals("pkts", GeminiOutputDefensiveParser.fuzzyMatchUnit("packets"))
        assertEquals("pkts", GeminiOutputDefensiveParser.fuzzyMatchUnit("pouch"))
    }
}
