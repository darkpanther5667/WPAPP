package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.sharmakhata.pqmzvk.data.model.Bill
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import com.aistudio.sharmakhata.pqmzvk.util.FormatUtils

data class GstSummary(
    val totalTaxableSales: Double = 0.0,
    val totalCgst: Double = 0.0,
    val totalSgst: Double = 0.0,
    val totalIgst: Double = 0.0,
    val totalGst: Double = 0.0,
    val b2bCount: Int = 0,
    val b2cCount: Int = 0,
    val gstBills: Int = 0,
    val nonGstBills: Int = 0,
    val rateWise: Map<Int, GstRateSummary> = emptyMap()
)

data class GstRateSummary(
    val rate: Int,
    val taxableAmount: Double = 0.0,
    val gstAmount: Double = 0.0,
    val billCount: Int = 0
)

fun calculateGstSummary(bills: List<Bill>): GstSummary {
    var totalTaxable = 0.0
    var cgst = 0.0
    var sgst = 0.0
    var igst = 0.0
    var gstCount = 0
    var nonGstCount = 0
    val rateMap = mutableMapOf<Int, MutableList<Double>>()
    bills.forEach { bill ->
        if ((bill.gstRate ?: 0) > 0) {
            gstCount++
            totalTaxable += bill.taxableAmount
            cgst += bill.totalCgst
            sgst += bill.totalSgst
            igst += bill.totalIgst
            rateMap.getOrPut(bill.gstRate) { mutableListOf() }.add(bill.taxableAmount)
        } else {
            nonGstCount++
        }
    }
    val rateWise = rateMap.mapValues { (rate, amounts) ->
        GstRateSummary(rate, amounts.sum(), amounts.sum() * rate / 100.0, amounts.size)
    }
    return GstSummary(
        totalTaxableSales = totalTaxable,
        totalCgst = cgst,
        totalSgst = sgst,
        totalIgst = igst,
        totalGst = cgst + sgst + igst,
        b2bCount = gstCount,
        b2cCount = nonGstCount,
        gstBills = gstCount,
        nonGstBills = nonGstCount,
        rateWise = rateWise
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GstReturnsScreen(
    bills: List<Bill>,
    onBack: () -> Unit
) {
    val gstSummary = remember(bills) { calculateGstSummary(bills) }
    Scaffold(
        topBar = {
            Surface(
                color = StitchBg,
                modifier = Modifier.fillMaxWidth().height(64.dp).border(0.5.dp, StitchBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = StitchTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Outlined.Assessment, contentDescription = null, tint = StitchPrimaryContainer, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("GST Returns", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = StitchTextPrimary)
                }
            }
        },
        containerColor = StitchBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Summary cards
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GstStatCard(
                    label = "GST Bills",
                    value = "${gstSummary.gstBills}",
                    icon = Icons.Outlined.CheckCircle,
                    gradient = GradientEmerald,
                    modifier = Modifier.weight(1f)
                )
                GstStatCard(
                    label = "Non-GST",
                    value = "${gstSummary.nonGstBills}",
                    icon = Icons.Outlined.Cancel,
                    gradient = GradientOrange,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GstStatCard(
                    label = "Total Taxable",
                    value = FormatUtils.formatCurrency(gstSummary.totalTaxableSales),
                    icon = Icons.AutoMirrored.Outlined.TrendingUp,
                    gradient = GradientIndigo,
                    modifier = Modifier.weight(1f)
                )
                GstStatCard(
                    label = "Total GST",
                    value = FormatUtils.formatCurrency(gstSummary.totalGst),
                    icon = Icons.Outlined.AccountBalance,
                    gradient = GradientWhatsApp,
                    modifier = Modifier.weight(1f)
                )
            }

            if (gstSummary.gstBills > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "GSTR-1 - Sales Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StitchSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        GstReturnRow("B2B (Registered)", gstSummary.b2bCount, gstSummary.totalTaxableSales, StitchPrimaryContainer)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = StitchBorder)
                        GstReturnRow("B2C (Unregistered)", gstSummary.b2cCount, 0.0, StitchTextSecondary)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = StitchBorder)
                        if (gstSummary.totalCgst > 0) {
                            GstRow("CGST Collected", FormatUtils.formatCurrency(gstSummary.totalCgst), AccentBlue)
                            GstRow("SGST Collected", FormatUtils.formatCurrency(gstSummary.totalSgst), AccentBlue)
                        }
                        if (gstSummary.totalIgst > 0) {
                            GstRow("IGST Collected", FormatUtils.formatCurrency(gstSummary.totalIgst), AccentBlue)
                        }
                    }
                }

                // Rate-wise breakdown
                if (gstSummary.rateWise.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Rate-wise Breakdown",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = StitchSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            gstSummary.rateWise.entries.sortedBy { it.key }.forEach { (rate, summary) ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(StitchPrimaryContainer.copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("$rate%", fontWeight = FontWeight.Bold, color = StitchPrimaryContainer, fontSize = 12.sp)
                                            }
                                            Text("${summary.billCount} bills", style = MaterialTheme.typography.bodySmall, color = StitchTextSecondary)
                                        }
                                        Text(
                                            FormatUtils.formatCurrency(summary.taxableAmount),
                                            fontWeight = FontWeight.SemiBold,
                                            color = StitchTextPrimary,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Text(
                                        "GST: ${FormatUtils.formatCurrency(summary.gstAmount)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StitchTextSecondary,
                                        modifier = Modifier.padding(start = 48.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // GSTR-3B section
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "GSTR-3B - Monthly Return",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StitchSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        GstRow("Total Taxable Value", FormatUtils.formatCurrency(gstSummary.totalTaxableSales), StitchTextPrimary)
                        if (gstSummary.totalCgst > 0) {
                            GstRow(" CGST", FormatUtils.formatCurrency(gstSummary.totalCgst), AccentBlue)
                            GstRow(" SGST", FormatUtils.formatCurrency(gstSummary.totalSgst), AccentBlue)
                        }
                        if (gstSummary.totalIgst > 0) {
                            GstRow(" IGST", FormatUtils.formatCurrency(gstSummary.totalIgst), AccentBlue)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = StitchBorder)
                        GstRow("Total GST Liability", FormatUtils.formatCurrency(gstSummary.totalGst), StitchPrimaryContainer)
                    }
                }
            } else {
                // Empty state
                Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Assessment,
                            contentDescription = null,
                            tint = StitchTextSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text("No GST data available", style = MaterialTheme.typography.bodyLarge, color = StitchTextSecondary)
                        Text(
                            "Create bills with GST enabled to see returns",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StitchTextSecondary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun GstStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StitchSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Box(
                modifier = Modifier.size(36.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = StitchTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StitchTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun GstReturnRow(label: String, count: Int, amount: Double, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = StitchTextPrimary)
            Text("$count transactions", style = MaterialTheme.typography.bodySmall, color = StitchTextSecondary)
        }
        Text(FormatUtils.formatCurrency(amount), fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun GstRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = StitchTextSecondary)
        Text(value, fontWeight = FontWeight.SemiBold, color = color)
    }
}
