package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.sharmakhata.pqmzvk.data.model.DailyReport
import com.aistudio.sharmakhata.pqmzvk.data.model.FullDatabase
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.MainViewModel
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.UiState
import com.aistudio.sharmakhata.pqmzvk.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToCustomers: () -> Unit,
    onNavigateToWebView: () -> Unit,
    onCreateInvoice: () -> Unit = {},
    onAddCustomer: () -> Unit = {},
    onRecordPayment: () -> Unit = {},
    onViewReports: () -> Unit = {},
    onSendReminder: () -> Unit = {},
    onWhatsApp: () -> Unit = {}
) {
    val dbState by viewModel.dbState.collectAsState()
    val reportState by viewModel.reportState.collectAsState()

    val pullToRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current

    val shopName = remember(dbState) {
        when (dbState) {
            is UiState.Success -> (dbState as UiState.Success).data.shop?.name ?: "Grahbook"
            else -> "Grahbook"
        }
    }

    val ownerName = remember(dbState) {
        when (dbState) {
            is UiState.Success -> (dbState as UiState.Success).data.shop?.owner ?: ""
            else -> ""
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = Spacing.large, vertical = Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Shop name + greeting
                    Column {
                        Text(
                            text = shopName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            text = ownerName.ifBlank { "Your Business" },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )
                    }
                    // Right: Web dashboard icon + avatar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        IconButton(onClick = onNavigateToWebView) {
                            Icon(
                                Icons.Outlined.Language,
                                contentDescription = "Web Dashboard",
                                tint = TextSecondaryLight,
                                modifier = Modifier.size(IconSize.medium)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(ComponentSize.avatarSmall)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(GradientIndigo)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (shopName.firstOrNull()?.uppercase() ?: "G").toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->

        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = reportState is UiState.Loading,
            onRefresh = { viewModel.fetchData(context) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (reportState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = IndigoPrimary)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
                            modifier = Modifier.padding(Spacing.xxxlarge)
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(IconSize.huge))
                            Text(
                                "Could not load dashboard",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                (reportState as UiState.Error).message,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { viewModel.fetchData(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                shape = ButtonShape
                            ) { Text("Retry") }
                        }
                    }
                }
                is UiState.Success -> {
                    val report = (reportState as UiState.Success).data
                    DashboardContent(
                        report = report,
                        shopName = shopName,
                        dbState = dbState,
                        onNavigateToCustomers = onNavigateToCustomers,
                        onCreateInvoice = onCreateInvoice,
                        onAddCustomer = onAddCustomer,
                        onRecordPayment = onRecordPayment,
                        onViewReports = onViewReports,
                        onSendReminder = onSendReminder,
                        onWhatsApp = onWhatsApp,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// Activity item model
private data class ActivityItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val amount: String,
    val amountColor: Color,
    val time: String,
    val avatarColorIndex: Int
)

@Composable
fun DashboardContent(
    report: DailyReport,
    shopName: String,
    dbState: UiState<FullDatabase>,
    onNavigateToCustomers: () -> Unit,
    onCreateInvoice: () -> Unit,
    onAddCustomer: () -> Unit,
    onRecordPayment: () -> Unit,
    onViewReports: () -> Unit,
    onSendReminder: () -> Unit,
    onWhatsApp: () -> Unit,
    viewModel: MainViewModel
) {
    val scrollState = rememberScrollState()
    val currentDate = remember { SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date()) }
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap)
    ) {
        // ===== DATE + GREETING =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xlarge, vertical = Spacing.small)
        ) {
            Text(
                text = currentDate,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiaryLight
            )
            Spacer(modifier = Modifier.height(Spacing.xxsmall))
            Text(
                text = "$greeting!",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondaryLight,
                fontWeight = FontWeight.Normal
            )
        }

        // ===== SUMMARY CARDS (2x2 grid per spec) =====
        // Card 1: Total Outstanding (red) | Card 2: Today's Collection (green)
        // Card 3: Pending Bills (amber)   | Card 4: Active Customers (blue)
        val totalOutstanding = report.outstanding.sumOf { it.balance }
        val pendingBillCount = report.outstanding.size

        val customerCount = when (dbState) {
            is UiState.Success -> (dbState as UiState.Success).data.customers.size
            else -> 0
        }

        Column(
            modifier = Modifier.padding(horizontal = Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                SummaryCard(
                    label = "Total Outstanding",
                    value = FormatUtils.formatCurrency(totalOutstanding),
                    icon = Icons.Outlined.MoneyOff,
                    iconTint = ErrorRed,
                    iconBg = ErrorRed.copy(alpha = 0.1f),
                    valueColor = ErrorRed,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = "Today's Collection",
                    value = FormatUtils.formatCurrency(report.paymentTotal),
                    icon = Icons.Outlined.AccountBalanceWallet,
                    iconTint = SuccessGreen,
                    iconBg = SuccessGreen.copy(alpha = 0.1f),
                    valueColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                SummaryCard(
                    label = "Pending Bills",
                    value = pendingBillCount.toString(),
                    icon = Icons.Outlined.PendingActions,
                    iconTint = AmberWarning,
                    iconBg = AmberWarning.copy(alpha = 0.1f),
                    valueColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = "Active Customers",
                    value = customerCount.toString(),
                    icon = Icons.Outlined.People,
                    iconTint = IndigoPrimary,
                    iconBg = IndigoContainer,
                    valueColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ===== QUICK ACTIONS =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xlarge)
        ) {
            Text(
                text = "QUICK ACTIONS",
                style = SectionOverlineStyle,
                color = TextTertiaryLight
            )
            Spacer(modifier = Modifier.height(Spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                QuickActionTile(
                    icon = Icons.Default.AddCircle,
                    label = "New Invoice",
                    gradient = GradientIndigo,
                    onClick = onCreateInvoice,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    icon = Icons.Default.PersonAdd,
                    label = "Add Customer",
                    gradient = GradientEmerald,
                    onClick = onAddCustomer,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    icon = Icons.Default.Payments,
                    label = "Payment",
                    gradient = GradientAmber,
                    onClick = onRecordPayment,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                QuickActionTile(
                    icon = Icons.Default.BarChart,
                    label = "Reports",
                    gradient = GradientOrange,
                    onClick = onViewReports,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    icon = Icons.Default.Notifications,
                    label = "Reminders",
                    gradient = GradientPurple,
                    onClick = onSendReminder,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    icon = Icons.Default.Message,
                    label = "WhatsApp",
                    gradient = GradientWhatsApp,
                    onClick = onWhatsApp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ===== TODAY'S SALES BANNER =====
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.large),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = IndigoPrimary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.large),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Today's Bills",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(Spacing.xsmall))
                    Text(
                        text = FormatUtils.formatCurrency(report.billsTotal),
                        style = AmountDisplayStyle,
                        color = Color.White,
                        fontSize = 28.sp
                    )
                    Text(
                        text = "${report.billsCount} bill${if (report.billsCount != 1) "s" else ""} created today",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(IconSize.xxlarge)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(IconSize.medium)
                    )
                }
            }
        }

        // ===== RECENT ACTIVITY =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xlarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ACTIVITY",
                    style = SectionOverlineStyle,
                    color = TextTertiaryLight
                )
                TextButton(onClick = onNavigateToCustomers) {
                    Text(
                        "View All",
                        color = IndigoPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            when (dbState) {
                is UiState.Success -> {
                    val db = (dbState as UiState.Success).data
                    val recentBills = db.bills
                        .sortedByDescending { it.createdAt }
                        .take(5)
                    val recentTransactions = db.transactions
                        .sortedByDescending { it.timestamp }
                        .take(5)

                    if (recentBills.isEmpty() && recentTransactions.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CardShape,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.flat)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.xxlarge),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.small)
                            ) {
                                Icon(
                                    Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = TextTertiaryLight,
                                    modifier = Modifier.size(IconSize.xlarge)
                                )
                                Text(
                                    "No recent activity",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondaryLight
                                )
                                Text(
                                    "Bills and payments will appear here",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiaryLight
                                )
                            }
                        }
                    } else {
                        // Merge and sort by date, take top 6
                        val items = mutableListOf<ActivityItem>()

                        recentBills.forEach { bill ->
                            val customer = db.customers.find { it.id == bill.customerId }
                            val name = customer?.name ?: "Unknown"
                            val colorIndex = (customer?.id?.hashCode()?.mod(AvatarColors.size))?.let { kotlin.math.abs(it) } ?: 0
                            items.add(
                                ActivityItem(
                                    icon = Icons.Default.Receipt,
                                    title = "Bill #${bill.id.take(8)}",
                                    subtitle = name,
                                    amount = FormatUtils.formatCurrency(bill.total),
                                    amountColor = if (bill.status == "paid") SuccessGreen else AmountDue,
                                    time = FormatUtils.formatShortDate(bill.createdAt),
                                    avatarColorIndex = colorIndex
                                )
                            )
                        }

                        recentTransactions.forEach { tx ->
                            val customer = db.customers.find { it.id == tx.customerId }
                            val name = customer?.name ?: "Unknown"
                            val isPayment = tx.type == "payment"
                            val colorIndex = (customer?.id?.hashCode()?.mod(AvatarColors.size))?.let { kotlin.math.abs(it) } ?: 0
                            items.add(
                                ActivityItem(
                                    icon = if (isPayment) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    title = if (isPayment) "Payment" else "Credit",
                                    subtitle = name,
                                    amount = "${if (isPayment) "+" else "-"}${FormatUtils.formatCurrency(tx.amount)}",
                                    amountColor = if (isPayment) SuccessGreen else ErrorRed,
                                    time = FormatUtils.formatShortDate(tx.timestamp),
                                    avatarColorIndex = colorIndex
                                )
                            )
                        }

                        val sortedItems = items.take(6)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CardShape,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.flat)
                        ) {
                            Column(modifier = Modifier.padding(vertical = Spacing.small)) {
                                sortedItems.forEachIndexed { index, item ->
                                    ActivityRow(item, index == sortedItems.lastIndex)
                                }
                            }
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(IconSize.medium), color = IndigoPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.large))
    }
}

