package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BillEntity
import com.example.ui.components.AppLogoIcon
import com.example.ui.components.CustomerEditDialog
import com.example.ui.components.LucideIcons
import com.example.ui.theme.InkDark
import com.example.ui.theme.InkLight
import com.example.ui.theme.InkMuted
import com.example.ui.theme.ParchiPurpleContainer
import com.example.ui.theme.ParchiPurpleDark
import com.example.ui.theme.ParchiPurpleDeep
import com.example.ui.theme.ParchiPurpleLight
import com.example.ui.theme.ParchiPurplePrimary
import com.example.ui.theme.ParchiPurpleSupporting
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: BillViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredBills by viewModel.filteredBills.collectAsStateWithLifecycle()
    val allBills by viewModel.savedBills.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var showStoreEditDialog by remember { mutableStateOf(false) }

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
            // Header Section: Store Name + SVG Receipt Logo, Search Toggle & Store Profile Icon
            HomeTopBar(
                storeName = uiState.storeName,
                isSearchExpanded = uiState.isSearchExpanded,
                onToggleSearch = { viewModel.toggleSearch() },
                onOpenStoreProfile = { showStoreEditDialog = true }
            )

            // Expandable Search Bar
            AnimatedVisibility(
                visible = uiState.isSearchExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = Color(0x14000000))
                            .testTag("search_bills_input"),
                        placeholder = {
                            Text(
                                text = "Search by bill #, customer, item or date...",
                                fontSize = 13.sp,
                                color = InkLight
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = LucideIcons.Search,
                                contentDescription = "Search",
                                tint = ParchiPurpleSupporting,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    viewModel.updateSearchQuery("")
                                } else {
                                    viewModel.toggleSearch()
                                    focusManager.clearFocus()
                                }
                            }) {
                                Icon(
                                    imageVector = LucideIcons.Close,
                                    contentDescription = "Clear search",
                                    tint = InkMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = ParchiPurpleSupporting,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                    )
                }
            }

            // Main Tab Content: Receipts (2 Column Grid) or Daily Sales
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (uiState.selectedHomeTab) {
                    HomeNavTab.Receipts, HomeNavTab.Profile -> {
                        Receipts2ColumnGridContent(
                            bills = filteredBills,
                            searchQuery = uiState.searchQuery,
                            onBillClick = { bill -> viewModel.openExistingBill(bill) },
                            onDeleteBill = { bill -> viewModel.deleteSavedBill(bill) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    HomeNavTab.DailySales -> {
                        DailySalesTabContent(
                            bills = allBills,
                            onBillClick = { bill -> viewModel.openExistingBill(bill) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Modern Navigation Bar with Circular Voice Mic Button
            JupiterBulgeNavigationBar(
                selectedTab = uiState.selectedHomeTab,
                isListening = uiState.isListening,
                onSelectTab = { tab -> viewModel.selectHomeTab(tab) },
                onMicClick = {
                    if (uiState.isListening) {
                        viewModel.stopVoiceRecording()
                    } else {
                        viewModel.startNewReceipt()
                    }
                }
            )
        }

        // Store Profile / Settings Modal
        if (showStoreEditDialog) {
            StoreProfileEditDialog(
                storeName = uiState.storeName,
                storeCategory = uiState.storeCategory,
                storePhone = uiState.storePhone,
                onDismiss = { showStoreEditDialog = false },
                onSave = { name, cat, phone ->
                    viewModel.updateStoreProfile(name, cat, phone)
                    showStoreEditDialog = false
                }
            )
        }
    }
}

/**
 * Top Header: App Logo SVG, Store Name, Search & Profile Icons
 */
@Composable
private fun HomeTopBar(
    storeName: String,
    isSearchExpanded: Boolean,
    onToggleSearch: () -> Unit,
    onOpenStoreProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            AppLogoIcon(
                size = 28.dp,
                fillColor = ParchiPurplePrimary,
                strokeColor = ParchiPurpleSupporting
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = storeName.uppercase(Locale.getDefault()),
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.1.sp,
                color = InkDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Toggle Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(2.dp, RoundedCornerShape(14.dp), spotColor = Color(0x14000000))
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSearchExpanded) ParchiPurpleLight else Color.White)
                    .border(
                        1.2.dp,
                        if (isSearchExpanded) ParchiPurpleSupporting else Color(0xFFE2E8F0),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onToggleSearch() }
                    .testTag("home_search_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIcons.Search,
                    contentDescription = "Search Bills",
                    tint = if (isSearchExpanded) ParchiPurpleSupporting else InkDark,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Store / Profile Settings Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(2.dp, RoundedCornerShape(14.dp), spotColor = Color(0x14000000))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.2.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                    .clickable { onOpenStoreProfile() }
                    .testTag("home_store_profile_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIcons.Store,
                    contentDescription = "My Store Profile",
                    tint = ParchiPurpleSupporting,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Modern Floating Pill Navigation Bar with Circular Voice Mic Button
 */
@Composable
private fun JupiterBulgeNavigationBar(
    selectedTab: HomeNavTab,
    isListening: Boolean,
    onSelectTab: (HomeNavTab) -> Unit,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating Pill Container Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(12.dp, CircleShape, spotColor = ParchiPurpleSupporting.copy(alpha = 0.25f), ambientColor = Color(0x1A000000))
                .clip(CircleShape)
                .background(Color.White)
                .border(1.5.dp, Color(0xFFF1F5F9), CircleShape)
                .padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Receipts (Left)
                val isReceiptsSelected = selectedTab == HomeNavTab.Receipts || selectedTab == HomeNavTab.Profile
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(if (isReceiptsSelected) ParchiPurpleContainer else Color.Transparent)
                        .clickable { onSelectTab(HomeNavTab.Receipts) }
                        .padding(horizontal = 14.dp)
                        .testTag("tab_receipts"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.Receipt,
                        contentDescription = "Receipts",
                        tint = if (isReceiptsSelected) ParchiPurpleSupporting else InkMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Receipts",
                        fontSize = 14.sp,
                        fontWeight = if (isReceiptsSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isReceiptsSelected) ParchiPurpleDark else InkMuted
                    )
                }

                Spacer(modifier = Modifier.width(72.dp)) // Center gap for elevated circular mic FAB

                // Tab 2: Daily Sales (Right)
                val isSalesSelected = selectedTab == HomeNavTab.DailySales
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(if (isSalesSelected) ParchiPurpleContainer else Color.Transparent)
                        .clickable { onSelectTab(HomeNavTab.DailySales) }
                        .padding(horizontal = 14.dp)
                        .testTag("tab_sales"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.TrendingUp,
                        contentDescription = "Daily Sales",
                        tint = if (isSalesSelected) ParchiPurpleSupporting else InkMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daily Sales",
                        fontSize = 14.sp,
                        fontWeight = if (isSalesSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSalesSelected) ParchiPurpleDark else InkMuted
                    )
                }
            }
        }

        // Bulge FAB: Outer Glowing Pulse Ring + Circular Voice Mic FAB
        Box(
            modifier = Modifier.offset(y = (-18).dp),
            contentAlignment = Alignment.Center
        ) {
            // Soft halo glow
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .graphicsLayer(scaleX = glowScale, scaleY = glowScale)
                        .background(ParchiPurpleSupporting.copy(alpha = 0.3f), CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(ParchiPurpleSupporting.copy(alpha = 0.15f), CircleShape)
                )
            }

            // Circular Voice Mic FAB
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(12.dp, CircleShape, spotColor = ParchiPurpleSupporting.copy(alpha = 0.5f))
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(ParchiPurpleSupporting, Color(0xFF8800D6))
                        )
                    )
                    .border(2.dp, ParchiPurplePrimary, CircleShape)
                    .clickable { onMicClick() }
                    .testTag("jupiter_mic_fab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) LucideIcons.Pause else LucideIcons.Mic,
                    contentDescription = "Voice Dictation",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

/**
 * 2-Column Grid for Receipts
 */
@Composable
private fun Receipts2ColumnGridContent(
    bills: List<BillEntity>,
    searchQuery: String,
    onBillClick: (BillEntity) -> Unit,
    onDeleteBill: (BillEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (bills.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ParchiPurpleLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.Receipt,
                        contentDescription = "No Receipts",
                        tint = ParchiPurpleSupporting,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (searchQuery.isNotEmpty()) "No matching receipts" else "No receipts created yet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (searchQuery.isNotEmpty()) "Try searching for a different item or customer name"
                    else "Tap the circular mic button below to dictate your first bill!",
                    fontSize = 12.sp,
                    color = InkMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        val groupedBills = remember(bills) {
            bills.groupBy { it.dateDisplay.ifEmpty { "Recent Receipts" } }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            groupedBills.forEach { (dateHeader, dateBills) ->
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateHeader,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = InkMuted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "•  ${dateBills.size} Bills",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = InkLight
                        )
                    }
                }

                items(dateBills, key = { it.id }) { bill ->
                    SwipeableReceiptGridCard(
                        bill = bill,
                        onClick = { onBillClick(bill) },
                        onDelete = { onDeleteBill(bill) }
                    )
                }
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Swipeable 2-Column Grid Card
 */
@Composable
private fun SwipeableReceiptGridCard(
    bill: BillEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue != SwipeToDismissBoxValue.Settled) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) Color(0xFFEF4444) else Color.Transparent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(color)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = LucideIcons.Trash,
                    contentDescription = "Delete Bill",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = Color(0x12000000))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .border(1.2.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                .clickable { onClick() }
                .padding(12.dp)
                .testTag("receipt_grid_card_${bill.billNumber}")
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${bill.billNumber}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = InkDark
                    )

                    Text(
                        text = bill.formattedDateTime.substringAfter("•").trim(),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = InkLight
                    )
                }

                Column {
                    if (bill.customerName.isNotBlank()) {
                        Text(
                            text = bill.customerName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ParchiPurpleDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${bill.itemCount} items",
                        fontSize = 11.sp,
                        color = InkMuted
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${if (bill.totalAmount % 1.0 == 0.0) bill.totalAmount.toInt() else String.format(Locale.US, "%.2f", bill.totalAmount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = InkDark
                    )
                }
            }
        }
    }
}

