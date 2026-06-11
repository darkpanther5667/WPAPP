package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.sharmakhata.pqmzvk.data.local.ItemEntity
import com.aistudio.sharmakhata.pqmzvk.ui.components.EmptyState
import com.aistudio.sharmakhata.pqmzvk.ui.components.FilterChipItem
import com.aistudio.sharmakhata.pqmzvk.ui.components.HamburgerAppBar
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import com.aistudio.sharmakhata.pqmzvk.util.FormatUtils
import androidx.compose.ui.res.stringResource
import com.aistudio.sharmakhata.pqmzvk.R
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    items: List<ItemEntity>,
    onMenuClick: () -> Unit = {},
    shopInitial: String = "S",
    onBack: () -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (Long) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onRefresh: () -> Unit = {},
    isLoading: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showSearch by remember { mutableStateOf(false) }

    val lowStockItems by remember(items) {
        derivedStateOf { items.count { it.stock > 0 && it.stock <= it.lowStockAlert } }
    }
    val outOfStockItems by remember(items) {
        derivedStateOf { items.count { it.stock == 0 } }
    }

    val filteredItems by remember(items, searchQuery, selectedFilter) {
        derivedStateOf {
            items.filter { item ->
                val matchesSearch = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.hsnCode.contains(searchQuery, ignoreCase = true)
                val matchesFilter = when (selectedFilter) {
                    "Low Stock" -> item.stock > 0 && item.stock <= item.lowStockAlert
                    "Out of Stock" -> item.stock == 0
                    else -> true
                }
                matchesSearch && matchesFilter
            }
        }
    }

    // Delete confirmation dialog state
    var itemToDelete by remember { mutableStateOf<ItemEntity?>(null) }

    Scaffold(
        topBar = {
            HamburgerAppBar(
                title = stringResource(R.string.inventory_title),
                onMenuClick = onMenuClick,
                shopInitial = shopInitial,
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            if (showSearch) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = stringResource(R.string.search),
                            tint = StitchTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )
        },
        containerColor = StitchBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddItem,
                containerColor = StitchPrimaryContainer,
                contentColor = StitchOnPrimaryContainer,
                shape = FabShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_item_desc), modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar (animated)
            AnimatedVisibility(
                visible = showSearch,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StitchSurface)
                        .border(0.5.dp, StitchBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = StitchTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    stringResource(R.string.search_inventory),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StitchTextSecondary.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = StitchPrimaryContainer,
                                focusedTextColor = StitchTextPrimary,
                                unfocusedTextColor = StitchTextPrimary,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear), tint = StitchTextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBadge(
                    label = stringResource(R.string.total_stat),
                    value = "${items.size}",
                    color = StitchPrimaryContainer,
                    icon = Icons.Outlined.ShoppingCart,
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    label = stringResource(R.string.low_stock),
                    value = "$lowStockItems",
                    color = StitchTertiaryContainer,
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    label = stringResource(R.string.out_stat),
                    value = "$outOfStockItems",
                    color = DebtRed,
                    icon = Icons.Outlined.Block,
                    modifier = Modifier.weight(1f)
                )
            }

            // Filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ModernFilterChip(
                        label = stringResource(R.string.all_with_item_count, items.size),
                        selected = selectedFilter == "All",
                        onClick = { selectedFilter = "All" },
                        color = StitchPrimaryContainer
                    )
                }
                item {
                    ModernFilterChip(
                        label = stringResource(R.string.low_stock_with_count, lowStockItems),
                        selected = selectedFilter == "Low Stock",
                        onClick = { selectedFilter = "Low Stock" },
                        color = StitchTertiaryContainer
                    )
                }
                item {
                    ModernFilterChip(
                        label = stringResource(R.string.out_of_stock_with_count, outOfStockItems),
                        selected = selectedFilter == "Out of Stock",
                        onClick = { selectedFilter = "Out of Stock" },
                        color = DebtRed
                    )
                }
            }

            // Content
            val pullToRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                state = pullToRefreshState,
                isRefreshing = isLoading,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    items.isEmpty() && !isLoading -> {
                        ModernEmptyInventory(onAddItem = onAddItem)
                    }
                    filteredItems.isEmpty() && !isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(GrahbookSpacing.sm)) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = if (searchQuery.isNotEmpty()) stringResource(R.string.no_items_found, searchQuery)
                                    else stringResource(R.string.no_filter_items, selectedFilter.lowercase()),
                                    style = TextStyle(
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = GrahbookFontSize.body,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                if (searchQuery.isNotEmpty()) {
                                    TextButton(onClick = { searchQuery = "" }) {
                                        Text(stringResource(R.string.clear_search), color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredItems, key = { it.id }) { item ->
                                ModernItemCard(
                                    item = item,
                                    onEdit = { onEditItem(item.id) },
                                    onDelete = { itemToDelete = item }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(
                    "Delete Item",
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${item.name}\"? This action cannot be undone.",
                    color = StitchTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteItem(item.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DebtRed,
                        contentColor = Color.White
                    ),
                    shape = ButtonShape
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel", color = StitchTextSecondary)
                }
            },
            containerColor = StitchSurface,
            shape = CardShape
        )
    }
}

@Composable
private fun StatBadge(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.5.dp, color.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Column {
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = StitchTextSecondary)
        }
    }
}

