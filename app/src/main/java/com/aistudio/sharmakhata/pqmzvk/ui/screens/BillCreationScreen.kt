package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.sharmakhata.pqmzvk.R
import com.aistudio.sharmakhata.pqmzvk.data.remote.ApiClient
import com.aistudio.sharmakhata.pqmzvk.data.remote.BillItemRequest
import com.aistudio.sharmakhata.pqmzvk.data.remote.StoredItem
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.BillingViewModel
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.OperationState
import com.aistudio.sharmakhata.pqmzvk.util.DecimalVisualTransformation
import com.aistudio.sharmakhata.pqmzvk.util.FormValidators
import com.aistudio.sharmakhata.pqmzvk.util.FormatUtils

data class BillItemEntry(
    val name: String = "",
    val price: String = "",
    val qty: String = "1"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillCreationScreen(
    viewModel: BillingViewModel,
    customerId: String,
    customerName: String,
    onBack: () -> Unit
) {
    val operationState by viewModel.operationState.collectAsState()
    val lastBillId by viewModel.lastCreatedBillId.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    var items by remember { mutableStateOf(listOf(BillItemEntry())) }
    var invoiceNumber by remember { mutableStateOf("") }
    var discountText by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Stored items from server
    var storedItems by remember { mutableStateOf<List<StoredItem>>(emptyList()) }
    var showStoredItems by remember { mutableStateOf(true) }

    // Fetch stored items on launch
    LaunchedEffect(Unit) {
        try {
            val response = ApiClient.apiService.getStoredItems()
            storedItems = response.items
        } catch (_: Exception) {
            // Silently fail — stored items are optional
        }
    }

    LaunchedEffect(operationState) {
        when (operationState) {
            is OperationState.Success -> {
                snackbarHostState.showSnackbar((operationState as OperationState.Success).message)
                if (!lastBillId.isNullOrBlank()) {
                    showSuccessDialog = true
                }
            }
            is OperationState.Error -> {
                snackbarHostState.showSnackbar((operationState as OperationState.Error).message)
                viewModel.resetOperationState()
            }
            else -> {}
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false; viewModel.resetOperationState(); onBack() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    Text(stringResource(R.string.bill_created_title), fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(stringResource(R.string.bill_created_message))
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!lastBillId.isNullOrBlank()) {
                        Button(
                            onClick = {
                                lastBillId?.let { billId ->
                                    viewModel.sendInvoiceOnWhatsApp(context, billId)
                                }
                                showSuccessDialog = false
                                viewModel.resetOperationState()
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.whatsapp_label))
                        }
                    }
                    TextButton(onClick = { showSuccessDialog = false; viewModel.resetOperationState(); onBack() }) {
                        Text(stringResource(R.string.done))
                    }
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    fun calculateTotal(): Double {
        return items.sumOf { item ->
            val price = item.price.toDoubleOrNull() ?: 0.0
            val qty = item.qty.toIntOrNull() ?: 1
            price * qty
        }
    }

    // Add a stored item to the bill — fills first empty row or appends a new one
    fun addStoredItem(stored: StoredItem) {
        val firstEmptyIdx = items.indexOfFirst { it.name.isBlank() }
        if (firstEmptyIdx >= 0) {
            items = items.toMutableList().also {
                it[firstEmptyIdx] = BillItemEntry(
                    name = stored.name,
                    price = stored.lastPrice.toString(),
                    qty = "1"
                )
            }
        } else {
            if (items.size < 20) {
                items = items + BillItemEntry(
                    name = stored.name,
                    price = stored.lastPrice.toString(),
                    qty = "1"
                )
            }
        }
    }

    val isValid = items.any {
        it.name.isNotBlank() && FormValidators.isValidPrice(it.price) && (it.qty.toIntOrNull() ?: 0) > 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_invoice_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        // Gradient icon container (matching ModernStatCard style)
                        Box(
                            modifier = Modifier
                                .size(ComponentSize.iconContainerMedium)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(GradientIndigo.map { it.copy(alpha = 0.15f) })),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(IconSize.small)
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.billing_for),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = customerName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // Stored items section (from server catalog)
                if (storedItems.isNotEmpty() && showStoredItems) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.flat)
                    ) {
                        Column(modifier = Modifier.padding(Spacing.cardPadding)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(IconSize.small)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.small))
                                    Text(
                                        text = stringResource(R.string.quick_add_items),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(
                                    onClick = { showStoredItems = false },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.hide),
                                        tint = Slate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(Spacing.small))
                            // Horizontally scrollable chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                            ) {
                                storedItems.take(20).forEach { stored ->
                                    SuggestionChip(
                                        onClick = { addStoredItem(stored) },
                                        label = {
                                            Text(
                                                text = "${stored.name} ₹${stored.lastPrice.toInt()}",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        icon = {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Invoice Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.invoice_items),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = {
                            if (items.size < 20) {
                                items = items + BillItemEntry()
                            }
                        }
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.add_item_button), color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Items Table Header
                if (items.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Table header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(R.string.item_column),
                                    modifier = Modifier.weight(1.5f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    stringResource(R.string.qty_column),
                                    modifier = Modifier.weight(0.6f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    stringResource(R.string.price_column),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    stringResource(R.string.amount_column),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.width(32.dp)) // for delete button
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            Spacer(modifier = Modifier.height(8.dp))

                            // Item rows
                            items.forEachIndexed { index, item ->
                                val itemTotal = (item.price.toDoubleOrNull() ?: 0.0) * (item.qty.toIntOrNull() ?: 1)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Item name
                                    OutlinedTextField(
                                        value = item.name,
                                        onValueChange = { newName ->
                                            items = items.toMutableList().also { it[index] = item.copy(name = newName) }
                                        },
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .height(48.dp),
                                        placeholder = { Text(stringResource(R.string.item_name_placeholder_small), fontSize = 12.sp) },
                                        textStyle = MaterialTheme.typography.bodySmall,
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Quantity
                                    OutlinedTextField(
                                        value = item.qty,
                                        onValueChange = { newQty ->
                                            if (newQty.isEmpty() || newQty.all { it.isDigit() }) {
                                                items = items.toMutableList().also { it[index] = item.copy(qty = newQty) }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(0.6f)
                                            .height(48.dp),
                                        placeholder = { Text(stringResource(R.string.qty_placeholder), fontSize = 12.sp, textAlign = TextAlign.Center) },
                                        textStyle = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Price
                                    OutlinedTextField(
                                        value = item.price,
                                        onValueChange = { newPrice ->
                                            if (newPrice.isEmpty() || newPrice.all { it.isDigit() || it == '.' }) {
                                                items = items.toMutableList().also { it[index] = item.copy(price = newPrice) }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        placeholder = { Text(stringResource(R.string.price_placeholder_zero), fontSize = 12.sp, textAlign = TextAlign.End) },
                                        textStyle = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.End),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        visualTransformation = DecimalVisualTransformation(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Amount (calculated)
                                    Text(
                                        text = FormatUtils.formatCurrency(itemTotal),
                                        modifier = Modifier.weight(1f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.End,
                                        maxLines = 1
                                    )

                                    // Delete button
                                    if (items.size > 1) {
                                        IconButton(
                                            onClick = {
                                                items = items.toMutableList().also { it.removeAt(index) }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.RemoveCircleOutline,
                                                contentDescription = stringResource(R.string.remove),
                                                tint = ErrorRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(28.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Custom Invoice & Discount Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = invoiceNumber,
                            onValueChange = { invoiceNumber = it },
                            label = { Text("Invoice No. (Optional)") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = discountText,
                            onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) discountText = it },
                            label = { Text("Discount (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Totals Section
                    val totalAmount = calculateTotal()
                    val discountAmount = discountText.toDoubleOrNull() ?: 0.0
                    val grandTotal = (totalAmount - discountAmount).coerceAtLeast(0.0)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.subtotal), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(FormatUtils.formatCurrency(totalAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            if (discountAmount > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Discount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("- " + FormatUtils.formatCurrency(discountAmount), style = MaterialTheme.typography.bodySmall, color = ErrorRed)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.gst_optional), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("—", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(R.string.grand_total),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    FormatUtils.formatCurrency(grandTotal),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Generate Invoice Button
                Button(
                    onClick = {
                        val billItems = items
                            .filter { it.name.isNotBlank() && it.price.isNotBlank() }
                            .map { item ->
                                val price = item.price.toDoubleOrNull() ?: 0.0
                                val qty = item.qty.toIntOrNull() ?: 1
                                BillItemRequest(item.name, price, qty)
                            }
                        if (billItems.isNotEmpty()) {
                            val total = calculateTotal()
                            val discountAmount = discountText.toDoubleOrNull() ?: 0.0
                            val grandTotal = (total - discountAmount).coerceAtLeast(0.0)
                            viewModel.createBill(
                                context = context, 
                                customerId = customerId, 
                                amount = grandTotal, 
                                items = billItems,
                                invoiceNumber = invoiceNumber.takeIf { it.isNotBlank() },
                                discount = discountAmount,
                                grandTotal = grandTotal
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ComponentSize.buttonHeight),
                    enabled = isValid && operationState !is OperationState.Loading,
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (operationState is OperationState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.create_invoice_button), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
