package com.aistudio.sharmakhata.pqmzvk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aistudio.sharmakhata.pqmzvk.ui.screens.BillCreationScreen
import com.aistudio.sharmakhata.pqmzvk.ui.screens.BillsScreen
import com.aistudio.sharmakhata.pqmzvk.ui.screens.CustomerDetailScreen
import com.aistudio.sharmakhata.pqmzvk.ui.screens.CustomersScreen
import com.aistudio.sharmakhata.pqmzvk.ui.screens.DashboardScreen
import com.aistudio.sharmakhata.pqmzvk.ui.screens.LedgerScreen
import com.aistudio.sharmakhata.pqmzvk.ui.screens.LoadingScreen
import com.aistudio.sharmakhata.pqmzvk.ui.screens.PaymentRecordingScreen
import com.aistudio.sharmakhata.pqmzvk.ui.screens.WebViewScreen
import com.aistudio.sharmakhata.pqmzvk.ui.screens.AddCustomerScreen
import com.aistudio.sharmakhata.pqmzvk.ui.screens.LoginScreen
import com.aistudio.sharmakhata.pqmzvk.ui.screens.RegisterStoreScreen
import com.aistudio.sharmakhata.pqmzvk.ui.screens.SearchScreen
import com.aistudio.sharmakhata.pqmzvk.ui.components.ShimmerLoading
import com.aistudio.sharmakhata.pqmzvk.ui.components.EmptyState
import com.aistudio.sharmakhata.pqmzvk.util.FormatUtils
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.MainViewModel
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.UiState
import com.aistudio.sharmakhata.pqmzvk.ui.theme.GrahbookTheme
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import com.aistudio.sharmakhata.pqmzvk.BuildConfig
import com.aistudio.sharmakhata.pqmzvk.util.SessionManager
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      GrahbookTheme {
        AppNavigation()
      }
    }
  }
}

