package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.sharmakhata.pqmzvk.data.model.Staff
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.OperationState
import com.aistudio.sharmakhata.pqmzvk.util.FormValidators
import com.aistudio.sharmakhata.pqmzvk.util.PhoneVisualTransformation
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffScreen(
    staffList: List<Staff>,
    currentUserPhone: String,
    operationState: OperationState = OperationState.Idle,
    onBack: () -> Unit,
    onAddStaff: (name: String, phone: String, role: String) -> Unit,
    onRemoveStaff: (id: String) -> Unit,
    onResetOperation: () -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var staffToDelete by remember { mutableStateOf<Staff?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(operationState) {
        when (operationState) {
            is OperationState.Success -> {
                snackbarHostState.showSnackbar(operationState.message)
                onResetOperation()
            }
            is OperationState.Error -> {
                snackbarHostState.showSnackbar(operationState.message)
                onResetOperation()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = StitchBg,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .border(width = 0.5.dp, color = StitchBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = StitchTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Outlined.SupervisorAccount,
                        contentDescription = null,
                        tint = StitchPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Staff Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextPrimary
                    )
                }
            }
        },
        containerColor = StitchBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = StitchPrimaryContainer,
                contentColor = StitchOnPrimaryContainer,
                shape = FabShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Staff", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Hero header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(CardShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                StitchPrimaryContainer.copy(alpha = 0.2f),
                                StitchBg
                            ),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY)
                        )
                    )
                    .border(0.5.dp, StitchPrimaryContainer.copy(alpha = 0.15f), CardShape)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(StitchPrimaryContainer.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Groups,
                            contentDescription = null,
                            tint = StitchPrimaryContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "${staffList.size} Staff Members",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = StitchTextPrimary
                        )
                        Text(
                            text = "Manage who can access your store via WhatsApp bot and app",
                            style = MaterialTheme.typography.labelSmall,
                            color = StitchTextSecondary
                        )
                    }
                }
            }

            if (staffList.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(StitchPrimaryContainer.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.PersonAdd,
                                contentDescription = null,
                                tint = StitchPrimaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Text(
                            text = "No Staff Members",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = StitchTextPrimary
                        )
                        Text(
                            text = "Add staff to allow them access to your store",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StitchTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(staffList, key = { it.id }) { staff ->
                        StaffCard(
                            staff = staff,
                            isCurrentUser = normalizeForCompare(staff.phone) == normalizeForCompare(currentUserPhone),
                            onDelete = { staffToDelete = staff }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Add Staff Dialog
    if (showAddDialog) {
        AddStaffDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, phone, role ->
                onAddStaff(name, phone, role)
                showAddDialog = false
            }
        )
    }

    // Delete Confirmation
    staffToDelete?.let { staff ->
        AlertDialog(
            onDismissRequest = { staffToDelete = null },
            title = {
                Text("Remove Staff", fontWeight = FontWeight.Bold, color = StitchTextPrimary)
            },
            text = {
                Text(
                    "Are you sure you want to remove \"${staff.name}\" from your store? They will lose access to the WhatsApp bot and app.",
                    color = StitchTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveStaff(staff.id)
                        staffToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DebtRed,
                        contentColor = Color.White
                    ),
                    shape = ButtonShape
                ) {
                    Text("Remove", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { staffToDelete = null }) {
                    Text("Cancel", color = StitchTextSecondary)
                }
            },
            containerColor = StitchSurface,
            shape = CardShape
        )
    }
}

@Composable
private fun StaffCard(
    staff: Staff,
    isCurrentUser: Boolean,
    onDelete: () -> Unit
) {
    val avatarColors = AvatarColors.map { it.first() }
    val avatarColor = avatarColors[abs(staff.name.hashCode()) % avatarColors.size]
    val initials = staff.name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = StitchSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials.ifEmpty { "?" },
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = staff.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCurrentUser) {
                        Box(
                            modifier = Modifier
                                .clip(BadgeShape)
                                .background(StitchPrimaryContainer.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "You",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StitchPrimaryContainer,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = StitchTextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = formatPhone(staff.phone),
                        style = MaterialTheme.typography.labelSmall,
                        color = StitchTextSecondary
                    )
                }
            }

            if (!isCurrentUser) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.PersonRemove,
                        contentDescription = "Remove",
                        tint = DebtRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddStaffDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String, role: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("staff") }
    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Outlined.PersonAdd,
                    contentDescription = null,
                    tint = StitchPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
                Text("Add Staff Member", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; if (it.isNotBlank()) nameError = false },
                    label = { Text("Name") },
                    placeholder = { Text("e.g. Raju Storekeeper") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = StitchPrimaryContainer) },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("Name is required", color = DebtRed) } } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = TextFieldShape,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StitchPrimaryContainer,
                        unfocusedBorderColor = StitchBorder,
                        focusedContainerColor = StitchSurfaceLow,
                        unfocusedContainerColor = StitchSurfaceLow,
                        focusedTextColor = StitchTextPrimary,
                        unfocusedTextColor = StitchTextPrimary,
                        cursorColor = StitchPrimaryContainer,
                        focusedLabelColor = StitchPrimaryContainer,
                        unfocusedLabelColor = StitchTextSecondary
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { newVal ->
                        phone = newVal.filter { it.isDigit() }.take(10)
                        if (phone.isNotBlank()) phoneError = false
                    },
                    label = { Text("Phone Number") },
                    placeholder = { Text("98765 43210") },
                    leadingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("+91", fontWeight = FontWeight.Bold, color = StitchPrimaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(modifier = Modifier.width(1.dp).height(20.dp).color(StitchBorder))
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Outlined.Phone, contentDescription = null, tint = StitchPrimaryContainer)
                        }
                    },
                    isError = phoneError || (phone.isNotEmpty() && !FormValidators.isValidPhone(phone)),
                    supportingText = when {
                        phoneError -> {{ Text("Valid phone is required", color = DebtRed) }}
                        phone.isNotEmpty() && !FormValidators.isValidPhone(phone) -> {{ Text("Enter valid 10-digit Indian number", color = DebtRed) }}
                        else -> null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = TextFieldShape,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    visualTransformation = PhoneVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StitchPrimaryContainer,
                        unfocusedBorderColor = StitchBorder,
                        focusedContainerColor = StitchSurfaceLow,
                        unfocusedContainerColor = StitchSurfaceLow,
                        focusedTextColor = StitchTextPrimary,
                        unfocusedTextColor = StitchTextPrimary,
                        cursorColor = StitchPrimaryContainer,
                        focusedLabelColor = StitchPrimaryContainer,
                        unfocusedLabelColor = StitchTextSecondary
                    )
                )

                // Role selector
                Text(
                    text = "Role",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = StitchPrimaryContainer
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("staff" to "Staff", "admin" to "Admin").forEach { (value, label) ->
                        FilterChip(
                            selected = role == value,
                            onClick = { role = value },
                            label = { Text(label, fontWeight = if (role == value) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StitchPrimaryContainer,
                                selectedLabelColor = StitchOnPrimaryContainer
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    nameError = name.isBlank()
                    phoneError = phone.isBlank() || phone.replace(Regex("[^0-9]"), "").length < 10
                    if (!nameError && !phoneError) {
                        onAdd(name.trim(), phone.trim(), role)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StitchPrimaryContainer,
                    contentColor = StitchOnPrimaryContainer
                ),
                shape = ButtonShape
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = StitchTextSecondary)
            }
        },
        containerColor = StitchSurface,
        shape = CardShape
    )
}

private fun formatPhone(phone: String): String {
    val digits = phone.replace(Regex("[^0-9]"), "")
    return when {
        digits.length >= 12 && digits.startsWith("91") -> "+91 ${digits.substring(2)}"
        digits.length == 10 -> "+91 $digits"
        else -> phone
    }
}

private fun normalizeForCompare(phone: String): String {
    val digits = phone.replace(Regex("[^0-9]"), "")
    return if (digits.startsWith("91") && digits.length == 12) digits.substring(2) else digits
}
