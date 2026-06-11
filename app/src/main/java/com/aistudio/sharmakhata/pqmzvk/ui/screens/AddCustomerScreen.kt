package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.CustomerViewModel
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.OperationState
import com.aistudio.sharmakhata.pqmzvk.util.PhoneVisualTransformation
import androidx.compose.ui.res.stringResource
import com.aistudio.sharmakhata.pqmzvk.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(
    viewModel: CustomerViewModel,
    onBack: () -> Unit,
) {
    val operationState by viewModel.operationState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val isValid = name.trim().length >= 2 && phone.trim().length >= 8

    LaunchedEffect(operationState) {
        when (operationState) {
            is OperationState.Success -> {
                snackbarHostState.showSnackbar((operationState as OperationState.Success).message)
                viewModel.resetOperationState()
                onBack()
            }
            is OperationState.Error -> {
                snackbarHostState.showSnackbar((operationState as OperationState.Error).message)
                viewModel.resetOperationState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_customer_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.large)
        ) {
            // Customer icon header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.flat),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xlarge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    Box(
                        modifier = Modifier
                            .size(ComponentSize.avatarLarge + 16.dp)
                            .background(
                                color = IndigoContainer,
                                shape = AvatarShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(IconSize.large)
                        )
                    }
                    Text(
                        text = stringResource(R.string.new_customer),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.customer_name_label)) },
                placeholder = { Text(stringResource(R.string.enter_full_name), color = TextTertiaryLight) },
                leadingIcon = {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = IndigoPrimary)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = TextFieldShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = IndigoPrimary,
                    unfocusedIndicatorColor = CardBorder,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = TextPrimaryLight,
                    unfocusedTextColor = TextPrimaryLight,
                    focusedLabelColor = IndigoPrimary,
                    unfocusedLabelColor = StitchTextSecondary,
                    cursorColor = IndigoPrimary
                ),
                singleLine = true
            )

            // Phone field
            OutlinedTextField(
                value = phone,
                onValueChange = { newVal ->
                    val digits = newVal.filter { it.isDigit() }.take(10)
                    phone = digits
                },
                label = { Text(stringResource(R.string.whatsapp_number_label)) },
                placeholder = { Text("98765 43210", color = TextTertiaryLight) },
                leadingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("+91", fontWeight = FontWeight.Bold, color = IndigoPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.width(1.dp).height(20.dp).color(CardBorder))
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Outlined.Phone, contentDescription = null, tint = IndigoPrimary)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                visualTransformation = PhoneVisualTransformation(),
                isError = phone.isNotEmpty() && phone.length < 10,
                supportingText = if (phone.isNotEmpty() && phone.length < 10) {
                    { Text("Enter 10 digit number", color = DebtRed) }
                } else null,
                shape = TextFieldShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = if (phone.length == 10) IndigoPrimary else DebtRed,
                    unfocusedIndicatorColor = CardBorder,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = TextPrimaryLight,
                    unfocusedTextColor = TextPrimaryLight,
                    focusedLabelColor = IndigoPrimary,
                    unfocusedLabelColor = StitchTextSecondary,
                    cursorColor = IndigoPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            // Save button
            Button(
                onClick = { viewModel.addCustomer(name.trim(), phone.trim(), context) },
                enabled = isValid && operationState !is OperationState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ComponentSize.buttonHeight),
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IndigoPrimary,
                    contentColor = Color.White
                )
            ) {
                if (operationState is OperationState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(IconSize.small),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(IconSize.small))
                }
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(stringResource(R.string.save_customer), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
