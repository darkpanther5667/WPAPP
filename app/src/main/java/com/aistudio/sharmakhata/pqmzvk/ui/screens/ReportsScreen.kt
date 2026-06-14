package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.ReportsViewModel
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.UiState
import com.aistudio.sharmakhata.pqmzvk.util.FormatUtils
import com.aistudio.sharmakhata.pqmzvk.data.model.DailyReport
import androidx.compose.ui.res.stringResource
import com.aistudio.sharmakhata.pqmzvk.R

enum class ReportPeriod {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    ALL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    expenses: List<com.aistudio.sharmakhata.pqmzvk.data.local.ExpenseEntity> = emptyList(),
    onMenuClick: () -> Unit = {},
    shopInitial: String = "S",
    onBack: () -> Unit
) {
    val dbState by viewModel.dbState.collectAsState()
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.TODAY) }

    Scaffold(
        topBar = {
            com.aistudio.sharmakhata.pqmzvk.ui.components.HamburgerAppBar(
                title = stringResource(R.string.reports_title),
                onMenuClick = onMenuClick,
                shopInitial = shopInitial
            )
        },
        containerColor = StitchBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Period filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val periods = listOf(
                    ReportPeriod.TODAY to R.string.today,
                    ReportPeriod.THIS_WEEK to R.string.this_week,
                    ReportPeriod.THIS_MONTH to R.string.this_month,
                    ReportPeriod.ALL to R.string.all
                )
                periods.forEach { (period, periodRes) ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(stringResource(periodRes), style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StitchPrimaryContainer.copy(alpha = 0.15f),
                            selectedLabelColor = StitchPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = StitchTextSecondary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            when (dbState) {
                is UiState.Success -> {
                    val db = (dbState as UiState.Success).data
                    
                    // Determine period start timestamp in ms (local time)
                    val periodStartTime = remember(selectedPeriod) {
                        val cal = java.util.Calendar.getInstance()
                        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        cal.set(java.util.Calendar.MINUTE, 0)
                        cal.set(java.util.Calendar.SECOND, 0)
                        cal.set(java.util.Calendar.MILLISECOND, 0)
                        when (selectedPeriod) {
                            ReportPeriod.TODAY -> cal.timeInMillis
                            ReportPeriod.THIS_WEEK -> {
                                val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
                                val diff = if (dayOfWeek == java.util.Calendar.SUNDAY) 6 else dayOfWeek - java.util.Calendar.MONDAY
                                cal.add(java.util.Calendar.DAY_OF_MONTH, -diff)
                                cal.timeInMillis
                            }
                            ReportPeriod.THIS_MONTH -> {
                                cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                                cal.timeInMillis
                            }
                            ReportPeriod.ALL -> 0L
                        }
                    }

                    // Filter bills, transactions and expenses
                    val filteredBills = remember(db.bills, periodStartTime) {
                        db.bills.filter { b ->
                            val date = FormatUtils.parseDate(b.createdAt)
                            date != null && date.time >= periodStartTime
                        }
                    }

                    val filteredTransactions = remember(db.transactions, periodStartTime) {
                        db.transactions.filter { t ->
                            val date = FormatUtils.parseDate(t.timestamp)
                            date != null && date.time >= periodStartTime
                        }
                    }

                    val filteredExpenses = remember(expenses, periodStartTime) {
                        expenses.filter { e ->
                            e.createdAt >= periodStartTime
                        }
                    }

                    val billsTotal = filteredBills.sumOf { it.total }
                    val paymentTotal = filteredTransactions.filter { it.type == "payment" }.sumOf { it.amount }
                    val totalExpenses = filteredExpenses.sumOf { it.amount }
                    val netProfit = billsTotal - totalExpenses
                    val billsCount = filteredBills.size
                    val expensesCount = filteredExpenses.size

                    val outstanding = remember(db.customers, db.transactions, db.bills) {
                        db.customers.map { customer ->
                            val cTxns = db.transactions.filter { it.customerId == customer.id }
                            val cBills = db.bills.filter { it.customerId == customer.id }
                            val payments = cTxns.filter { it.type == "payment" }.sumOf { it.amount }
                            val credits = cTxns.filter { it.type == "credit" }.sumOf { it.amount }
                            val billTotal = cBills.sumOf { it.total }
                            val balance = credits + billTotal - payments
                            com.aistudio.sharmakhata.pqmzvk.data.model.OutstandingCustomer(
                                name = customer.name,
                                phone = customer.phone,
                                balance = balance
                            )
                        }.filter { it.balance > 0.0 }
                    }

                    // Summary cards row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReportStatCard(
                            label = stringResource(R.string.total_sales),
                            value = FormatUtils.formatCurrency(billsTotal),
                            icon = Icons.AutoMirrored.Outlined.TrendingUp,
                            gradient = GradientWhatsApp,
                            valueColor = StitchPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        ReportStatCard(
                            label = stringResource(R.string.expenses_stat),
                            value = FormatUtils.formatCurrency(totalExpenses),
                            icon = Icons.Outlined.MoneyOff,
                            gradient = GradientOrange,
                            valueColor = OrangeDanger,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReportStatCard(
                            label = stringResource(R.string.net_profit),
                            value = FormatUtils.formatCurrency(netProfit.coerceAtLeast(0.0)),
                            icon = Icons.Outlined.AccountBalance,
                            gradient = if (netProfit >= 0) GradientEmerald else GradientOrange,
                            valueColor = if (netProfit >= 0) SuccessGreen else ErrorRed,
                            modifier = Modifier.weight(1f)
                        )
                        ReportStatCard(
                            label = stringResource(R.string.collection_label),
                            value = FormatUtils.formatCurrency(paymentTotal),
                            icon = Icons.Outlined.AccountBalanceWallet,
                            gradient = GradientIndigo,
                            valueColor = AccentBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Revenue Breakdown section
                    Text(
                        text = stringResource(R.string.revenue_breakdown),
                        style = SectionOverlineStyle,
                        color = TextTertiaryLight,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            BreakdownRow(
                                label = stringResource(R.string.total_bills_label),
                                value = FormatUtils.formatCurrency(billsTotal),
                                count = stringResource(R.string.bills_count, billsCount),
                                valueColor = StitchPrimaryContainer
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = DividerColor)
                            BreakdownRow(
                                label = stringResource(R.string.total_expenses_label),
                                value = FormatUtils.formatCurrency(totalExpenses),
                                count = stringResource(R.string.entries_count, expensesCount),
                                valueColor = ErrorRed
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = DividerColor)
                            BreakdownRow(
                                label = stringResource(R.string.net_revenue),
                                value = FormatUtils.formatCurrency(netProfit.coerceAtLeast(0.0)),
                                count = if (netProfit >= 0) stringResource(R.string.profit_label) else stringResource(R.string.loss_label),
                                valueColor = if (netProfit >= 0) SuccessGreen else ErrorRed
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = DividerColor)
                            BreakdownRow(
                                label = stringResource(R.string.outstanding_label),
                                value = FormatUtils.formatCurrency(outstanding.sumOf { it.balance }),
                                count = stringResource(R.string.customers_count, outstanding.size),
                                valueColor = OrangeDanger
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Outstanding section
                    if (outstanding.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.outstanding_section),
                            style = SectionOverlineStyle,
                            color = TextTertiaryLight,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                outstanding.take(5).forEachIndexed { index, entry ->
                                    OutstandingRow(
                                        name = entry.name,
                                        balance = entry.balance,
                                        isLast = index == outstanding.lastIndex || index == 4
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = StitchPrimaryContainer)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(stringResource(R.string.failed_to_load_reports), style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    gradient: List<Color>,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = StitchTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    count: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = count,
                style = MaterialTheme.typography.bodySmall,
                color = StitchTextSecondary
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun OutstandingRow(
    name: String,
    balance: Double,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val initial = name.firstOrNull()?.uppercase()?.toString() ?: "?"
        val gradient = AvatarColors[name.hashCode().mod(AvatarColors.size).let { kotlin.math.abs(it) }]
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(gradient)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = FormatUtils.formatCurrency(balance),
            style = AmountSmallStyle,
            fontWeight = FontWeight.Bold,
            color = if (balance > 0) AmountDue else AmountCredit
        )
    }
    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.5.dp,
            color = DividerColor
        )
    }
}
