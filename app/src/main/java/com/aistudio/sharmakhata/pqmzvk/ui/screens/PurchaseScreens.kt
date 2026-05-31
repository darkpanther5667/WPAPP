package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseEntity
import com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseItemEntry
import com.aistudio.sharmakhata.pqmzvk.ui.components.HamburgerAppBar
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import com.aistudio.sharmakhata.pqmzvk.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.*

// ============================================================
// PURCHASES LIST SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    purchases: List<PurchaseEntity>,
    onMenuClick: () -> Unit = {},
    shopInitial: String = "S",
    onBack: () -> Unit,
    onAddPurchase: () -> Unit,
    onPurchaseClick: (Long) -> Unit,
    onDeletePurchase: (Long) -> Unit,
    onRefresh: () -> Unit = {},
    isLoading: Boolean = false
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filteredPurchases = remember(purchases, selectedFilter) {
        when (selectedFilter) {
            "Paid" -> purchases.filter { it.status == "paid" }
            "Unpaid" -> purchases.filter { it.status == "unpaid" || it.status == "partial" }
            else -> purchases
        }
    }
    val totalPurchaseAmount = remember(filteredPurchases) { filteredPurchases.sumOf { it.totalAmount } }

    Scaffold(
        topBar = { HamburgerAppBar(title = "Purchases", onMenuClick = onMenuClick, shopInitial = shopInitial) },
        containerColor = StitchBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPurchase,
                containerColor = StitchPrimaryContainer,
                contentColor = StitchOnPrimaryContainer,
                shape = FabShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add purchase")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Summary card
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = StitchSurface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(GradientIndigo.map { it.copy(alpha = 0.15f) })),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.LocalShipping, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${selectedFilter} Purchases",
                            style = MaterialTheme.typography.labelSmall,
                            color = StitchTextSecondary
                        )
                        Text(
                            FormatUtils.formatCurrency(totalPurchaseAmount),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = StitchTextPrimary
                        )
                        Text(
                            "${filteredPurchases.size} entries",
                            style = MaterialTheme.typography.bodySmall,
                            color = StitchTextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Filter chips
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Paid", "Unpaid").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StitchPrimaryContainer.copy(alpha = 0.15f),
                            selectedLabelColor = StitchPrimaryContainer,
                            containerColor = StitchSurface,
                            labelColor = StitchTextSecondary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            if (purchases.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.LocalShipping,
                            contentDescription = null,
                            tint = StitchTextSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text("No purchases yet", style = MaterialTheme.typography.bodyLarge, color = StitchTextSecondary)
                        Text(
                            "Track your supplier purchases",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StitchTextSecondary.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPurchases, key = { it.id }) { purchase ->
                        PurchaseListItem(
                            purchase = purchase,
                            onClick = { onPurchaseClick(purchase.id) },
                            onDelete = { onDeletePurchase(purchase.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseListItem(purchase: PurchaseEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    val statusColor = when (purchase.status) {
        "paid" -> SuccessGreen
        "partial" -> AmberWarning
        else -> ErrorRed
    }
    val statusLabel = when (purchase.status) {
        "paid" -> "Paid"
        "partial" -> "Partial"
        else -> "Unpaid"
    }
    val dateStr = try {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(purchase.createdAt))
    } catch (_: Exception) { "" }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = StitchSurface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(GradientIndigo.map { it.copy(alpha = 0.2f) })),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.LocalShipping, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    purchase.supplierName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(BadgeShape)
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                    Text(dateStr, style = MaterialTheme.typography.bodySmall, color = StitchTextSecondary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    FormatUtils.formatCurrency(purchase.totalAmount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary
                )
                if (purchase.paidAmount > 0) {
                    Text(
                        "Paid: ${FormatUtils.formatCurrency(purchase.paidAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen
                    )
                }
            }
        }
    }
}

// ============================================================
// ADD PURCHASE SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseScreen(
    onBack: () -> Unit,
    onSave: (String, String, List<PurchaseItemEntry>, Double, Double, String) -> Unit
) {
    var supplierName by remember { mutableStateOf("") }
    var supplierPhone by remember { mutableStateOf("") }
    var paidAmount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(listOf(PurchaseItemEntry())) }
    val isValid = supplierName.isNotBlank() && items.any { it.name.isNotBlank() && it.price.isNotBlank() }

    fun calculateTotal(): Double {
        return items.sumOf {
            (it.price.toDoubleOrNull() ?: 0.0) * (it.qty.toIntOrNull() ?: 1)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = StitchBg,
                modifier = Modifier.fillMaxWidth().height(64.dp).border(0.5.dp, StitchBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = StitchTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Purchase", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = StitchTextPrimary)
                }
            }
        },
        containerColor = StitchBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Supplier Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = StitchSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Supplier Details",
                        fontWeight = FontWeight.Bold,
                        color = StitchPrimaryContainer,
                        style = MaterialTheme.typography.labelMedium
                    )
                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text("Supplier Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = StitchPrimaryContainer) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = TextFieldShape,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StitchPrimaryContainer,
                            unfocusedBorderColor = StitchBorder,
                            focusedContainerColor = StitchSurfaceLow,
                            unfocusedContainerColor = StitchSurfaceLow,
                            focusedTextColor = StitchTextPrimary,
                            unfocusedTextColor = StitchTextPrimary,
                            cursorColor = StitchPrimaryContainer,
                            focusedLabelColor = StitchPrimaryContainer,
                            unfocusedLabelColor = StitchTextSecondary
                        )
                    )
                    OutlinedTextField(
                        value = supplierPhone,
                        onValueChange = { v -> if (v.all { c -> c.isDigit() || c == '+' }) supplierPhone = v },
                        label = { Text("Phone") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = StitchPrimaryContainer) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = TextFieldShape,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StitchPrimaryContainer,
                            unfocusedBorderColor = StitchBorder,
                            focusedContainerColor = StitchSurfaceLow,
                            unfocusedContainerColor = StitchSurfaceLow,
                            focusedTextColor = StitchTextPrimary,
                            unfocusedTextColor = StitchTextPrimary,
                            cursorColor = StitchPrimaryContainer,
                            focusedLabelColor = StitchPrimaryContainer,
                            unfocusedLabelColor = StitchTextSecondary
                        )
                    )
                }
            }

            // Purchase Items
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = StitchSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Outlined.ShoppingCart, contentDescription = null, tint = StitchPrimaryContainer, modifier = Modifier.size(18.dp))
                            Text("Purchase Items", fontWeight = FontWeight.Bold, color = StitchTextPrimary, style = MaterialTheme.typography.titleSmall)
                        }
                        TextButton(onClick = { if (items.size < 20) items = items + PurchaseItemEntry() }) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = StitchPrimaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", color = StitchPrimaryContainer)
                        }
                    }
                    // Header row
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Item", modifier = Modifier.weight(1.3f), fontSize = 10.sp, color = StitchTextSecondary)
                        Text("Qty", modifier = Modifier.weight(0.5f), fontSize = 10.sp, color = StitchTextSecondary, textAlign = TextAlign.Center)
                        Text("Price", modifier = Modifier.weight(0.8f), fontSize = 10.sp, color = StitchTextSecondary, textAlign = TextAlign.End)
                        Text("Amt", modifier = Modifier.weight(0.8f), fontSize = 10.sp, color = StitchTextSecondary, textAlign = TextAlign.End)
                        Spacer(modifier = Modifier.width(32.dp))
                    }
                    items.forEachIndexed { index, item ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = item.name,
                                onValueChange = { v ->
                                    items = items.toMutableList().also { it[index] = item.copy(name = v) }
                                },
                                modifier = Modifier.weight(1.3f).height(44.dp),
                                placeholder = { Text("Item", fontSize = 11.sp) },
                                textStyle = MaterialTheme.typography.bodySmall,
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = StitchPrimaryContainer,
                                    unfocusedIndicatorColor = StitchBorder,
                                    focusedContainerColor = StitchSurfaceLow,
                                    unfocusedContainerColor = StitchSurfaceLow
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedTextField(
                                value = item.qty,
                                onValueChange = { v ->
                                    if (v.isEmpty() || v.all { it.isDigit() }) {
                                        items = items.toMutableList().also { it[index] = item.copy(qty = v) }
                                    }
                                },
                                modifier = Modifier.weight(0.5f).height(44.dp),
                                placeholder = { Text("1", fontSize = 11.sp, textAlign = TextAlign.Center) },
                                textStyle = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = StitchPrimaryContainer,
                                    unfocusedIndicatorColor = StitchBorder,
                                    focusedContainerColor = StitchSurfaceLow,
                                    unfocusedContainerColor = StitchSurfaceLow
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedTextField(
                                value = item.price,
                                onValueChange = { v ->
                                    if (v.isEmpty() || v.all { it.isDigit() || it == '.' }) {
                                        items = items.toMutableList().also { it[index] = item.copy(price = v) }
                                    }
                                },
                                modifier = Modifier.weight(0.8f).height(44.dp),
                                placeholder = { Text("$0", fontSize = 11.sp, textAlign = TextAlign.End) },
                                textStyle = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.End),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(8.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = StitchPrimaryContainer,
                                    unfocusedIndicatorColor = StitchBorder,
                                    focusedContainerColor = StitchSurfaceLow,
                                    unfocusedContainerColor = StitchSurfaceLow
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                FormatUtils.formatCurrency(
                                    (item.price.toDoubleOrNull() ?: 0.0) * (item.qty.toIntOrNull() ?: 1)
                                ),
                                modifier = Modifier.weight(0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StitchTextPrimary,
                                textAlign = TextAlign.End
                            )
                            if (items.size > 1) {
                                IconButton(onClick = { items = items.toMutableList().also { it.removeAt(index) } }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove", tint = ErrorRed, modifier = Modifier.size(16.dp))
                                }
                            } else {
                                Spacer(modifier = Modifier.width(28.dp))
                            }
                        }
                    }
                    val totalAmount = calculateTotal()
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = StitchBorder)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontWeight = FontWeight.Bold, color = StitchTextPrimary)
                        Text(
                            FormatUtils.formatCurrency(totalAmount),
                            fontWeight = FontWeight.Bold,
                            color = StitchPrimaryContainer,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            // Payment Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = StitchSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Payment Details", fontWeight = FontWeight.Bold, color = StitchPrimaryContainer, style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = paidAmount,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) paidAmount = it },
                        label = { Text("Amount Paid") },
                        leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = StitchPrimaryContainer) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = TextFieldShape,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StitchPrimaryContainer,
                            unfocusedBorderColor = StitchBorder,
                            focusedContainerColor = StitchSurfaceLow,
                            unfocusedContainerColor = StitchSurfaceLow,
                            focusedTextColor = StitchTextPrimary,
                            unfocusedTextColor = StitchTextPrimary,
                            cursorColor = StitchPrimaryContainer,
                            focusedLabelColor = StitchPrimaryContainer,
                            unfocusedLabelColor = StitchTextSecondary
                        )
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = TextFieldShape,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StitchPrimaryContainer,
                            unfocusedBorderColor = StitchBorder,
                            focusedContainerColor = StitchSurfaceLow,
                            unfocusedContainerColor = StitchSurfaceLow,
                            focusedTextColor = StitchTextPrimary,
                            unfocusedTextColor = StitchTextPrimary,
                            cursorColor = StitchPrimaryContainer,
                            focusedLabelColor = StitchPrimaryContainer,
                            unfocusedLabelColor = StitchTextSecondary
                        )
                    )
                }
            }

            Button(
                onClick = { onSave(supplierName, supplierPhone, items, calculateTotal(), paidAmount.toDoubleOrNull() ?: 0.0, notes) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = isValid,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StitchPrimaryContainer,
                    contentColor = StitchOnPrimaryContainer
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Purchase", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ============================================================
// PURCHASE DETAIL SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseDetailScreen(
    purchase: PurchaseEntity?,
    onBack: () -> Unit,
    onDelete: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            Surface(
                color = StitchBg,
                modifier = Modifier.fillMaxWidth().height(64.dp).border(0.5.dp, StitchBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = StitchTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Purchase Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = StitchTextPrimary)
                }
            }
        },
        containerColor = StitchBg
    ) { padding ->
        if (purchase == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Purchase not found", color = StitchTextSecondary)
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Supplier card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = StitchSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(GradientIndigo.map { it.copy(alpha = 0.2f) })),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.LocalShipping, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(purchase.supplierName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StitchTextPrimary)
                            if (purchase.supplierPhone.isNotBlank()) {
                                Text(purchase.supplierPhone, style = MaterialTheme.typography.bodySmall, color = StitchTextSecondary)
                            }
                        }
                    }
                }
            }

            // Amount card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = StitchSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount", color = StitchTextSecondary)
                        Text(FormatUtils.formatCurrency(purchase.totalAmount), fontWeight = FontWeight.Bold, color = StitchTextPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Paid", color = StitchTextSecondary)
                        Text(FormatUtils.formatCurrency(purchase.paidAmount), color = SuccessGreen)
                    }
                    HorizontalDivider(color = StitchBorder, modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Balance", fontWeight = FontWeight.Bold, color = StitchTextSecondary)
                        Text(
                            FormatUtils.formatCurrency(purchase.totalAmount - purchase.paidAmount),
                            fontWeight = FontWeight.Bold,
                            color = if (purchase.paidAmount >= purchase.totalAmount) SuccessGreen else ErrorRed
                        )
                    }
                }
            }

            // Notes card
            if (purchase.notes.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = StitchSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Notes", fontWeight = FontWeight.Bold, color = StitchTextPrimary, style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(purchase.notes, color = StitchTextSecondary)
                    }
                }
            }

            // Delete button
            OutlinedButton(
                onClick = { onDelete(purchase.id) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = ButtonShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                border = ButtonDefaults.outlinedButtonBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ErrorRed.copy(alpha = 0.5f))
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp), tint = ErrorRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Purchase", fontWeight = FontWeight.Bold, color = ErrorRed)
            }
        }
    }
}
