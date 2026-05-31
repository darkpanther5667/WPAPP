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
import com.aistudio.sharmakhata.pqmzvk.data.model.Bill
import com.aistudio.sharmakhata.pqmzvk.data.model.Customer
import com.aistudio.sharmakhata.pqmzvk.data.model.Transaction
import com.aistudio.sharmakhata.pqmzvk.data.local.ExpenseEntity
import com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseEntity
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import com.aistudio.sharmakhata.pqmzvk.util.FormatUtils

// ============================================================
// MAIN COMPOSABLE — Financial Reports with 3 tabs
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportsScreen(
    bills: List<Bill>,
    transactions: List<Transaction>,
    expenses: List<ExpenseEntity>,
    customers: List<Customer>,
    purchases: List<PurchaseEntity> = emptyList(),
    onBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("P&L", "Balance Sheet", "Cash Flow")

    // ============================================================
    // P&L CALCULATIONS
    // ============================================================
    val totalRevenue = remember(bills) { bills.sumOf { it.total } }
    val totalGst = remember(bills) { bills.sumOf { it.totalCgst + it.totalSgst + it.totalIgst } }
    val netRevenue = remember(totalRevenue, totalGst) { totalRevenue - totalGst }
    val totalExpenses = remember(expenses) { expenses.sumOf { it.amount } }

    val expenseCategories = remember(expenses) {
        expenses.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
    }
    val topCategories = remember(expenseCategories) {
        expenseCategories.take(5).map { Pair(it.key, it.value) }
    }
    val otherExpenses = remember(expenseCategories) {
        if (expenseCategories.size > 5) expenseCategories.drop(5).sumOf { it.value } else 0.0
    }

    val netProfit = remember(netRevenue, totalExpenses) { netRevenue - totalExpenses }
    val profitMargin = remember(netProfit, netRevenue) {
        if (netRevenue > 0) (netProfit / netRevenue) * 100 else 0.0
    }

    // ============================================================
    // BALANCE SHEET CALCULATIONS
    // ============================================================
    val cashInHand = remember(transactions) {
        transactions.filter { it.type == "payment" }.sumOf { it.amount }
    }
    val accountsReceivable = remember(bills) {
        bills.filter { it.status == "unpaid" }.sumOf { it.total }
    }
    val inventoryValue = 0.0
    val totalAssets = remember(cashInHand, accountsReceivable) {
        cashInHand + accountsReceivable + inventoryValue
    }
    val accountsPayable = remember(purchases) {
        purchases.filter { it.status in listOf("unpaid", "partial") }
            .sumOf { it.totalAmount - it.paidAmount }
    }
    val outstandingExpenses = remember(expenses) { expenses.sumOf { it.amount } }
    val totalLiabilities = remember(accountsPayable, outstandingExpenses) {
        accountsPayable + outstandingExpenses
    }
    val retainedEarnings = remember(totalAssets, totalLiabilities) {
        totalAssets - totalLiabilities
    }
    val totalEquity = retainedEarnings

    // ============================================================
    // CASH FLOW CALCULATIONS
    // ============================================================
    val cashFromSales = remember(transactions) {
        transactions.filter { it.type == "payment" }.sumOf { it.amount }
    }
    val cashPaidForExpenses = remember(expenses) { expenses.sumOf { it.amount } }
    val netOperatingCashFlow = remember(cashFromSales, cashPaidForExpenses) {
        cashFromSales - cashPaidForExpenses
    }
    val netCashFlow = netOperatingCashFlow
    val openingBalance = 0.0
    val closingBalance = remember(openingBalance, netCashFlow) { openingBalance + netCashFlow }

    // ============================================================
    // UI
    // ============================================================
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Financial Reports",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = StitchBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = StitchSurface,
                contentColor = StitchTextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        height = 3.dp,
                        color = StitchPrimaryContainer
                    )
                },
                divider = {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = DividerColor
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        },
                        selectedContentColor = StitchPrimaryContainer,
                        unselectedContentColor = TextSecondaryLight
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> PLStatementSection(
                    totalRevenue = totalRevenue,
                    totalGst = totalGst,
                    netRevenue = netRevenue,
                    totalExpenses = totalExpenses,
                    topCategories = topCategories,
                    otherExpenses = otherExpenses,
                    netProfit = netProfit,
                    profitMargin = profitMargin,
                    billsCount = bills.size,
                    expensesCount = expenses.size
                )
                1 -> BalanceSheetSection(
                    cashInHand = cashInHand,
                    accountsReceivable = accountsReceivable,
                    inventoryValue = inventoryValue,
                    totalAssets = totalAssets,
                    accountsPayable = accountsPayable,
                    outstandingExpenses = outstandingExpenses,
                    totalLiabilities = totalLiabilities,
                    retainedEarnings = retainedEarnings,
                    totalEquity = totalEquity
                )
                2 -> CashFlowSection(
                    cashFromSales = cashFromSales,
                    cashPaidForExpenses = cashPaidForExpenses,
                    netOperatingCashFlow = netOperatingCashFlow,
                    netCashFlow = netCashFlow,
                    openingBalance = openingBalance,
                    closingBalance = closingBalance
                )
            }
        }
    }
}

