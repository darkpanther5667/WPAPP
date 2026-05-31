package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Wallet
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
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.DashboardViewModel
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.UiState
import kotlinx.coroutines.launch
import com.aistudio.sharmakhata.pqmzvk.util.FormatUtils
import com.aistudio.sharmakhata.pqmzvk.ui.components.HamburgerAppBar
import androidx.compose.ui.res.stringResource
import com.aistudio.sharmakhata.pqmzvk.R
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onMenuClick: () -> Unit = {},
    shopInitial: String = "V",
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

    val scope = rememberCoroutineScope()
    val shopName = remember(dbState) {
        when (dbState) {
            is UiState.Success -> (dbState as UiState.Success).data.shop?.name ?: context.getString(R.string.default_shop_name)
            else -> context.getString(R.string.default_shop_name)
        }
    }

    Scaffold(
        topBar = {
            HamburgerAppBar(
                title = stringResource(R.string.dashboard_title),
                onMenuClick = onMenuClick,
                shopInitial = shopInitial
            )
        },
        containerColor = StitchBg
    ) { padding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = reportState is UiState.Loading,
            onRefresh = { scope.launch { LiveSyncManager.forceRefresh() } },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (reportState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = StitchPrimaryContainer)
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(32.dp)) {
                            Text("⚠️", fontSize = 48.sp)
                            Text(stringResource(R.string.could_not_load_dashboard), style = MaterialTheme.typography.bodyLarge, color = StitchTextPrimary)
                            Text((reportState as UiState.Error).message, style = MaterialTheme.typography.bodySmall, color = StitchTextSecondary, textAlign = TextAlign.Center)
                            Button(onClick = { scope.launch { LiveSyncManager.forceRefresh() } },
                                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryContainer, contentColor = StitchOnPrimaryContainer),
                                shape = ButtonShape
                            ) { Text(stringResource(R.string.retry)) }
                        }
                    }
                }
                is UiState.Success -> {
                    val report = (reportState as UiState.Success).data
                    DashboardContent(
                        report = report,
                        shopName = shopName,
                        dbState = dbState,
                        onCreateInvoice = onCreateInvoice,
                        onAddCustomer = onAddCustomer,
                        onViewReports = onViewReports,
                        onNavigateToCustomers = onNavigateToCustomers
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    report: DailyReport,
    shopName: String,
    dbState: UiState<FullDatabase>,
    onCreateInvoice: () -> Unit,
    onAddCustomer: () -> Unit,
    onViewReports: () -> Unit,
    onNavigateToCustomers: () -> Unit
) {
    val totalOutstanding = if (report.outstanding.isNotEmpty()) report.outstanding.sumOf { it.balance } else 0.0
    val overdueCount = report.outstanding.size

    val customerCount = when (dbState) {
        is UiState.Success -> (dbState as UiState.Success).data.customers.size
        else -> 0
    }
    val totalBills = when (dbState) {
        is UiState.Success -> (dbState as UiState.Success).data.bills.size
        else -> 0
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ===== GREETING =====
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = stringResource(R.string.welcome_back),
                    style = MaterialTheme.typography.bodyMedium,
                    color = StitchTextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = shopName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary
                )
            }
        }

        // ===== BENTO GRID — Premium Layout =====
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Row 1: Hero card (2x width) + revenue card
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    // Hero: Today's Collection (spans 2 cols of 3)
                    HeroMetricCard(
                        label = stringResource(R.string.todays_collection),
                        value = FormatUtils.formatCurrency(report.paymentTotal),
                        subtitle = stringResource(R.string.bills_today, report.billsCount),
                        trendIcon = Icons.Default.TrendingUp,
                        trendText = stringResource(R.string.trend_vs_yesterday),
                        gradientColors = listOf(Color(0xFF1A3A2A), Color(0xFF0D2818)),
                        accentColor = StitchPrimaryContainer,
                        modifier = Modifier.weight(1.7f)
                    )
                    // Total Revenue (small)
                    CompactMetricCard(
                        label = stringResource(R.string.revenue_label),
                        value = FormatUtils.formatCurrency(report.paymentTotal),
                        valueColor = StitchPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: 3 compact cards
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    CompactMetricCard(
                        label = stringResource(R.string.clients_label),
                        value = "$customerCount",
                        valueColor = StitchSecondary,
                        icon = Icons.Outlined.Groups,
                        modifier = Modifier.weight(1f)
                    )
                    CompactMetricCard(
                        label = stringResource(R.string.invoices_label),
                        value = "$totalBills",
                        valueColor = StitchTertiaryContainer,
                        icon = Icons.Outlined.Description,
                        modifier = Modifier.weight(1f)
                    )
                    CompactMetricCard(
                        label = stringResource(R.string.pending_label),
                        value = "$overdueCount",
                        valueColor = StitchTertiary,
                        icon = Icons.Outlined.PendingActions,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ===== QUICK ACTIONS =====
        item {
            Text(
                text = stringResource(R.string.quick_actions),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = StitchTextPrimary,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickActionChip(
                    icon = Icons.Outlined.ReceiptLong,
                    label = stringResource(R.string.new_invoice),
                    gradient = listOf(Color(0xFF25D366), Color(0xFF1DA851)),
                    onClick = onCreateInvoice,
                    modifier = Modifier.weight(1f)
                )
                QuickActionChip(
                    icon = Icons.Outlined.Payments,
                    label = stringResource(R.string.record_payment),
                    gradient = listOf(StitchSecondaryContainer, Color(0xFF2D7BE0)),
                    onClick = onAddCustomer,
                    modifier = Modifier.weight(1f)
                )
                QuickActionChip(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    label = stringResource(R.string.reports_label),
                    gradient = listOf(StitchTertiaryContainer, Color(0xFFE06040)),
                    onClick = onViewReports,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ===== RECENT INVOICES SECTION =====
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.recent_invoices),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary
                )
                TextButton(onClick = onNavigateToCustomers) {
                    Text(
                        text = stringResource(R.string.see_all),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = StitchPrimaryContainer
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = StitchPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // ===== INVOICE LIST =====
        item {
            when (dbState) {
                is UiState.Success -> {
                    val db = (dbState as UiState.Success).data
                    val recentBills = db.bills.sortedByDescending { it.createdAt }.take(5)

                    if (recentBills.isEmpty()) {
                        ModernEmptyState()
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CardShape)
                                .background(StitchSurface)
                                .border(0.5.dp, StitchBorder, CardShape)
                        ) {
                            recentBills.forEachIndexed { index, bill ->
                                val customer = db.customers.find { it.id == bill.customerId }
                                val name = customer?.name ?: stringResource(R.string.unknown)
                                val initials = name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase().toString() }.joinToString("").ifEmpty { "?" }
                                val avatarColor = getAvatarColor(name)

                                val (statusLabel, badgeBg, badgeText, avatarBg, avatarContent) = when (bill.status) {
                                    "paid" -> Quadruple(stringResource(R.string.paid_label), StitchSecondaryContainer, StitchOnSecondaryContainer, StitchSecondary, StitchOnSecondary)
                                    "unpaid" -> Quadruple(stringResource(R.string.sent_via_wa), StitchPrimaryContainer, StitchOnPrimaryContainer, StitchSecondaryContainer, StitchOnSecondaryContainer)
                                    else -> Quadruple(stringResource(R.string.sent_via_wa), StitchPrimaryContainer, StitchOnPrimaryContainer, StitchSecondaryContainer, StitchOnSecondaryContainer)
                                }

                                ModernInvoiceRow(
                                    initials = initials,
                                    name = name,
                                    invoiceNumber = stringResource(R.string.invoice_number, bill.id.take(8).uppercase()),
                                    amount = FormatUtils.formatCurrency(bill.total),
                                    statusLabel = statusLabel,
                                    badgeBg = badgeBg,
                                    badgeText = badgeText,
                                    avatarBg = avatarBg,
                                    avatarContent = avatarContent,
                                    avatarColor = avatarColor,
                                    showDivider = index < recentBills.lastIndex
                                )
                            }
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                            .clip(CardShape).background(StitchSurface)
                            .border(0.5.dp, StitchBorder, CardShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = StitchPrimaryContainer, strokeWidth = 2.dp)
                    }
                }
            }
        }

        // Bottom spacer for FAB
        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

// ===== HERO METRIC CARD (Bento hero tile) =====
@Composable
private fun HeroMetricCard(
    label: String,
    value: String,
    subtitle: String,
    trendIcon: ImageVector,
    trendText: String,
    gradientColors: List<Color>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(160.dp)
            .clip(CardShape)
            .background(Brush.linearGradient(gradientColors))
            .border(0.5.dp, accentColor.copy(alpha = 0.3f), CardShape)
            .padding(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 26.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(trendIcon, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                Text(text = trendText, style = MaterialTheme.typography.labelSmall, color = accentColor)
            }
        }
    }
}

// ===== COMPACT STAT CARD =====
@Composable
private fun CompactMetricCard(
    label: String,
    value: String,
    valueColor: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(80.dp)
            .clip(CardShape)
            .background(StitchSurface)
            .border(0.5.dp, StitchBorder, CardShape)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = StitchTextSecondary)
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = valueColor.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ===== QUICK ACTION CHIP =====
@Composable
private fun QuickActionChip(
    icon: ImageVector,
    label: String,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(ButtonShape)
            .background(Brush.linearGradient(gradient))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

// ===== MODERN INVOICE ROW =====
@Composable
private fun ModernInvoiceRow(
    initials: String,
    name: String,
    invoiceNumber: String,
    amount: String,
    statusLabel: String,
    badgeBg: Color,
    badgeText: Color,
    avatarBg: Color,
    avatarContent: Color,
    avatarColor: Color,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with colored background
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = initials, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = StitchTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = invoiceNumber, style = MaterialTheme.typography.labelSmall, color = StitchTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = StitchTextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier.clip(BadgeShape).background(badgeBg).padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(text = statusLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = badgeText, fontSize = 10.sp)
                }
            }
        }
        if (showDivider) {
            HorizontalDivider(color = StitchBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 14.dp))
        }
    }
}

// ===== MODERN EMPTY STATE =====
@Composable
private fun ModernEmptyState() {
    Box(
        modifier = Modifier.fillMaxWidth().height(140.dp)
            .clip(CardShape).background(StitchSurface)
            .border(0.5.dp, StitchBorder, CardShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = StitchTextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
            Text(stringResource(R.string.no_invoices_yet), style = MaterialTheme.typography.bodyMedium, color = StitchTextSecondary)
            Text(stringResource(R.string.create_first_invoice), style = MaterialTheme.typography.labelSmall, color = StitchTextSecondary.copy(alpha = 0.7f))
        }
    }
}

private fun getAvatarColor(name: String): Color {
    val avatarColors = listOf(
        StitchPrimaryContainer,
        StitchSecondaryContainer,
        StitchTertiaryContainer,
        StitchSecondary,
        StitchTertiary,
        Color(0xFF4A9EFF),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899),
    )
    val index = abs(name.hashCode()) % avatarColors.size
    return avatarColors[index]
}

// Helper data class for tuple return
private data class Quadruple<T1, T2, T3, T4, T5>(val v1: T1, val v2: T2, val v3: T3, val v4: T4, val v5: T5)