@Composable
fun AppNavigation(viewModel: MainViewModel = viewModel()) {
  val context = LocalContext.current
  val navController = rememberNavController()
  var currentScreen by remember { mutableStateOf("dashboard") }
  val startDestination = if (SessionManager.token.isNullOrBlank()) "login" else "loading"

  // Observe 401 unauthorized events from the server
  val logoutEvent by viewModel.logoutEvent.collectAsState()
  LaunchedEffect(logoutEvent) {
    if (logoutEvent) {
      SessionManager.clear(context)
      currentScreen = "login"
      navController.navigate("login") {
        popUpTo(0) { inclusive = true }
      }
      viewModel.consumeLogoutEvent()
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .navigationBarsPadding(),
    bottomBar = {
      if (currentScreen != "login" && currentScreen != "loading" && currentScreen != "register_store") {
        BottomNavigationBar(
          currentScreen = currentScreen,
          onNavigate = { screen ->
            when (screen) {
              "dashboard" -> navController.navigate("dashboard") {
                popUpTo("dashboard") { inclusive = true }
                launchSingleTop = true
              }
              "customers" -> navController.navigate("customers") {
                popUpTo("customers") { inclusive = true }
                launchSingleTop = true
              }
              "bills" -> navController.navigate("bills") {
                popUpTo("bills") { inclusive = true }
                launchSingleTop = true
              }
              "profile" -> navController.navigate("profile") {
                popUpTo("profile") { inclusive = true }
                launchSingleTop = true
              }
            }
            currentScreen = screen
          }
        )
      }
    }
  ) { padding ->
    NavHost(
      navController = navController,
      startDestination = startDestination,
      modifier = Modifier.padding(padding)
    ) {
      composable("login") {
        LoginScreen(
          viewModel = viewModel,
          onLoggedIn = {
            currentScreen = "loading"
            navController.navigate("loading") {
              popUpTo("login") { inclusive = true }
              launchSingleTop = true
            }
          },
          onRegisterStore = {
            currentScreen = "register_store"
            navController.navigate("register_store")
          }
        )
      }
      composable("register_store") {
        RegisterStoreScreen(
          viewModel = viewModel,
          onBack = {
            currentScreen = "login"
            navController.popBackStack()
          },
          onSuccess = {
            // After registration, take user back to login with storeId shown in snackbar.
            currentScreen = "login"
            navController.popBackStack()
          }
        )
      }
      composable("loading") {
        LoadingScreen(
          viewModel = viewModel,
          onReady = {
            currentScreen = "dashboard"
            navController.navigate("dashboard") {
              popUpTo("loading") { inclusive = true }
              launchSingleTop = true
            }
          },
          onBackToLogin = {
            // Clear session and navigate back to login
            SessionManager.clear(context)
            currentScreen = "login"
            navController.navigate("login") {
              popUpTo("loading") { inclusive = true }
              launchSingleTop = true
            }
          }
        )
      }
      composable("dashboard") {
        DashboardScreen(
          viewModel = viewModel,
          onNavigateToCustomers = {
            currentScreen = "customers"
            navController.navigate("customers")
          },
          onNavigateToWebView = {
            currentScreen = "webview"
            navController.navigate("webview")
          },
          onCreateInvoice = {
            currentScreen = "customers"
            navController.navigate("customers")
          },
          onAddCustomer = {
            currentScreen = "add_customer"
            navController.navigate("add_customer")
          },
          onRecordPayment = {
            currentScreen = "customers"
            navController.navigate("customers")
          },
          onViewReports = {
            // Reports screen coming in Phase 3
          },
          onSendReminder = {
            currentScreen = "customers"
            navController.navigate("customers")
          },
          onWhatsApp = {
            currentScreen = "webview"
            navController.navigate("webview")
          }
        )
      }
      composable("customers") {
        CustomersScreen(
          viewModel = viewModel,
          onBack = {
            currentScreen = "dashboard"
            navController.popBackStack()
          },
          onAddCustomer = {
            currentScreen = "add_customer"
            navController.navigate("add_customer")
          },
          onCustomerClick = { customerId ->
            currentScreen = "customer_detail"
            navController.navigate("customer_detail/$customerId")
          },
          onNavigateToSearch = {
            navController.navigate("search")
          }
        )
      }
      composable("add_customer") {
        AddCustomerScreen(
          viewModel = viewModel,
          onBack = {
            currentScreen = "customers"
            navController.popBackStack()
          }
        )
      }
      composable("customer_detail/{customerId}",
        arguments = listOf(navArgument("customerId") { type = NavType.StringType })
      ) { backStackEntry ->
        val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
        val dbState by viewModel.dbState.collectAsState()
        val customerName = when (dbState) {
          is com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.UiState.Success -> {
            val db = (dbState as com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.UiState.Success).data
            val customer = db.customers.find { it.id == customerId }
            customer?.name ?: "Customer"
          }
          else -> "Customer"
        }
        
        CustomerDetailScreen(
          viewModel = viewModel,
          customerId = customerId,
          onBack = {
            currentScreen = "customers"
            navController.popBackStack()
          },
          onAddPayment = {
            currentScreen = "payment"
            navController.navigate("payment/$customerId")
          },
          onCreateBill = {
            currentScreen = "bill"
            navController.navigate("bill/$customerId")
          },
          onViewBills = {
            currentScreen = "bills"
            navController.navigate("bills/$customerId")
          },
          onViewLedger = {
            currentScreen = "ledger"
            navController.navigate("ledger/$customerId")
          }
        )
      }
      composable("payment/{customerId}",
        arguments = listOf(navArgument("customerId") { type = NavType.StringType })
      ) { backStackEntry ->
        val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
        val dbState by viewModel.dbState.collectAsState()
        val customerName = when (dbState) {
          is com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.UiState.Success -> {
            val db = (dbState as com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.UiState.Success).data
            val customer = db.customers.find { it.id == customerId }
            customer?.name ?: "Customer"
          }
          else -> "Customer"
        }
        
        PaymentRecordingScreen(
          viewModel = viewModel,
          customerId = customerId,
          customerName = customerName,
          onBack = {
            currentScreen = "customer_detail"
            navController.popBackStack()
          }
        )
      }
      composable("bill/{customerId}",
        arguments = listOf(navArgument("customerId") { type = NavType.StringType })
      ) { backStackEntry ->
        val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
        val dbState by viewModel.dbState.collectAsState()
        val customerName = when (dbState) {
          is com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.UiState.Success -> {
            val db = (dbState as com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.UiState.Success).data
            val customer = db.customers.find { it.id == customerId }
            customer?.name ?: "Customer"
          }
          else -> "Customer"
        }
        
        BillCreationScreen(
          viewModel = viewModel,
          customerId = customerId,
          customerName = customerName,
          onBack = {
            currentScreen = "customer_detail"
            navController.popBackStack()
          }
        )
      }
      composable("ledger/{customerId}",
        arguments = listOf(navArgument("customerId") { type = NavType.StringType })
      ) { backStackEntry ->
        val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
        LedgerScreen(
          viewModel = viewModel,
          customerId = customerId,
          onBack = {
            currentScreen = "customer_detail"
            navController.popBackStack()
          }
        )
      }
      composable("bills/{customerId}",
        arguments = listOf(navArgument("customerId") { type = NavType.StringType })
      ) { backStackEntry ->
        val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
        BillsScreen(
          viewModel = viewModel,
          customerId = customerId,
          onBack = {
            currentScreen = "customer_detail"
            navController.popBackStack()
          },
          onOpenPdf = { billId ->
            currentScreen = "webview"
            navController.navigate("webview_pdf/$billId")
          }
        )
      }
      composable("webview_pdf/{billId}",
        arguments = listOf(navArgument("billId") { type = NavType.StringType })
      ) { backStackEntry ->
        val billId = backStackEntry.arguments?.getString("billId") ?: ""
        WebViewScreen(
          url = "https://wpapp-xz9l.onrender.com/api/bill/$billId/pdf",
          onBack = {
            currentScreen = "bills"
            navController.popBackStack()
          }
        )
      }
      composable("webview") {
        WebViewScreen(
          url = "https://wpapp-xz9l.onrender.com",
          onBack = {
            currentScreen = "dashboard"
            navController.popBackStack()
          }
        )
      }
      composable("bills") {
        BillsOverviewScreen(
          viewModel = viewModel,
          onCustomerClick = { customerId ->
            currentScreen = "customer_detail"
            navController.navigate("customer_detail/$customerId")
          },
          onNavigateToSearch = {
            navController.navigate("search")
          }
        )
      }
      composable("profile") {
        ProfileScreen(
          viewModel = viewModel,
          onLogout = {
            currentScreen = "login"
            SessionManager.clear(context)
            navController.navigate("login") {
              popUpTo("dashboard") { inclusive = true }
              launchSingleTop = true
            }
          }
        )
      }
      composable("search") {
        SearchScreen(
          viewModel = viewModel,
          onBack = {
            navController.popBackStack()
          },
          onCustomerClick = { customerId ->
            currentScreen = "customer_detail"
            navController.navigate("customer_detail/$customerId")
          },
          onBillClick = { customerId, billId ->
            currentScreen = "bills"
            navController.navigate("bills/$customerId")
          }
        )
      }
    }
  }
}

@Composable
fun BottomNavigationBar(
  currentScreen: String,
  onNavigate: (String) -> Unit
) {
  val items = listOf(
    BottomNavItem("dashboard", "Home", Icons.Default.Home),
    BottomNavItem("customers", "Customers", Icons.Default.Group),
    BottomNavItem("bills", "Bills", Icons.Default.Receipt),
    BottomNavItem("profile", "Profile", Icons.Default.Person)
  )

  NavigationBar(
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 4.dp
  ) {
    items.forEach { item ->
      val selected = currentScreen == item.screen
      NavigationBarItem(
        selected = selected,
        onClick = { onNavigate(item.screen) },
        icon = {
          Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (selected) IndigoPrimary else Slate400
          )
        },
        label = {
          Text(
            text = item.label,
            color = if (selected) IndigoPrimary else Slate500,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            style = MaterialTheme.typography.labelSmall
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = IndigoPrimary,
          selectedTextColor = IndigoPrimary,
          unselectedIconColor = Slate400,
          unselectedTextColor = Slate500,
          indicatorColor = IndigoContainer
        )
      )
    }
  }
}

data class BottomNavItem(
  val screen: String,
  val label: String,
  val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsOverviewScreen(
  viewModel: MainViewModel,
  onCustomerClick: (String) -> Unit,
  onNavigateToSearch: () -> Unit = {}
) {
  val dbState by viewModel.dbState.collectAsState()
  val context = LocalContext.current

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Bills Overview") },
        actions = {
          IconButton(onClick = onNavigateToSearch) {
            Icon(Icons.Default.Search, contentDescription = "Search")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background,
          titleContentColor = MaterialTheme.colorScheme.onBackground
        )
      )
    }
  ) { padding ->
    val pullToRefreshState = rememberPullToRefreshState()
    
    PullToRefreshBox(
      state = pullToRefreshState,
      isRefreshing = dbState is UiState.Loading,
      onRefresh = { viewModel.fetchData(context) },
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
              message = "Error: ${(dbState as UiState.Error).message}",
              description = "There was an error loading bills data.",
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
                message = "No bills yet",
                description = "Bills will appear here once created.",
                icon = Icons.Default.Receipt
              )
            }
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              items(db.bills.take(20)) { bill ->
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
          color = TextSecondaryLight
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  viewModel: MainViewModel,
  onLogout: () -> Unit
) {
  val context = androidx.compose.ui.platform.LocalContext.current
  val token = SessionManager.token
  val dbState by viewModel.dbState.collectAsState()
  val storeName = when (val s = dbState) {
    is UiState.Success -> s.data.shop?.name ?: "My Store"
    else -> "My Store"
  }
  val ownerName = when (val s = dbState) {
    is UiState.Success -> s.data.shop?.owner ?: ""
    else -> ""
  }
  val customerCount = when (val s = dbState) {
    is UiState.Success -> s.data.customers.size
    else -> 0
  }
  val billCount = when (val s = dbState) {
    is UiState.Success -> s.data.bills.size
    else -> 0
  }
  
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Profile", fontWeight = FontWeight.SemiBold) },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface
        )
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(Spacing.large),
      verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap)
    ) {
      // Profile Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier.padding(Spacing.xxlarge),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(Spacing.large)
        ) {
          Box(
            modifier = Modifier
              .size(ComponentSize.avatarLarge + 28.dp)
              .clip(CircleShape)
              .background(Brush.linearGradient(GradientIndigo)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = (storeName.firstOrNull()?.uppercase() ?: "S").toString(),
              color = Color.White,
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold
            )
          }
          
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = storeName,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            if (ownerName.isNotBlank()) {
              Text(
                text = ownerName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryLight
              )
            }
            Spacer(modifier = Modifier.height(Spacing.small))
            // Status badge
            Box(
              modifier = Modifier
                .clip(BadgeShape)
                .background(if (token != null) BadgePaidBg else BadgeUnpaidBg)
                .padding(horizontal = Spacing.medium, vertical = Spacing.xsmall)
            ) {
              Text(
                text = if (token != null) "Active" else "Not logged in",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (token != null) BadgePaidText else BadgeUnpaidText
              )
            }
          }
        }
      }

      // Stats row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
      ) {
        Card(
          modifier = Modifier.weight(1f),
          shape = CardShape,
          elevation = CardDefaults.cardElevation(defaultElevation = Elevation.flat),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(text = customerCount.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = IndigoPrimary)
            Text(text = "Customers", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
          }
        }
        Card(
          modifier = Modifier.weight(1f),
          shape = CardShape,
          elevation = CardDefaults.cardElevation(defaultElevation = Elevation.flat),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(text = billCount.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = EmeraldSecondary)
            Text(text = "Bills", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
          }
        }
      }
      
      // App Info
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.flat),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier.padding(Spacing.large),
          verticalArrangement = Arrangement.spacedBy(Spacing.xsmall)
        ) {
          Text(
            text = "APP INFORMATION",
            style = SectionOverlineStyle,
            color = TextTertiaryLight
          )
          Spacer(modifier = Modifier.height(Spacing.small))
          
          InfoRow("Version", BuildConfig.VERSION_NAME)
          HorizontalDivider(thickness = 0.5.dp, color = DividerColor)
          InfoRow("Build", BuildConfig.BUILD_TYPE)
          HorizontalDivider(thickness = 0.5.dp, color = DividerColor)
          InfoRow("Sync Status", if (token != null) "Active" else "Inactive")
        }
      }
      
      // Logout Button
      Button(
        onClick = onLogout,
        modifier = Modifier.fillMaxWidth().height(ComponentSize.buttonHeight),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error
        ),
        shape = ButtonShape
      ) {
        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(IconSize.small))
        Spacer(modifier = Modifier.width(Spacing.small))
        Text("Logout", style = MaterialTheme.typography.labelLarge)
      }
    }
  }
}

@Composable
fun InfoRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium
    )
  }
}