@Composable
private fun ModernFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) color else StitchSurface)
            .border(0.5.dp, if (selected) color else StitchBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else StitchTextSecondary
        )
    }
}

@Composable
private fun ModernItemCard(
    item: ItemEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val stockLevel = when {
        item.stock == 0 -> StockLevel.OutOfStock
        item.stock <= item.lowStockAlert -> StockLevel.Low
        else -> StockLevel.InStock
    }

    val stockColor = when (stockLevel) {
        StockLevel.OutOfStock -> DebtRed
        StockLevel.Low -> StitchTertiaryContainer
        StockLevel.InStock -> StitchPrimaryContainer
    }

    val stockLabel = when (stockLevel) {
        StockLevel.OutOfStock -> stringResource(R.string.out_of_stock_label)
        StockLevel.Low -> stringResource(R.string.low_stock_label)
        StockLevel.InStock -> stringResource(R.string.in_stock_label)
    }

    val stockProgress = when {
        item.stock == 0 -> 0f
        item.lowStockAlert > 0 -> (item.stock.toFloat() / (item.lowStockAlert * 3).coerceAtLeast(item.stock)).coerceAtMost(1f)
        else -> 1f
    }

    val avatarColor = getItemAvatarColor(item.name)
    val initials = item.name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase().toString() }.joinToString("").ifEmpty { "?" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = StitchSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Name + price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = FormatUtils.formatCurrency(item.price),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = StitchPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // HSN code (if present)
                if (item.hsnCode.isNotBlank()) {
                    Text(
                        text = "HSN: ${item.hsnCode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = StitchTextSecondary.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Stock info row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Stock count with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ShoppingCart,
                            contentDescription = null,
                            tint = stockColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.stock_units, item.stock, if (item.stock == 1) stringResource(R.string.unit_singular) else stringResource(R.string.unit_plural)),
                            style = MaterialTheme.typography.labelSmall,
                            color = stockColor,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Badge
                    Box(
                        modifier = Modifier
                            .clip(BadgeShape)
                            .background(stockColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stockLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = stockColor,
                            fontSize = 10.sp
                        )
                    }
                }

                // Stock progress bar
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { stockProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = stockColor,
                    trackColor = StitchBorder.copy(alpha = 0.5f),
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action buttons
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = StitchTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = DebtRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernEmptyInventory(onAddItem: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(GrahbookSpacing.screenHorizontal * 2),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(GrahbookSpacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(GrahbookRadius.xxl))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
            Text(
                text = stringResource(R.string.inventory_empty),
                style = TextStyle(
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = GrahbookFontSize.heading,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.add_first_item),
                style = TextStyle(
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = GrahbookFontSize.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(GrahbookSpacing.sm))
            Button(
                onClick = onAddItem,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(GrahbookRadius.md)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.add_item_button),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = GrahbookFontSize.body
                )
            }
        }
    }
}

private enum class StockLevel { InStock, Low, OutOfStock }

@Composable
private fun getItemAvatarColor(name: String): Color {
    val colors = AvatarColors.map { it.first() }
    return colors[abs(name.hashCode()) % colors.size]
}
