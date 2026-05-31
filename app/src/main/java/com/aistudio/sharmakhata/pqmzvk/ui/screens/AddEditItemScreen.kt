package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import com.aistudio.sharmakhata.pqmzvk.R
import com.aistudio.sharmakhata.pqmzvk.data.local.ItemEntity
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreen(
    itemId: Long? = null,
    existingItem: ItemEntity? = null,
    onBack: () -> Unit,
    onSave: (name: String, price: Double, stock: Int, lowStockAlert: Int) -> Unit,
    onDelete: (Long) -> Unit = {}
) {
    val isEditing = itemId != null || existingItem != null

    var name by remember { mutableStateOf(existingItem?.name ?: "") }
    var price by remember { mutableStateOf(existingItem?.price?.let { if (it == 0.0) "" else it.toString() } ?: "") }
    var stock by remember { mutableStateOf(existingItem?.stock?.toString() ?: "0") }
    var lowStockAlert by remember { mutableStateOf(existingItem?.lowStockAlert?.toString() ?: "5") }

    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    val isValid = name.isNotBlank() && price.isNotBlank()

    fun validateAndSave() {
        nameError = name.isBlank()
        priceError = price.isBlank()
        if (name.isNotBlank() && price.isNotBlank()) {
            onSave(name.trim(), price.toDoubleOrNull() ?: 0.0, stock.toIntOrNull() ?: 0, lowStockAlert.toIntOrNull() ?: 5)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = StitchBg,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .border(width = 0.5.dp, color = StitchBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = StitchTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = StitchPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isEditing) stringResource(R.string.edit_item_title) else stringResource(R.string.add_item_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextPrimary
                    )
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
            // Hero gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(CardShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                StitchPrimaryContainer.copy(alpha = 0.3f),
                                StitchBg
                            ),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY)
                        )
                    )
                    .border(0.5.dp, StitchPrimaryContainer.copy(alpha = 0.2f), CardShape),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(StitchPrimaryContainer.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = StitchPrimaryContainer,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isEditing) stringResource(R.string.edit_item_details) else stringResource(R.string.new_inventory_item),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = StitchTextPrimary
                        )
                        Text(
                            text = if (isEditing) stringResource(R.string.update_item_info) else stringResource(R.string.add_item_info),
                            style = MaterialTheme.typography.labelSmall,
                            color = StitchTextSecondary
                        )
                    }
                }
            }

            // Form card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(StitchSurface)
                    .border(0.5.dp, StitchBorder, CardShape)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Item Name
                Text(
                    text = stringResource(R.string.basic_information),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = StitchPrimaryContainer
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text(stringResource(R.string.item_name_label)) },
                    placeholder = { Text(stringResource(R.string.item_name_placeholder)) },
                    leadingIcon = {
                        Icon(Icons.Outlined.ShoppingCart, contentDescription = null, tint = StitchPrimaryContainer)
                    },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text(stringResource(R.string.item_name_required), color = Color(0xFFFF6B6B)) }
                    } else null,
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

                // Price
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        if (it.isNotBlank()) priceError = false
                    },
                    label = { Text(stringResource(R.string.price_label)) },
                    placeholder = { Text(stringResource(R.string.price_placeholder)) },
                    leadingIcon = {
                        Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = StitchPrimaryContainer)
                    },
                    isError = priceError,
                    supportingText = if (priceError) {
                        { Text(stringResource(R.string.price_required), color = Color(0xFFFF6B6B)) }
                    } else null,
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

                HorizontalDivider(color = StitchBorder, thickness = 0.5.dp)

                // Stock section
                Text(
                    text = stringResource(R.string.stock_management),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = StitchPrimaryContainer
                )

                // Stock Quantity
                OutlinedTextField(
                    value = stock,
                    onValueChange = {
                        if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                            stock = it
                        }
                    },
                    label = { Text(stringResource(R.string.stock_quantity_label)) },
                    placeholder = { Text(stringResource(R.string.stock_quantity_placeholder)) },
                    leadingIcon = {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = StitchPrimaryContainer)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = TextFieldShape,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                // Low Stock Alert
                OutlinedTextField(
                    value = lowStockAlert,
                    onValueChange = {
                        if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                            lowStockAlert = it
                        }
                    },
                    label = { Text(stringResource(R.string.low_stock_alert_label)) },
                    placeholder = { Text(stringResource(R.string.low_stock_alert_placeholder)) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Warning, contentDescription = null, tint = StitchTertiaryContainer)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = TextFieldShape,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                // Preview card
                if (price.isNotBlank() || name.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CardShape)
                            .background(StitchPrimaryContainer.copy(alpha = 0.08f))
                            .border(0.5.dp, StitchPrimaryContainer.copy(alpha = 0.15f), CardShape)
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = StitchPrimaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (name.isNotBlank()) name else stringResource(R.string.item_name_preview),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StitchTextPrimary
                                )
                                Text(
                                    text = buildString {
                                        append(if (price.isNotBlank()) "₹${price}" else "₹0.00")
                                        append(" • ${stock} units")
                                        append(" • Alert at ${lowStockAlert}")
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StitchTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Save Button
            Button(
                onClick = { validateAndSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = isValid,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StitchPrimaryContainer,
                    contentColor = StitchOnPrimaryContainer,
                    disabledContainerColor = StitchPrimaryContainer.copy(alpha = 0.38f),
                    disabledContentColor = StitchOnPrimaryContainer.copy(alpha = 0.38f)
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) stringResource(R.string.update_item_button) else stringResource(R.string.save_item_button),
                    fontWeight = FontWeight.Bold
                )
            }

            // Delete button
            if (isEditing && existingItem != null) {
                OutlinedButton(
                    onClick = { onDelete(existingItem.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = ButtonShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF6B6B)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFF6B6B).copy(alpha = 0.5f))
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFFFF6B6B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.delete_item_button), fontWeight = FontWeight.Bold, color = Color(0xFFFF6B6B))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
