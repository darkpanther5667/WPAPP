package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.sharmakhata.pqmzvk.data.model.Customer
import com.aistudio.sharmakhata.pqmzvk.data.model.FullDatabase
import com.aistudio.sharmakhata.pqmzvk.ui.components.AppAvatar
import com.aistudio.sharmakhata.pqmzvk.ui.components.EmptyState
import com.aistudio.sharmakhata.pqmzvk.ui.components.HamburgerAppBar
import com.aistudio.sharmakhata.pqmzvk.ui.components.ShimmerListItem
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.CustomerViewModel
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.UiState
import com.aistudio.sharmakhata.pqmzvk.util.FormatUtils
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager
import androidx.compose.ui.res.stringResource
import com.aistudio.sharmakhata.pqmzvk.R
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    viewModel: CustomerViewModel,
    onMenuClick: () -> Unit = {},
    shopInitial: String = "S",
    onBack: () -> Unit,
    onCustomerClick: (String) -> Unit,
    onAddCustomer: () -> Unit,
    onNavigateToSearch: () -> Unit = {},
) {
    val dbState by viewModel.dbState.collectAsState()
    val pagedCustomers = viewModel.customersPagingData.collectAsLazyPagingItems()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            HamburgerAppBar(
                title = stringResource(R.string.customers_title),
                onMenuClick = onMenuClick,
                shopInitial = shopInitial,
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search), tint = StitchTextSecondary)
                    }
                }
            )
        },
        containerColor = StitchBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCustomer,
                containerColor = StitchPrimaryContainer,
                contentColor = Color.White,
                shape = FabShape
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = stringResource(R.string.add_customer_action))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val pullToRefreshState = rememberPullToRefreshState()

        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = dbState is UiState.Loading,
            onRefresh = { scope.launch { LiveSyncManager.forceRefresh() } },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (dbState) {
                is UiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.large)
                    ) {
                        repeat(6) {
                            ShimmerListItem()
                            Spacer(modifier = Modifier.height(Spacing.small))
                        }
                    }
                }
                is UiState.Error -> {
                    EmptyState(
                        message = stringResource(R.string.error_loading_customers),
                        description = (dbState as UiState.Error).message,
                        icon = Icons.Default.Error,
                        actionLabel = stringResource(R.string.retry),
                        onAction = { scope.launch { LiveSyncManager.forceRefresh() } }
                    )
                }
                is UiState.Success -> {
                    val db = (dbState as UiState.Success).data
                    if (db.customers.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Person,
                            message = stringResource(R.string.no_customers_yet),
                            description = stringResource(R.string.add_first_customer),
                            actionLabel = stringResource(R.string.add_customer_action),
                            onAction = onAddCustomer
                        )
                    } else {
                        CustomersList(
                            db = db,
                            pagedCustomers = pagedCustomers,
                            onCustomerClick = onCustomerClick,
                            onAddCustomer = onAddCustomer,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomersList(
    db: FullDatabase,
    pagedCustomers: LazyPagingItems<Customer>,
    onCustomerClick: (String) -> Unit,
    onAddCustomer: () -> Unit,
    viewModel: CustomerViewModel
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.filter.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text(stringResource(R.string.search_by_name_or_phone), color = TextTertiaryLight) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = StitchPrimaryContainer)
            },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear), tint = TextSecondaryLight)
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.large, vertical = Spacing.small),
            shape = SearchBarShape,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = StitchPrimaryContainer,
                unfocusedIndicatorColor = CardBorder,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = TextPrimaryLight,
                unfocusedTextColor = TextPrimaryLight
            ),
            singleLine = true
        )

        // Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.large, vertical = Spacing.small),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == "All",
                    onClick = { viewModel.setFilter("All") },
                    label = { Text(stringResource(R.string.all_with_count, db.customers.size)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StitchTeal,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = TextSecondaryLight
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "With Outstanding",
                    onClick = { viewModel.setFilter("With Outstanding") },
                    label = { Text(stringResource(R.string.with_outstanding)) },
                    leadingIcon = if (selectedFilter == "With Outstanding") {
                        { Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ErrorRed,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = TextSecondaryLight
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "Paid",
                    onClick = { viewModel.setFilter("Paid") },
                    label = { Text(stringResource(R.string.paid_filter)) },
                    leadingIcon = if (selectedFilter == "Paid") {
                        { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SuccessGreen,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = TextSecondaryLight
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        if (pagedCustomers.itemCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.xxxlarge),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    Icon(
                        Icons.Outlined.PersonSearch,
                        contentDescription = null,
                        tint = TextTertiaryLight,
                        modifier = Modifier.size(IconSize.huge)
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) stringResource(R.string.no_customers_matching, searchQuery)
                        else stringResource(R.string.no_filter_customers, selectedFilter.lowercase()),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondaryLight,
                        textAlign = TextAlign.Center
                    )
                    if (searchQuery.isNotEmpty()) {
                        TextButton(onClick = { viewModel.setSearchQuery("") }) {
                            Text(stringResource(R.string.clear_search), color = IndigoPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = Spacing.large,
                    end = Spacing.large,
                    top = Spacing.small,
                    bottom = 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.listItemGap)
            ) {
                items(count = pagedCustomers.itemCount, key = { index -> pagedCustomers[index]?.id ?: index }) { index ->
                    val customer = pagedCustomers[index]
                    if (customer != null) {
                        CustomerCard(
                            customer = customer,
                            db = db,
                            colorIndex = abs(customer.id.hashCode()) % AvatarColors.size,
                            onClick = { onCustomerClick(customer.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCard(
    customer: Customer,
    db: FullDatabase,
    colorIndex: Int,
    onClick: () -> Unit
) {
    // Calculate balance
    val transactions = db.transactions.filter { it.customerId == customer.id }
    val bills = db.bills.filter { it.customerId == customer.id }
    val payments = transactions.filter { it.type == "payment" }.sumOf { it.amount }
    val credits = transactions.filter { it.type == "credit" }.sumOf { it.amount }
    val billTotal = bills.sumOf { it.total }
    val balance = credits + billTotal - payments

    val balanceColor = when {
        balance > 0 -> AmountDue
        balance < 0 -> AmountCredit
        else -> AmountNeutral
    }
    val balanceLabel = when {
        balance > 0 -> stringResource(R.string.due_label)
        balance < 0 -> stringResource(R.string.you_get_label)
        else -> stringResource(R.string.settled_label)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = ListCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored avatar
            AppAvatar(
                name = customer.name,
                size = ComponentSize.avatarLarge,
                colorIndex = colorIndex
            )

            Spacer(modifier = Modifier.width(Spacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xsmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        tint = StitchTeal.copy(alpha = 0.6f),
                        modifier = Modifier.size(IconSize.xsmall)
                    )
                    Text(
                        text = customer.phone ?: stringResource(R.string.no_phone),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Balance amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FormatUtils.formatCurrency(abs(balance)),
                    style = AmountSmallStyle,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor
                )
                Text(
                    text = balanceLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = balanceColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(Spacing.small))

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.view_details),
                tint = TextTertiaryLight,
                modifier = Modifier.size(IconSize.medium)
            )
        }
    }
}
