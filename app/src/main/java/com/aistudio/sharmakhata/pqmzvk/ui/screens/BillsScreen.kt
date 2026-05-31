package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.sharmakhata.pqmzvk.data.model.Bill
import com.aistudio.sharmakhata.pqmzvk.ui.components.EmptyState
import com.aistudio.sharmakhata.pqmzvk.ui.components.ShimmerLoading
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.BillingViewModel
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.OperationState
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.UiState
import com.aistudio.sharmakhata.pqmzvk.util.FormatUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    viewModel: BillingViewModel,
    customerId: String,
    onBack: () -> Unit,
    onOpenPdf: (String) -> Unit,
) {
    val dbState by viewModel.dbState.collectAsState()
    val operationState by viewModel.operationState.collectAsState()
    val pagedBills = remember(customerId) {
        viewModel.billsPagingData(customerId)
    }.collectAsLazyPagingItems()
    var showConfirmDialog by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(operationState) {
        when (operationState) {
            is OperationState.Success -> {
                snackbarHostState.showSnackbar((operationState as OperationState.Success).message)
                viewModel.resetOperationState()
            }
            is OperationState.Error -> {
                snackbarHostState.showSnackbar((operationState as OperationState.Error).message)
                viewModel.resetOperationState()
            }
            else -> {}
        }
    }

    // Confirmation Dialog
    if (showConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            title = { Text("Mark as Paid?") },
            text = { Text("This will mark the bill as paid. Continue?") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog?.let { viewModel.markBillPaid(context, it) }
                        showConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) { Text("Yes, Mark Paid") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bills", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        val pullToRefreshState = rememberPullToRefreshState()

        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = dbState is UiState.Loading,
            onRefresh = { scope.launch { com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager.forceRefresh() } },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (dbState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { ShimmerLoading() }
                }
                is UiState.Error -> {
                    EmptyState(
                        message = "Error loading bills",
                        description = (dbState as UiState.Error).message,
                        icon = Icons.Default.Error,
                        actionLabel = "Retry",
                        onAction = { scope.launch { com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager.forceRefresh() } }
                    )
                }
                is UiState.Success -> {
                    val db = (dbState as UiState.Success).data
                    val selectedFilter by viewModel.billFilter.collectAsState()
                    val allBillsForCustomer = db.bills.filter { it.customerId == customerId }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // Filter Chips
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedFilter == "All",
                                    onClick = { viewModel.setBillFilter("All") },
                                    label = { Text("All") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = IndigoPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedFilter == "Paid",
                                    onClick = { viewModel.setBillFilter("Paid") },
                                    label = { Text("Paid") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SuccessGreen,
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedFilter == "Unpaid",
                                    onClick = { viewModel.setBillFilter("Unpaid") },
                                    label = { Text("Unpaid") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ErrorRed,
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedFilter == "Overdue",
                                    onClick = { viewModel.setBillFilter("Overdue") },
                                    label = { Text("Overdue") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AmberWarning,
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        if (pagedBills.itemCount == 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.ReceiptLong,
                                        contentDescription = null,
                                        tint = TextSecondaryLight,
                                        modifier = Modifier.size(72.dp)
                                    )
                                    Text(
                                        text = if (allBillsForCustomer.isEmpty()) "No bills found for this customer"
                                        else "No ${selectedFilter.lowercase()} bills",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(count = pagedBills.itemCount, key = { index -> pagedBills[index]?.id ?: index }) { index ->
                                    val bill = pagedBills[index]
                                    if (bill != null) {
                                        val customerName = db.customers.find { it.id == bill.customerId }?.name ?: "Unknown"
                                        InvoiceCard(
                                            bill = bill,
                                            customerName = customerName,
                                            onSendWhatsApp = { viewModel.sendInvoiceOnWhatsApp(context, bill.id) },
                                            onMarkPaid = { showConfirmDialog = bill.id },
                                            onOpenPdf = { onOpenPdf(bill.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceCard(
    bill: Bill,
    customerName: String,
    onSendWhatsApp: () -> Unit,
    onMarkPaid: () -> Unit,
    onOpenPdf: () -> Unit,
) {
    val isPaid = bill.status == "paid"
    val statusColor = when {
        isPaid -> SuccessGreen
        bill.status == "overdue" -> AmberWarning
        else -> ErrorRed
    }
    val statusBgColor = when {
        isPaid -> SuccessGreen.copy(alpha = 0.1f)
        bill.status == "overdue" -> AmberWarning.copy(alpha = 0.1f)
        else -> ErrorRed.copy(alpha = 0.1f)
    }
    val statusText = when {
        isPaid -> "Paid"
        bill.status == "overdue" -> "Overdue"
        else -> "Unpaid"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row: Invoice # and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Invoice #${bill.id.take(8).uppercase()}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = FormatUtils.formatDate(bill.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .background(statusBgColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customer name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = IndigoPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = customerName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )
                Text(
                    text = FormatUtils.formatCurrency(bill.total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Items count if any
            if (bill.items.isNotEmpty()) {
                Text(
                    text = "${bill.items.size} item(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = CardBorder)

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // PDF
                OutlinedButton(
                    onClick = onOpenPdf,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IndigoPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", fontSize = 12.sp)
                }

                // WhatsApp
                OutlinedButton(
                    onClick = onSendWhatsApp,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366)),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 12.sp)
                }

                // Mark Paid (only if unpaid)
                if (!isPaid) {
                    Button(
                        onClick = onMarkPaid,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paid", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
