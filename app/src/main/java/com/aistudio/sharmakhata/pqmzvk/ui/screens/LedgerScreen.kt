package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aistudio.sharmakhata.pqmzvk.data.model.Bill
import com.aistudio.sharmakhata.pqmzvk.data.model.Transaction
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.CustomerViewModel
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.UiState
import com.aistudio.sharmakhata.pqmzvk.util.FormatUtils
import androidx.compose.ui.res.stringResource
import com.aistudio.sharmakhata.pqmzvk.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    viewModel: CustomerViewModel,
    customerId: String,
    onBack: () -> Unit
) {
    val dbState by viewModel.dbState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ledger_title), fontWeight = FontWeight.SemiBold) },
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
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (dbState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(Spacing.xxxlarge),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(IconSize.xlarge))
                        Text(
                            text = stringResource(R.string.error_search_prefix, (dbState as UiState.Error).message),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is UiState.Success -> {
                    val db = (dbState as UiState.Success).data
                    val customer = db.customers.find { it.id == customerId }
                    val transactions = db.transactions.filter { it.customerId == customerId }
                    val bills = db.bills.filter { it.customerId == customerId }
                    
                    if (customer == null) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(Spacing.xxxlarge),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.PersonOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(IconSize.huge))
                            Spacer(modifier = Modifier.height(Spacing.medium))
                            Text(stringResource(R.string.customer_not_found), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        LedgerContent(customerName = customer.name, transactions = transactions, bills = bills)
                    }
                }
            }
        }
    }
}

@Composable
fun LedgerContent(customerName: String, transactions: List<Transaction>, bills: List<Bill>) {
    val events = remember(transactions, bills) {
        val list = mutableListOf<LedgerEvent>()
        transactions.forEach {
            list.add(LedgerEvent(it.timestamp, it.type, it.amount, it.note ?: ""))
        }
        bills.forEach {
            list.add(LedgerEvent(it.createdAt, "bill", it.total, "Bill - ${it.status.replaceFirstChar { c -> c.uppercase() }}"))
        }
        list.sortedByDescending { it.dateIso }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        ) {
            Column(modifier = Modifier.padding(Spacing.large)) {
                Text(
                    text = stringResource(R.string.ledger_for),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = customerName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(Spacing.xsmall))
                Text(
                    text = stringResource(R.string.entries_count_label, events.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(IconSize.huge)
                    )
                    Text(
                        stringResource(R.string.no_history),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(Spacing.large),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                items(events) { event ->
                    LedgerEventCard(event)
                }
            }
        }
    }
}

@Composable
fun LedgerEventCard(event: LedgerEvent) {
    val isPayment = event.type == "payment"
    val isCredit = event.type == "credit"
    val isBill = event.type == "bill"

    val amountColor = when {
        isPayment -> RupeeGreen
        isCredit -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    val sign = if (isPayment) "+" else "-"
    val icon = when {
        isPayment -> Icons.Outlined.ArrowDownward
        isCredit -> Icons.Outlined.ArrowUpward
        else -> Icons.Outlined.Receipt
    }
    val iconBg = when {
        isPayment -> RupeeGreen.copy(alpha = 0.1f)
        isCredit -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val label = when {
        isPayment -> stringResource(R.string.payment_received_label)
        isCredit -> stringResource(R.string.credit_given_label)
        else -> stringResource(R.string.bill_created_event)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ListCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.flat)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(ComponentSize.iconContainerMedium)
                    .background(iconBg, ActionIconShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = amountColor,
                    modifier = Modifier.size(IconSize.small)
                )
            }

            Spacer(modifier = Modifier.width(Spacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = FormatUtils.formatDateTime(event.dateIso),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (event.description.isNotEmpty()) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = "$sign${FormatUtils.formatCurrency(event.amount)}",
                style = AmountSmallStyle,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

data class LedgerEvent(
    val dateIso: String,
    val type: String,
    val amount: Double,
    val description: String
)
