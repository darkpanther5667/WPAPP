package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aistudio.sharmakhata.pqmzvk.R
import com.aistudio.sharmakhata.pqmzvk.ui.components.EmptyState
import com.aistudio.sharmakhata.pqmzvk.ui.components.ShimmerLoading
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.MainViewModel
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.UiState
import com.aistudio.sharmakhata.pqmzvk.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsOverviewScreen(
  viewModel: MainViewModel,
  onCustomerClick: (String) -> Unit,
  onNavigateToSearch: () -> Unit = {},
  onMenuClick: () -> Unit = {},
  onBack: () -> Unit = {}
) {
  val dbState by viewModel.dbState.collectAsState()
  val context = LocalContext.current

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.bills_overview)) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
          }
        },
        actions = {
          IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.Menu, contentDescription = "Menu")
          }
          IconButton(onClick = onNavigateToSearch) {
            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background,
          titleContentColor = MaterialTheme.colorScheme.onBackground
        )
      )
    }
  ) { padding ->
    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
      state = pullToRefreshState,
      isRefreshing = refreshing,
      onRefresh = {
        scope.launch {
          refreshing = true
          LiveSyncManager.forceRefresh()
          refreshing = false
        }
      },
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      when (dbState) {
        is UiState.Loading -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .align(Alignment.Center),
            contentAlignment = Alignment.Center
          ) {
            ShimmerLoading()
          }
        }
        is UiState.Error -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .align(Alignment.Center)
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            EmptyState(
              message = stringResource(R.string.error_search_prefix, (dbState as UiState.Error).message),
              description = stringResource(R.string.error_loading_bills),
              icon = Icons.Default.Error
            )
          }
        }
        is UiState.Success -> {
          val db = (dbState as UiState.Success).data
          if (db.bills.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
              contentAlignment = Alignment.Center
            ) {
              EmptyState(
                message = stringResource(R.string.no_bills_yet),
                description = stringResource(R.string.no_bills_for_customer),
                icon = Icons.Default.Receipt
              )
            }
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              items(db.bills.take(50)) { bill ->
                BillOverviewCard(bill, onCustomerClick)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun BillOverviewCard(
  bill: com.aistudio.sharmakhata.pqmzvk.data.model.Bill,
  onCustomerClick: (String) -> Unit
) {
  val isPaid = bill.status == "paid"
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCustomerClick(bill.customerId) },
    shape = CardShape,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low)
  ) {
    Row(
      modifier = Modifier.padding(Spacing.large),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(ComponentSize.iconContainerLarge)
          .clip(ActionIconShape)
          .background(if (isPaid) SuccessGreen.copy(alpha = 0.1f) else IndigoContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          Icons.Default.Receipt,
          contentDescription = null,
          tint = if (isPaid) SuccessGreen else IndigoPrimary,
          modifier = Modifier.size(IconSize.small)
        )
      }

      Spacer(modifier = Modifier.width(Spacing.large))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Bill #${bill.id.take(8).uppercase()}",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = FormatUtils.formatCurrency(bill.total),
          style = AmountSmallStyle,
          color = if (isPaid) SuccessGreen else MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = FormatUtils.formatDate(bill.createdAt),
          style = MaterialTheme.typography.bodySmall,
          color = StitchTextSecondary
        )
      }
    }
  }
}
