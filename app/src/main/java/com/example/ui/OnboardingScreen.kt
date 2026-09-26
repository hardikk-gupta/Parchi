package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppLogoIcon
import com.example.ui.components.LucideIcons
import com.example.ui.theme.CategoryBakeryBg
import com.example.ui.theme.CategoryDairyBg
import com.example.ui.theme.CategoryFruitsBg
import com.example.ui.theme.CategoryGeneralBg
import com.example.ui.theme.InkDark
import com.example.ui.theme.InkMuted
import com.example.ui.theme.ParchiPurpleContainer
import com.example.ui.theme.ParchiPurpleDark
import com.example.ui.theme.ParchiPurpleDeep
import com.example.ui.theme.ParchiPurpleLight
import com.example.ui.theme.ParchiPurplePrimary
import com.example.ui.theme.ParchiPurpleSupporting
import com.example.ui.theme.ParchiPurpleSurface

@Composable
fun OnboardingScreen(
    onComplete: (businessName: String, category: String, phone: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(0) }
    var businessName by remember { mutableStateOf("Manmohani Hatti") }
    var selectedCategory by remember { mutableStateOf("General Store") }
    var storePhone by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val categories = listOf(
        "General Store",
        "Dairy & Sweets",
        "Fruits & Vegetables",
        "Bakery & Snacks",
        "Clothing & Retail"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF7FC))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // App Logo Badge
                Row(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
                        .clip(RoundedCornerShape(20.dp))
                        .background(ParchiPurpleSurface)
                        .border(1.2.dp, ParchiPurpleContainer, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppLogoIcon(
                        size = 28.dp,
                        fillColor = ParchiPurplePrimary,
                        strokeColor = ParchiPurpleSupporting
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "PARCHI  •  पर्ची",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = ParchiPurpleDeep,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (step == 0) {
                    // STEP 1: Feature Overview with Tall Vertical Cards
                    Text(
                        text = "Voice Billing for Kirana & Retailers",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = InkDark,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Bol kar instant parchi banayein. Simple, fast, and completely offline.",
                        fontSize = 13.sp,
                        color = InkMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Feature Cards
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        TallVerticalFeatureCard(
                            icon = LucideIcons.Mic,
                            title = "Voice Input & Kirana Dictation",
                            subtitle = "SMART AUDIO PARSER",
                            desc = "Speak items naturally in Hindi, Hinglish, or English without pausing. Dictate full grocery bills in seconds.",
                            bgColor = ParchiPurpleLight,
                            accentColor = ParchiPurpleSupporting
                        )

                        TallVerticalFeatureCard(
                            icon = LucideIcons.Receipt,
                            title = "Smart Items & Price Extraction",
                            subtitle = "PHONETIC & FRACTION ENGINE",
                            desc = "Auto-detects items, weights (10 kg, 500 g, 2 pcs), and prices directly from speech with zero manual entry.",
                            bgColor = CategoryDairyBg,
                            accentColor = ParchiPurpleSupporting
                        )

                        TallVerticalFeatureCard(
                            icon = LucideIcons.TrendingUp,
                            title = "Thermal Receipts & WhatsApp Share",
                            subtitle = "PRINT & DAILY SALES",
                            desc = "Instant 3D thermal paper receipts. Print or share directly on WhatsApp with your shop logo and phone.",
                            bgColor = CategoryFruitsBg,
                            accentColor = Color(0xFF10B981)
                        )
                    }
                } else {
                    // STEP 2: Set up your business
                    Text(
                        text = "Set up your business",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = InkDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Enter your shop details to personalize your bills and receipts.",
                        fontSize = 13.sp,
                        color = InkMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Business Name Input
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "SHOP / BUSINESS NAME *",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = InkMuted,
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            placeholder = { Text("e.g. Manmohani Hatti") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = Color(0x14000000))
                                .testTag("onboarding_business_name_input"),
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = LucideIcons.Store,
                                    contentDescription = "Store",
                                    tint = ParchiPurpleSupporting
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = ParchiPurpleSupporting,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Category Selection Chips
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "BUSINESS CATEGORY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = InkMuted,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (cat in categories) {
                                val isSelected = selectedCategory == cat
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) ParchiPurpleContainer else Color.White)
                                        .border(
                                            1.2.dp,
                                            if (isSelected) ParchiPurpleSupporting else Color(0xFFE2E8F0),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isSelected) ParchiPurpleDark else InkDark
                                    )

                                    if (isSelected) {
                                        Icon(
                                            imageVector = LucideIcons.Check,
                                            contentDescription = "Selected",
                                            tint = ParchiPurpleSupporting,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Phone Number Input
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "STORE PHONE / WHATSAPP (OPTIONAL)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = InkMuted,
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = storePhone,
                            onValueChange = { storePhone = it },
                            placeholder = { Text("e.g. 9876543210") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = Color(0x14000000))
                                .testTag("onboarding_phone_input"),
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = ParchiPurpleSupporting,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }
                }
            }

            // Bottom CTA Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 12.dp)
            ) {
                Button(
                    onClick = {
                        if (step == 0) {
                            step = 1
                        } else {
                            val finalName = businessName.trim().ifEmpty { "Manmohani Hatti" }
                            onComplete(finalName, selectedCategory, storePhone.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = ParchiPurpleSupporting.copy(alpha = 0.3f))
                        .testTag("onboarding_continue_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ParchiPurpleSupporting,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (step == 0) "GET STARTED" else "START BILLING NOW",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TallVerticalFeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    desc: String,
    bgColor: Color,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(22.dp), spotColor = Color(0x14000000))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(1.2.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = accentColor
                )

                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = InkDark
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = desc,
                    fontSize = 12.sp,
                    color = InkMuted,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
