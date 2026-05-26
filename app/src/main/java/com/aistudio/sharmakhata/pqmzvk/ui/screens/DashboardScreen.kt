package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.sharmakhata.pqmzvk.data.model.DailyReport
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.MainViewModel
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.UiState
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel, 
    onNavigateToCustomers: () -> Unit, 
    onNavigateToWebView: () -> Unit
) {
    val reportState by viewModel.reportState.collectAsState()

    // Data is fetched in LoadingScreen; use the Refresh action to sync again.

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Profile Avatar Placeholder
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4F46E5).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A", 
                                color = Color(0xFF4F46E5), 
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Text("Grahbook", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, letterSpacing = (-0.5).sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF191c1e)
                ),
                actions = {
                    IconButton(onClick = onNavigateToWebView) {
                        Icon(Icons.Default.Language, contentDescription = "Web Dashboard", tint = Color(0xFF4F46E5))
                    }
                    IconButton(onClick = { viewModel.fetchData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF3c4a3d))
                    }
                },
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = Color(0xFFe0e3e5),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCustomers,
                containerColor = Color(0xFF25d366),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Group, "Customers") },
                text = { Text("View Customers", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        val pullToRefreshState = rememberPullToRefreshState()
        
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = reportState is UiState.Loading,
            onRefresh = { viewModel.fetchData() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFf7f9fb))
        ) {
            when (reportState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF25d366)
                    )
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Error, 
                            contentDescription = "Error", 
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Error: ${(reportState as UiState.Error).message}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { viewModel.fetchData() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                        ) {
                            Text("Retry")
                        }
                    }
                }
                is UiState.Success -> {
                    val report = (reportState as UiState.Success).data
                    DashboardContent(report, onNavigateToCustomers)
                }
            }
        }
    }
}

@Composable
fun DashboardContent(report: DailyReport, onNavigateToCustomers: () -> Unit) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp), // Safe margin for FAB and Navigation
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Message
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Good morning, Alex",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191c1e)
            )
            Text(
                text = "Here's how your business is performing today.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF3c4a3d)
            )
        }

        // Bento Grid Metrics Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Revenue Bento Card (Col-span 2)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFe0e3e5))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL REVENUE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3c4a3d),
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF25d366).copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Payments, 
                                contentDescription = "Revenue", 
                                tint = Color(0xFF25d366),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    val formattedRevenue = formatRs(if (report.billsTotal > 0) report.billsTotal else 12480.0)
                    Text(
                        text = formattedRevenue,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191c1e)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp, 
                            contentDescription = "Trending Up", 
                            tint = Color(0xFF25d366),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "+12.5% from last month",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF25d366)
                        )
                    }
                }
            }

            // Row for the two half-width Bento cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Active Bots Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFe0e3e5))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ACTIVE BOTS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3c4a3d),
                            letterSpacing = 0.8.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "42",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF191c1e)
                            )
                            // Pulsing Dot
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "alpha"
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF25d366).copy(alpha = alpha))
                            )
                        }
                        Text(
                            text = "Sessions active",
                            fontSize = 11.sp,
                            color = Color(0xFF3c4a3d)
                        )
                    }
                }

                // New Orders Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFe0e3e5))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "NEW ORDERS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3c4a3d),
                            letterSpacing = 0.8.sp
                        )
                        val orderCount = if (report.billsCount > 0) report.billsCount else 15
                        Text(
                            text = orderCount.toString(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191c1e)
                        )
                        Text(
                            text = "Pending dispatch",
                            fontSize = 11.sp,
                            color = Color(0xFF3c4a3d)
                        )
                    }
                }
            }
        }

        // Premium Mock Chart Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFe0e3e5))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Revenue Over Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191c1e)
                    )
                    Text(
                        text = "Last 7 Days",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF3c4a3d)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // Elegant custom-drawn chart line via Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .drawBehind {
                            // Draw background subtle grid bars
                            val barCount = 7
                            val barWidth = 4.dp.toPx()
                            val spacing = (size.width - (barCount * barWidth)) / (barCount - 1)
                            val barHeights = listOf(0.2f, 0.4f, 0.35f, 0.6f, 0.55f, 0.85f, 0.95f)

                            for (i in 0 until barCount) {
                                val x = i * (barWidth + spacing)
                                val h = size.height * barHeights[i]
                                drawRoundRect(
                                    color = Color(0xFFeceef0),
                                    topLeft = Offset(x, size.height - h),
                                    size = androidx.compose.ui.geometry.Size(barWidth, h),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                                )
                            }

                            // Draw Bezier Gradient Fill
                            val fillPath = Path().apply {
                                moveTo(0f, size.height)
                                lineTo(0f, size.height * (1f - 0.2f))
                                cubicTo(
                                    size.width * 0.25f, size.height * (1f - 0.5f),
                                    size.width * 0.5f, size.height * (1f - 0.3f),
                                    size.width * 0.75f, size.height * (1f - 0.85f)
                                )
                                lineTo(size.width, size.height * (1f - 0.95f))
                                lineTo(size.width, size.height)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF25d366).copy(alpha = 0.15f),
                                        Color(0xFF25d366).copy(alpha = 0f)
                                    )
                                )
                            )

                            // Draw bezier line
                            val strokePath = Path().apply {
                                moveTo(0f, size.height * (1f - 0.2f))
                                cubicTo(
                                    size.width * 0.25f, size.height * (1f - 0.5f),
                                    size.width * 0.5f, size.height * (1f - 0.3f),
                                    size.width * 0.75f, size.height * (1f - 0.85f)
                                )
                                lineTo(size.width, size.height * (1f - 0.95f))
                            }
                            drawPath(
                                path = strokePath,
                                color = Color(0xFF25d366),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mon", fontSize = 11.sp, color = Color(0xFF3c4a3d))
                    Text("Sun", fontSize = 11.sp, color = Color(0xFF3c4a3d))
                }
            }
        }

        // Quick Actions Horizontal Scroll Section
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191c1e),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickActionItem(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong, 
                    label = "Create Invoice", 
                    containerColor = Color(0xFFdae2fd), 
                    iconColor = Color(0xFF3f465c),
                    onClick = onNavigateToCustomers
                )
            }
        }

        // (Removed) Live Bot Activity mock section
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(112.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFe0e3e5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(containerColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191c1e),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun ActivityRow(
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    title: String,
    time: String,
    chatId: String,
    statusIcon: ImageVector,
    statusColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFe0e3e5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191c1e),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$time • $chatId",
                        fontSize = 11.sp,
                        color = Color(0xFF3c4a3d)
                    )
                }
            }
            Icon(
                imageVector = statusIcon, 
                contentDescription = null, 
                tint = statusColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatRs(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    format.maximumFractionDigits = 0
    return format.format(amount)
}
