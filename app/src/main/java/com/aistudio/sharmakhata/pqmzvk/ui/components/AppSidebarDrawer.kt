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

@Composable
fun AppSidebarDrawer(
    drawerState: DrawerState,
    currentScreen: String,
    shopName: String,
    shopOwner: String,
    customerCount: Int,
    billCount: Int,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {

    val primaryMenuItems = listOf(
        DrawerMenuItem("dashboard", "Dashboard", Icons.Outlined.Home, Icons.Filled.Home),
        DrawerMenuItem("customers", "Customers", Icons.Outlined.People, Icons.Filled.People),
        DrawerMenuItem("bills", "Invoices", Icons.Outlined.Receipt, Icons.Filled.Receipt),
        DrawerMenuItem("reports", "Reports", Icons.Outlined.Assessment, Icons.Filled.Assessment),
        DrawerMenuItem("financial_reports", "Accounting", Icons.Outlined.AccountBalance, Icons.Filled.AccountBalance),
        DrawerMenuItem("items", "Inventory", Icons.Outlined.Inventory2, Icons.Filled.Inventory),
        DrawerMenuItem("expenses", "Expenses", Icons.Outlined.MoneyOff, Icons.Filled.MoneyOff),
        DrawerMenuItem("purchases", "Purchases", Icons.Outlined.LocalShipping, Icons.Filled.LocalShipping),
        DrawerMenuItem("quick_bill", "Quick Bill", Icons.Outlined.Bolt, Icons.Filled.Bolt),
    )

    val secondaryMenuItems = listOf(
        DrawerMenuItem("profile", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings),
        DrawerMenuItem("share", "Share App", Icons.Outlined.Share, Icons.Filled.Share),
        DrawerMenuItem("gst_returns", "GST Returns", Icons.Outlined.Assessment, Icons.Filled.Assessment),
        DrawerMenuItem("invoice_templates", "Invoice Templates", Icons.Outlined.Description, Icons.Filled.Description),
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
                            .height(220.dp)
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
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(StitchPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = shopName.firstOrNull()?.uppercase().toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = StitchOnPrimaryContainer,
                                    fontSize = 24.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Shop Name
                            Text(
                                text = shopName,
                                style = MaterialTheme.typography.headlineSmall,
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

                            Spacer(modifier = Modifier.height(12.dp))

                            // Stats row
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                HeaderStat("Customers", "$customerCount")
                                HeaderStat("Bills", "$billCount")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ===== SCROLLABLE MENU =====
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Primary nav items
                        Text(
                            text = "MAIN MENU",
                            style = MaterialTheme.typography.labelSmall,
                            color = StitchTextSecondary.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
                        )

                        primaryMenuItems.forEach { item ->
                            DrawerNavItem(
                                item = item,
                                isSelected = currentScreen == item.id,
                                onClick = {
                                    onNavigate(item.id)
                                }
                            )
                        }

                        HorizontalDivider(
                            color = StitchBorder,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )

                        // Secondary nav items
                        Text(
                            text = "OTHER",
                            style = MaterialTheme.typography.labelSmall,
                            color = StitchTextSecondary.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 8.dp)
                        )

                        secondaryMenuItems.forEach { item ->
                            DrawerNavItem(
                                item = item,
                                isSelected = currentScreen == item.id,
                                onClick = { onNavigate(item.id) }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // ===== FOOTER =====
                    HorizontalDivider(
                        color = StitchBorder,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    // Logout
                    DrawerNavItem(
                        item = DrawerMenuItem("logout", "Logout", Icons.AutoMirrored.Filled.Logout, Icons.AutoMirrored.Filled.Logout),
                        isSelected = false,
                        iconTint = Color(0xFFFF6B6B),
                        labelColor = Color(0xFFFF6B6B),
                        onClick = onLogout
                    )

                    // Version
                    Text(
                        text = "v${com.aistudio.sharmakhata.pqmzvk.BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelSmall,
                        color = StitchTextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 16.dp)
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
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = iconTint ?: if (isSelected) StitchPrimaryContainer else StitchTextSecondary.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp)
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
