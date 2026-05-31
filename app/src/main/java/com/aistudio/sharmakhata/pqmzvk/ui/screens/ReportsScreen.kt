package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    expenses: List<com.aistudio.sharmakhata.pqmzvk.data.local.ExpenseEntity> = emptyList(),
    onMenuClick: () -> Unit = {},
    shopInitial: String = "S",
    onBack: () -> Unit
) {
    val reportState by viewModel.reportState.collectAsState()
    val dbState by viewModel.dbState.collectAsState()
    var selectedPeriod by remember { mutableStateOf("Today") }

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
                listOf(R.string.today, R.string.this_week, R.string.this_month, R.string.all).forEach { periodRes ->
                    val period = stringResource(periodRes)
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(stringResource(periodRes), style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StitchPrimaryContainer.copy(alpha = 0.15f),
                            selectedLabelColor = StitchPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = TextSecondaryLight
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            when (reportState) {
                is UiState.Success -> {
                    val report = (reportState as UiState.Success).data
                    val totalExpenses = expenses.sumOf { it.amount }
                    val netProfit = report.billsTotal - totalExpenses

                    // Summary cards row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReportStatCard(
                            label = stringResource(R.string.total_sales),
                            value = FormatUtils.formatCurrency(report.billsTotal),
                            icon = Icons.Outlined.TrendingUp,
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
                            value = FormatUtils.formatCurrency(report.paymentTotal),
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
                                value = FormatUtils.formatCurrency(report.billsTotal),
                                count = stringResource(R.string.bills_count, report.billsCount),
                                valueColor = StitchPrimaryContainer
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = DividerColor)
                            BreakdownRow(
                                label = stringResource(R.string.total_expenses_label),
                                value = FormatUtils.formatCurrency(totalExpenses),
                                count = stringResource(R.string.entries_count, expenses.size),
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
                                value = FormatUtils.formatCurrency(report.outstanding.sumOf { it.balance }),
                                count = stringResource(R.string.customers_count, report.outstanding.size),
                                valueColor = OrangeDanger
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Outstanding section
                    if (report.outstanding.isNotEmpty()) {
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
                                report.outstanding.take(5).forEachIndexed { index, entry ->
                                    OutstandingRow(
                                        name = entry.name,
                                        balance = entry.balance,
                                        isLast = index == report.outstanding.lastIndex || index == 4
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
                color = TextSecondaryLight,
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
                color = TextSecondaryLight
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