// ===== SUMMARY CARD (white card with icon, label, value) =====
@Composable
fun SummaryCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = CardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            // Icon in tinted circle
            Box(
                modifier = Modifier
                    .size(ComponentSize.iconContainerMedium)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(IconSize.small)
                )
            }

            // Label
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryLight,
                letterSpacing = 0.3.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Value (large, per spec: 28sp bold)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 22.sp
            )
        }
    }
}

// ===== QUICK ACTION TILE =====
@Composable
fun QuickActionTile(
    icon: ImageVector,
    label: String,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = ActionCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Box(
                modifier = Modifier
                    .size(ComponentSize.iconContainerLarge)
                    .clip(ActionIconShape)
                    .background(Brush.linearGradient(colors = gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(IconSize.medium)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ===== ACTIVITY ROW =====
@Composable
private fun ActivityRow(item: ActivityItem, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Spacing.large,
                end = Spacing.large,
                top = Spacing.small,
                bottom = Spacing.small
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mini avatar
        val gradient = AvatarColors[item.avatarColorIndex % AvatarColors.size]
        Box(
            modifier = Modifier
                .size(ComponentSize.avatarSmall)
                .clip(CircleShape)
                .background(Brush.linearGradient(colors = gradient)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(IconSize.xsmall)
            )
        }

        Spacer(modifier = Modifier.width(Spacing.medium))

        // Text content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.subtitle} \u2022 ${item.time}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Amount
        Text(
            text = item.amount,
            style = AmountSmallStyle,
            color = item.amountColor
        )
    }

    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Spacing.large),
            thickness = 0.5.dp,
            color = DividerColor
        )
    }
}
