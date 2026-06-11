package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*

enum class InvoiceTemplate(val displayName: String, val description: String) {
    MODERN("Modern", "Clean and minimal with green accents"),
    CLASSIC("Classic", "Traditional ledger-style format"),
    PROFESSIONAL("Professional", "Formal design with company header"),
    MINIMAL("Minimal", "Simple text-only layout for thermal printing")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceTemplateScreen(
    currentTemplate: InvoiceTemplate = InvoiceTemplate.MODERN,
    onTemplateSelected: (InvoiceTemplate) -> Unit,
    onBack: () -> Unit
) {
    var selectedTemplate by remember { mutableStateOf(currentTemplate) }
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
                    Icon(Icons.Outlined.Description, contentDescription = null, tint = StitchPrimaryContainer, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Invoice Templates", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = StitchTextPrimary)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Choose a template style for your invoices. This will be used when generating PDFs and printouts.",
                style = MaterialTheme.typography.bodyMedium,
                color = StitchTextSecondary
            )
            InvoiceTemplate.entries.forEach { template ->
                val isSelected = selectedTemplate == template
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { selectedTemplate = template },
                    shape = CardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) StitchPrimaryContainer.copy(alpha = 0.1f) else StitchSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp),
                    border = if (isSelected) {
                        CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(StitchPrimaryContainer.copy(alpha = 0.5f))
                        )
                    } else null
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(StitchPrimaryContainer.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (template) {
                                    InvoiceTemplate.MODERN -> Icons.Outlined.Description
                                    InvoiceTemplate.CLASSIC -> Icons.Outlined.Receipt
                                    InvoiceTemplate.PROFESSIONAL -> Icons.Outlined.Business
                                    InvoiceTemplate.MINIMAL -> Icons.AutoMirrored.Outlined.TextSnippet
                                },
                                contentDescription = null,
                                tint = StitchPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    template.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) StitchPrimaryContainer else StitchTextPrimary
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StitchPrimaryContainer, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(template.description, style = MaterialTheme.typography.bodySmall, color = StitchTextSecondary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onTemplateSelected(selectedTemplate) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = selectedTemplate != currentTemplate,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StitchPrimaryContainer,
                    contentColor = StitchOnPrimaryContainer,
                    disabledContainerColor = StitchPrimaryContainer.copy(alpha = 0.38f),
                    disabledContentColor = StitchOnPrimaryContainer.copy(alpha = 0.38f)
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply Template", fontWeight = FontWeight.Bold)
            }
        }
    }
}