// ============================================================
// TAB 1 — PROFIT & LOSS STATEMENT
// ============================================================

@Composable
private fun PLStatementSection(
    totalRevenue: Double,
    totalGst: Double,
    netRevenue: Double,
    totalExpenses: Double,
    topCategories: List<Pair<String, Double>>,
    otherExpenses: Double,
    netProfit: Double,
    profitMargin: Double,
    billsCount: Int = 0,
    expensesCount: Int = 0
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Stat cards row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FinancialStatCard(
                label = "Net Revenue",
                value = FormatUtils.formatCurrency(netRevenue),
                icon = Icons.Outlined.TrendingUp,
                gradient = GradientWhatsApp,
                valueColor = StitchPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            FinancialStatCard(
                label = "Net Profit",
                value = FormatUtils.formatCurrency(netProfit.coerceAtLeast(0.0)),
                icon = Icons.Outlined.AccountBalance,
                gradient = if (netProfit >= 0) GradientEmerald else GradientOrange,
                valueColor = if (netProfit >= 0) SuccessGreen else ErrorRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Profit Margin mini stat
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FinancialStatCard(
                label = "Expenses",
                value = FormatUtils.formatCurrency(totalExpenses),
                icon = Icons.Outlined.MoneyOff,
                gradient = GradientOrange,
                valueColor = OrangeDanger,
                modifier = Modifier.weight(1f)
            )
            FinancialStatCard(
                label = "Profit Margin",
                value = if (netRevenue > 0) String.format("%.1f%%", profitMargin) else "0%",
                icon = Icons.Outlined.PieChart,
                gradient = if (netProfit >= 0) GradientIndigo else GradientOrange,
                valueColor = if (netProfit >= 0) AccentBlue else ErrorRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== INCOME SECTION =====
        SectionHeader(title = "INCOME")

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FinancialBreakdownRow(
                    label = "Total Revenue",
                    value = FormatUtils.formatCurrency(totalRevenue),
                    subtitle = "$billsCount invoices",
                    valueColor = StitchPrimaryContainer
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = DividerColor
                )
                FinancialBreakdownRow(
                    label = "Less GST",
                    value = FormatUtils.formatCurrency(totalGst),
                    subtitle = "CGST + SGST + IGST",
                    valueColor = TextSecondaryLight
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = DividerColor
                )
                FinancialBreakdownRow(
                    label = "Net Revenue",
                    value = FormatUtils.formatCurrency(netRevenue),
                    subtitle = "Revenue after tax",
                    valueColor = StitchPrimaryContainer,
                    isBold = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ===== EXPENSES SECTION =====
        SectionHeader(title = "EXPENSES")

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FinancialBreakdownRow(
                    label = "Total Expenses",
                    value = FormatUtils.formatCurrency(totalExpenses),
                    subtitle = "$expensesCount entries",
                    valueColor = ErrorRed,
                    isBold = true
                )

                if (topCategories.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 0.5.dp,
                        color = DividerColor
                    )

                    topCategories.forEachIndexed { index, (category, amount) ->
                        ExpenseCategoryRow(
                            category = category,
                            amount = amount,
                            percentage = if (totalExpenses > 0) (amount / totalExpenses) * 100 else 0.0
                        )
                        if (index < topCategories.lastIndex) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    if (otherExpenses > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 0.5.dp,
                            color = DividerColor
                        )
                        ExpenseCategoryRow(
                            category = "Other Expenses",
                            amount = otherExpenses,
                            percentage = if (totalExpenses > 0) (otherExpenses / totalExpenses) * 100 else 0.0
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ===== NET RESULT SECTION =====
        SectionHeader(title = "NET RESULT")

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FinancialBreakdownRow(
                    label = "Net Revenue",
                    value = FormatUtils.formatCurrency(netRevenue),
                    subtitle = "After GST",
                    valueColor = StitchPrimaryContainer
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = DividerColor
                )
                FinancialBreakdownRow(
                    label = "Total Expenses",
                    value = FormatUtils.formatCurrency(totalExpenses),
                    subtitle = "All categories",
                    valueColor = ErrorRed
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = DividerColor
                )
                FinancialBreakdownRow(
                    label = if (netProfit >= 0) "Net Profit" else "Net Loss",
                    value = FormatUtils.formatCurrency(kotlin.math.abs(netProfit)),
                    subtitle = if (netProfit >= 0) "Profitable" else "In loss",
                    valueColor = if (netProfit >= 0) SuccessGreen else ErrorRed,
                    isBold = true,
                    isLarge = true
                )
                if (netRevenue > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 0.5.dp,
                        color = DividerColor
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Profit Margin",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondaryLight
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = String.format("%.1f%%", profitMargin),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (netProfit >= 0) SuccessGreen else ErrorRed
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ============================================================
// TAB 2 — BALANCE SHEET
// ============================================================

@Composable
private fun BalanceSheetSection(
    cashInHand: Double,
    accountsReceivable: Double,
    inventoryValue: Double,
    totalAssets: Double,
    accountsPayable: Double,
    outstandingExpenses: Double,
    totalLiabilities: Double,
    retainedEarnings: Double,
    totalEquity: Double
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Stat cards row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FinancialStatCard(
                label = "Total Assets",
                value = FormatUtils.formatCurrency(totalAssets),
                icon = Icons.Outlined.AccountBalance,
                gradient = GradientEmerald,
                valueColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            FinancialStatCard(
                label = "Total Liabilities",
                value = FormatUtils.formatCurrency(totalLiabilities),
                icon = Icons.Outlined.Money,
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
            FinancialStatCard(
                label = "Equity",
                value = FormatUtils.formatCurrency(totalEquity),
                icon = Icons.Outlined.Savings,
                gradient = GradientIndigo,
                valueColor = AccentBlue,
                modifier = Modifier.weight(1f)
            )
            FinancialStatCard(
                label = "Receivables",
                value = FormatUtils.formatCurrency(accountsReceivable),
                icon = Icons.Outlined.Receipt,
                gradient = GradientAmber,
                valueColor = AmberWarning,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== ASSETS SECTION =====
        SectionHeader(title = "ASSETS")

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FinancialBreakdownRow(
                    label = "Cash in Hand",
                    value = FormatUtils.formatCurrency(cashInHand),
                    subtitle = "Payment collections",
                    valueColor = SuccessGreen
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = DividerColor
                )
                FinancialBreakdownRow(
                    label = "Accounts Receivable",
                    value = FormatUtils.formatCurrency(accountsReceivable),
                    subtitle = "Unpaid invoices",
                    valueColor = AmberWarning
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = DividerColor
                )
                FinancialBreakdownRow(
                    label = "Inventory Value",
                    value = FormatUtils.formatCurrency(inventoryValue),
                    subtitle = "Stock on hand",
                    valueColor = TextSecondaryLight
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = DividerColor
                )
                FinancialBreakdownRow(
                    label = "Total Assets",
                    value = FormatUtils.formatCurrency(totalAssets),
                    subtitle = "Assets total",
                    valueColor = SuccessGreen,
                    isBold = true,
                    isLarge = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ===== LIABILITIES SECTION =====
        SectionHeader(title = "LIABILITIES")

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FinancialBreakdownRow(
                    label = "Accounts Payable",
                    value = FormatUtils.formatCurrency(accountsPayable),
                    subtitle = "Unpaid purchases",
                    valueColor = OrangeDanger
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = DividerColor
                )
                FinancialBreakdownRow(
                    label = "Outstanding Expenses",
                    value = FormatUtils.formatCurrency(outstandingExpenses),
                    subtitle = "Accrued expenses",
                    valueColor = ErrorRed
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = DividerColor
                )
                FinancialBreakdownRow(
                    label = "Total Liabilities",
                    value = FormatUtils.formatCurrency(totalLiabilities),
                    subtitle = "Liabilities total",
                    valueColor = ErrorRed,
                    isBold = true,
                    isLarge = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ===== EQUITY SECTION =====
        SectionHeader(title = "EQUITY")

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FinancialBreakdownRow(
                    label = "Retained Earnings",
                    value = FormatUtils.formatCurrency(retainedEarnings),
                    subtitle = "Accumulated earnings",
                    valueColor = AccentBlue
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = DividerColor
                )
                FinancialBreakdownRow(
                    label = "Total Equity",
                    value = FormatUtils.formatCurrency(totalEquity),
                    subtitle = "Equity total",
                    valueColor = AccentBlue,
                    isBold = true,
                    isLarge = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ===== BALANCE CHECK =====
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            val isBalanced = kotlin.math.abs(totalAssets - (totalLiabilities + totalEquity)) < 0.01

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isBalanced) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (isBalanced) SuccessGreen else ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBalanced) "Balance Sheet is Balanced" else "Balance Sheet is Unbalanced",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isBalanced) SuccessGreen else ErrorRed
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Assets",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = FormatUtils.formatCurrency(totalAssets),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Liabilities + Equity",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = FormatUtils.formatCurrency(totalLiabilities + totalEquity),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ============================================================
// TAB 3 — CASH FLOW STATEMENT
// ============================================================

@Composable
private fun CashFlowSection(
    cashFromSales: Double,
    cashPaidForExpenses: Double,
    netOperatingCashFlow: Double,
    netCashFlow: Double,
    openingBalance: Double,
    closingBalance: Double
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Stat cards row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FinancialStatCard(
                label = "Operating Cash Flow",
                value = FormatUtils.formatCurrency(netOperatingCashFlow),
                icon = Icons.Outlined.AccountBalanceWallet,
                gradient = if (netOperatingCashFlow >= 0) GradientWhatsApp else GradientOrange,
                valueColor = if (netOperatingCashFlow >= 0) SuccessGreen else ErrorRed,
                modifier = Modifier.weight(1f)
            )
            FinancialStatCard(
                label = "Closing Balance",
                value = FormatUtils.formatCurrency(closingBalance),
                icon = Icons.Outlined.Payments,
                gradient = GradientIndigo,
                valueColor = if (closingBalance >= 0) AccentBlue else ErrorRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== OPERATING ACTIVITIES =====
        SectionHeader(title = "OPERATING ACTIVITIES")

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FinancialBreakdownRow(
                    label = "Cash from Sales",
                    value = FormatUtils.formatCurrency(cashFromSales),
                    subtitle = "Payment collections",
                    valueColor = SuccessGreen
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = DividerColor
                )
                FinancialBreakdownRow(
                    label = "Cash Paid for Expenses",
                    value = FormatUtils.formatCurrency(cashPaidForExpenses),
                    subtitle = "Operating expenses",
                    valueColor = ErrorRed
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = DividerColor
                )
                FinancialBreakdownRow(
                    label = "Net Operating Cash Flow",
                    value = FormatUtils.formatCurrency(netOperatingCashFlow),
                    subtitle = if (netOperatingCashFlow >= 0) "Positive cash flow" else "Negative cash flow",
                    valueColor = if (netOperatingCashFlow >= 0) SuccessGreen else ErrorRed,
                    isBold = true,
                    isLarge = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ===== NET CASH FLOW =====
        SectionHeader(title = "NET CASH FLOW")

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FinancialBreakdownRow(
                    label = "Opening Balance",
                    value = FormatUtils.formatCurrency(openingBalance),
                    subtitle = "Beginning period",
                    valueColor = TextSecondaryLight
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = DividerColor
                )
                FinancialBreakdownRow(
                    label = "Net Cash Flow",
                    value = FormatUtils.formatCurrency(netCashFlow),
                    subtitle = if (netCashFlow >= 0) "Net increase" else "Net decrease",
                    valueColor = if (netCashFlow >= 0) SuccessGreen else ErrorRed,
                    isBold = true
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = DividerColor
                )
                FinancialBreakdownRow(
                    label = "Closing Balance",
                    value = FormatUtils.formatCurrency(closingBalance),
                    subtitle = "Ending period",
                    valueColor = AccentBlue,
                    isBold = true,
                    isLarge = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ============================================================
// REUSABLE COMPONENTS
// ============================================================

@Composable
private fun FinancialStatCard(
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
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
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
private fun FinancialBreakdownRow(
    label: String,
    value: String,
    subtitle: String,
    valueColor: Color,
    isBold: Boolean = false,
    isLarge: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight
            )
        }
        Text(
            text = value,
            style = if (isLarge) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            fontSize = if (isLarge) 20.sp else 15.sp
        )
    }
}

@Composable
private fun ExpenseCategoryRow(
    category: String,
    amount: Double,
    percentage: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category color dot
        val categoryColors = mapOf(
            "Rent" to GradientOrange,
            "Utilities" to GradientAmber,
            "Salary" to GradientIndigo,
            "Wages" to GradientIndigo,
            "Transport" to GradientSky,
            "Marketing" to GradientPurple,
            "Office" to GradientTeal,
            "Maintenance" to GradientAmber,
            "Insurance" to GradientEmerald,
            "Tax" to listOf(ErrorRed, OrangeDanger),
            "Legal" to GradientPurple,
            "Other" to GradientOrange
        )
        val dotGradient = categoryColors.entries
            .firstOrNull { category.contains(it.key, ignoreCase = true) }
            ?.value ?: GradientOrange

        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(dotGradient))
        )
        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = String.format("%.1f%%", percentage),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight
            )
        }
        Text(
            text = FormatUtils.formatCurrency(amount),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ErrorRed,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = SectionOverlineStyle,
        color = TextSecondaryLight,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
    )
}