/**
 * Daily Sales Analytics Tab Content
 */
@Composable
private fun DailySalesTabContent(
    bills: List<BillEntity>,
    onBillClick: (BillEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalRevenue = remember(bills) { bills.sumOf { it.totalAmount } }
    val totalBills = bills.size
    val totalItems = remember(bills) { bills.sumOf { it.itemCount } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        // Revenue Summary Card in Parchi Soft Purple Tint
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(3.dp, RoundedCornerShape(22.dp), spotColor = Color(0x14000000))
                .clip(RoundedCornerShape(22.dp))
                .background(ParchiPurpleLight)
                .border(1.2.dp, ParchiPurpleContainer, RoundedCornerShape(22.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL STORE REVENUE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = ParchiPurpleDeep
                    )
                    Icon(
                        imageVector = LucideIcons.TrendingUp,
                        contentDescription = "Sales",
                        tint = ParchiPurpleSupporting,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "₹ ${if (totalRevenue % 1.0 == 0.0) totalRevenue.toInt() else String.format(Locale.US, "%.2f", totalRevenue)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = InkDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(text = "BILLS ISSUED", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = InkMuted)
                            Text(text = "$totalBills", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = InkDark)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(text = "TOTAL ITEMS", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = InkMuted)
                            Text(text = "$totalItems", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = InkDark)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ALL TRANSACTIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = InkMuted,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bills, key = { it.id }) { bill ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(14.dp), spotColor = Color(0x0F000000))
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                        .clickable { onBillClick(bill) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "#${bill.billNumber}", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = InkDark)
                        Text(text = "${bill.itemCount} items • ${bill.formattedDateTime.substringAfter("•").trim()}", fontSize = 11.sp, color = InkMuted)
                    }
                    Text(
                        text = "₹${if (bill.totalAmount % 1.0 == 0.0) bill.totalAmount.toInt() else String.format(Locale.US, "%.2f", bill.totalAmount)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = InkDark
                    )
                }
            }
        }
    }
}

/**
 * Store Profile & Settings Modal Dialog
 */
@Composable
private fun StoreProfileEditDialog(
    storeName: String,
    storeCategory: String,
    storePhone: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var editName by remember(storeName) { mutableStateOf(storeName) }
    var editCategory by remember(storeCategory) { mutableStateOf(storeCategory) }
    var editPhone by remember(storePhone) { mutableStateOf(storePhone) }

    CustomerEditDialog(
        initialName = editName,
        initialPhone = editPhone,
        initialHouseNo = editCategory,
        onDismiss = onDismiss,
        onSave = { name, phone, cat ->
            onSave(name.ifBlank { "Manmohani Hatti" }, cat.ifBlank { "General Store" }, phone)
        }
    )
}
