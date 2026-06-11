package com.aistudio.sharmakhata.pqmzvk.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
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
import kotlin.math.abs

data class DrawerMenuItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val badge: Int = 0
)

data class DrawerMenuSection(
    val title: String,
    val icon: ImageVector,
    val items: List<DrawerMenuItem>
)

@Composable
fun AppSidebarDrawer(
    drawerState: DrawerState,
    currentScreen: String,
    shopName: String,
    shopOwner: String,
    shopPhone: String = "",
    customerCount: Int,
    billCount: Int,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {

    val menuSections = listOf(
        DrawerMenuSection(
            title = "SALES & BILLING",
            icon = Icons.Outlined.PointOfSale,
            items = listOf(
                DrawerMenuItem("dashboard", "Dashboard", Icons.Outlined.Home, Icons.Filled.Home),
                DrawerMenuItem("customers", "Customers", Icons.Outlined.People, Icons.Filled.People),
                DrawerMenuItem("bills", "Invoices", Icons.Outlined.Receipt, Icons.Filled.Receipt),
                DrawerMenuItem("quick_bill", "Quick Bill", Icons.Outlined.Bolt, Icons.Filled.Bolt),
            )
        ),
        DrawerMenuSection(
            title = "PURCHASES & EXPENSES",
            icon = Icons.Outlined.ShoppingCart,
            items = listOf(
                DrawerMenuItem("purchases", "Purchases", Icons.Outlined.LocalShipping, Icons.Filled.LocalShipping),
                DrawerMenuItem("expenses", "Expenses", Icons.Outlined.MoneyOff, Icons.Filled.MoneyOff),
            )
        ),
        DrawerMenuSection(
            title = "STOCK & REPORTS",
            icon = Icons.Outlined.Assessment,
            items = listOf(
                DrawerMenuItem("items", "Inventory", Icons.Outlined.Inventory2, Icons.Filled.Inventory),
                DrawerMenuItem("reports", "Reports", Icons.Outlined.Assessment, Icons.Filled.Assessment),
                DrawerMenuItem("financial_reports", "Accounting", Icons.Outlined.AccountBalance, Icons.Filled.AccountBalance),
                DrawerMenuItem("gst_returns", "GST Returns", Icons.Outlined.Description, Icons.Filled.Description),
            )
        ),
        DrawerMenuSection(
            title = "SETTINGS & ADMIN",
            icon = Icons.Outlined.Settings,
            items = listOf(
                DrawerMenuItem("profile", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings),
                DrawerMenuItem("invoice_templates", "Invoice Templates", Icons.Outlined.Description, Icons.Filled.Description),
                DrawerMenuItem("staff", "Staff Management", Icons.Outlined.SupervisorAccount, Icons.Filled.SupervisorAccount),
                DrawerMenuItem("share", "Share App", Icons.Outlined.Share, Icons.Filled.Share),
            )
        ),
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(310.dp),
                drawerContainerColor = Color.Transparent,
                drawerContentColor = StitchTextPrimary,
                drawerTonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(StitchBg)
                ) {
                    // ===== DRAWER HEADER =====
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        StitchPrimaryContainer.copy(alpha = 0.15f),
                                        StitchBg
                                    ),
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY)
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(StitchPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = shopName.firstOrNull()?.uppercase().toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = StitchOnPrimaryContainer,
                                    fontSize = 22.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Shop Name
                            Text(
                                text = shopName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = StitchTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (shopOwner.isNotBlank()) {
                                Text(
                                    text = shopOwner,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StitchTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Stats row
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                HeaderStat("Customers", "$customerCount")
                                HeaderStat("Bills", "$billCount")
                            }
                        }
                    }

                    // ===== SCROLLABLE MENU =====
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        menuSections.forEachIndexed { index, section ->
                            // Section header
                            Row(
                                modifier = Modifier.padding(start = 20.dp, top = if (index == 0) 8.dp else 12.dp, bottom = 6.dp, end = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    section.icon,
                                    contentDescription = null,
                                    tint = StitchTextSecondary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StitchTextSecondary.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }

                            section.items.forEach { item ->
                                DrawerNavItem(
                                    item = item,
                                    isSelected = currentScreen == item.id,
                                    onClick = {
                                        onNavigate(item.id)
                                    }
                                )
                            }

                            // Divider between sections (not after the last one)
                            if (index < menuSections.size - 1) {
                                HorizontalDivider(
                                    color = StitchBorder,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // ===== PROFILE CARD FOOTER =====
                    HorizontalDivider(
                        color = StitchBorder,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    ProfileFooterCard(
                        shopName = shopName,
                        shopOwner = shopOwner,
                        shopPhone = shopPhone,
                        onTap = { onNavigate("profile") }
                    )

                    // Logout
                    DrawerNavItem(
                        item = DrawerMenuItem("logout", "Logout", Icons.AutoMirrored.Filled.Logout, Icons.AutoMirrored.Filled.Logout),
                        isSelected = false,
                        iconTint = DebtRed,
                        labelColor = DebtRed,
                        onClick = onLogout
                    )

                    // Version
                    Text(
                        text = "v${com.aistudio.sharmakhata.pqmzvk.BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelSmall,
                        color = StitchTextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.padding(start = 20.dp, top = 2.dp, bottom = 12.dp)
                    )
                }
            }
        },
        content = {
            Box {
                content()
            }
        }
    )
}

@Composable
private fun ProfileFooterCard(
    shopName: String,
    shopOwner: String,
    shopPhone: String,
    onTap: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(StitchPrimaryContainer.copy(alpha = 0.08f))
            .border(0.5.dp, StitchPrimaryContainer.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Small avatar
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(StitchPrimaryContainer.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                tint = StitchPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = shopName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = StitchTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (shopPhone.isNotBlank()) {
                Text(
                    text = if (shopPhone.length >= 10) "+91 ${shopPhone.takeLast(10)}" else shopPhone,
                    style = MaterialTheme.typography.labelSmall,
                    color = StitchTextSecondary.copy(alpha = 0.7f)
                )
            }
        }

        Icon(
            Icons.Outlined.Settings,
            contentDescription = "Settings",
            tint = StitchTextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun DrawerNavItem(
    item: DrawerMenuItem,
    isSelected: Boolean,
    iconTint: Color? = null,
    labelColor: Color? = null,
    onClick: () -> Unit
) {
    val bgAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        label = "drawerBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) StitchPrimaryContainer.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = iconTint ?: if (isSelected) StitchPrimaryContainer else StitchTextSecondary.copy(alpha = 0.7f),
            modifier = Modifier.size(21.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = labelColor ?: if (isSelected) StitchPrimaryContainer else StitchTextSecondary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (item.badge > 0) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(StitchTertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.badge.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = StitchOnTertiaryContainer,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun HeaderStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = StitchPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = StitchTextSecondary.copy(alpha = 0.7f)
        )
    }
}
