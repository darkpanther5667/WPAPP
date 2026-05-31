package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.aistudio.sharmakhata.pqmzvk.R
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.MainViewModel
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.OperationState
import com.aistudio.sharmakhata.pqmzvk.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoggedIn: () -> Unit,
    onRegisterStore: () -> Unit = {}
) {
    val operationState by viewModel.operationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isOtpStage by remember { mutableStateOf(false) }
    var storeNotFound by remember { mutableStateOf(false) }
    var loginMode by remember { mutableStateOf("password") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        SessionManager.load(context)
        viewModel.resetOperationState()
        if (phoneNumber.isEmpty()) {
            val storedPhone = SessionManager.phone
            if (!storedPhone.isNullOrBlank()) {
                val digits = storedPhone.filter { it.isDigit() }
                if (digits.length >= 10) {
                    phoneNumber = digits.takeLast(10)
                }
            }
        }
    }

    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is OperationState.Success -> {
                when (state.message) {
                    "Logged in" -> onLoggedIn()
                    "Code sent on WhatsApp" -> {
                        isOtpStage = true
                        storeNotFound = false
                        viewModel.resetOperationState()
                    }
                    else -> {
                        if (!isOtpStage && phoneNumber.length == 10 && loginMode == "otp") {
                            isOtpStage = true
                        }
                        viewModel.resetOperationState()
                    }
                }
            }
            is OperationState.Error -> {
                val message = state.message
                storeNotFound = message.contains("No store found", ignoreCase = true) ||
                        message.contains("not authorized", ignoreCase = true) ||
                        message.contains("No account found", ignoreCase = true)

                val actionLabel = when {
                    message.contains("internet", ignoreCase = true) ||
                    message.contains("timeout", ignoreCase = true) ||
                    message.contains("connect", ignoreCase = true) -> "Retry"
                    storeNotFound -> "Register"
                    else -> null
                }

                val result = snackbarHostState.showSnackbar(
                    message = state.message,
                    actionLabel = actionLabel,
                    withDismissAction = true
                )
                when {
                    result == SnackbarResult.ActionPerformed && actionLabel == "Retry" && !isOtpStage -> {
                        viewModel.requestLoginCode("+91$phoneNumber")
                    }
                    result == SnackbarResult.ActionPerformed && actionLabel == "Register" -> {
                        onRegisterStore()
                    }
                }
                viewModel.resetOperationState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(StitchBg) // Solid dark background — no gradient
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ── Logo ──
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(StitchSurfaceHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(StitchPrimaryContainer.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.app_logo_text),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.app_name_display),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = StitchTextSecondary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── Login Card ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StitchSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(StitchBorder)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (!isOtpStage) {
                            // ── Phone Input Stage ──
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = StitchPrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                stringResource(R.string.welcome_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = StitchTextPrimary
                            )
                            Text(
                                stringResource(R.string.enter_phone_instruction),
                                style = MaterialTheme.typography.bodySmall,
                                color = StitchTextSecondary,
                                textAlign = TextAlign.Center
                            )

                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { newVal ->
                                    if (newVal.length <= 10 && newVal.all { it.isDigit() }) {
                                        phoneNumber = newVal
                                        storeNotFound = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.phone_number_label)) },
                                placeholder = { Text(stringResource(R.string.phone_placeholder)) },
                                supportingText = {
                                    when {
                                        storeNotFound -> Text(
                                            stringResource(R.string.no_account_found),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        phoneNumber.isNotEmpty() && phoneNumber.length < 10 -> Text(
                                            stringResource(R.string.enter_10_digit),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        else -> Text(stringResource(R.string.enter_indian_mobile))
                                    }
                                },
                                leadingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(stringResource(R.string.country_code), fontWeight = FontWeight.Bold, color = StitchPrimaryContainer)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(StitchBorder))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = StitchPrimaryContainer, modifier = Modifier.size(18.dp))
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = StitchTextPrimary,
                                    unfocusedTextColor = StitchTextPrimary,
                                    focusedIndicatorColor = if (phoneNumber.length == 10) StitchPrimaryContainer else MaterialTheme.colorScheme.error,
                                    unfocusedIndicatorColor = StitchBorder,
                                    focusedContainerColor = StitchSurfaceLow,
                                    unfocusedContainerColor = StitchSurfaceLow,
                                    focusedLabelColor = if (phoneNumber.length == 10) StitchPrimaryContainer else MaterialTheme.colorScheme.error,
                                    unfocusedLabelColor = StitchTextSecondary,
                                    cursorColor = StitchPrimaryContainer,
                                    focusedSupportingTextColor = StitchTextSecondary,
                                    unfocusedSupportingTextColor = StitchTextSecondary
                                ),
                                isError = (phoneNumber.isNotEmpty() && phoneNumber.length < 10) || storeNotFound
                            )

                            // ── Login Mode Toggle ──
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(StitchSurfaceLow, RoundedCornerShape(10.dp))
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FilterChip(
                                    selected = loginMode == "password",
                                    onClick = { loginMode = "password" },
                                    label = { Text(stringResource(R.string.password_label), fontWeight = if (loginMode == "password") FontWeight.Bold else FontWeight.Normal) },
                                    leadingIcon = {
                                        if (loginMode == "password") {
                                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = StitchPrimaryContainer)
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StitchPrimaryContainer.copy(alpha = 0.15f),
                                        selectedLabelColor = StitchPrimaryContainer,
                                        containerColor = Color.Transparent,
                                        labelColor = StitchTextSecondary
                                    )
                                )
                                FilterChip(
                                    selected = loginMode == "otp",
                                    onClick = { loginMode = "otp" },
                                    label = { Text(stringResource(R.string.otp_mode), fontWeight = if (loginMode == "otp") FontWeight.Bold else FontWeight.Normal) },
                                    leadingIcon = {
                                        if (loginMode == "otp") {
                                            Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp), tint = StitchPrimaryContainer)
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StitchPrimaryContainer.copy(alpha = 0.15f),
                                        selectedLabelColor = StitchPrimaryContainer,
                                        containerColor = Color.Transparent,
                                        labelColor = StitchTextSecondary
                                    )
                                )
                            }

                            if (loginMode == "password") {
                                // ── Password Login ──
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(stringResource(R.string.password_label)) },
                                    placeholder = { Text(stringResource(R.string.enter_password_placeholder)) },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = StitchPrimaryContainer) },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (passwordVisible) stringResource(R.string.hide_password_desc) else stringResource(R.string.show_password_desc),
                                                tint = StitchTextSecondary
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = StitchTextPrimary,
                                        unfocusedTextColor = StitchTextPrimary,
                                        focusedIndicatorColor = StitchPrimaryContainer,
                                        unfocusedIndicatorColor = StitchBorder,
                                        focusedContainerColor = StitchSurfaceLow,
                                        unfocusedContainerColor = StitchSurfaceLow,
                                        focusedLabelColor = StitchPrimaryContainer,
                                        unfocusedLabelColor = StitchTextSecondary,
                                        cursorColor = StitchPrimaryContainer
                                    )
                                )

                                Button(
                                    onClick = {
                                        storeNotFound = false
                                        viewModel.loginWithPassword("+91$phoneNumber", password, context)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    enabled = phoneNumber.length == 10 && password.length >= 4 && operationState !is OperationState.Loading,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StitchPrimaryContainer,
                                        disabledContainerColor = StitchSurfaceHighest
                                    )
                                ) {
                                    if (operationState is OperationState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = StitchOnPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.login_button), fontWeight = FontWeight.Bold, color = StitchOnPrimary)
                                    }
                                }
                            } else {
                                // ── OTP Login ──
                                Button(
                                    onClick = {
                                        storeNotFound = false
                                        viewModel.requestLoginCode("+91$phoneNumber")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    enabled = phoneNumber.length == 10 && operationState !is OperationState.Loading,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StitchPrimaryContainer,
                                        disabledContainerColor = StitchSurfaceHighest
                                    )
                                ) {
                                    if (operationState is OperationState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = StitchOnPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(stringResource(R.string.send_otp), fontWeight = FontWeight.Bold, color = StitchOnPrimary)
                                    }
                                }
                            }

                            // Register link
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.new_here), color = StitchTextSecondary, fontSize = 13.sp)
                                TextButton(onClick = onRegisterStore) {
                                    Text(stringResource(R.string.register_your_store), color = StitchPrimaryContainer, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else {
                            // ── OTP Stage ──
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = StitchPrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                stringResource(R.string.verify_otp_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = StitchTextPrimary
                            )
                            Text(
                                stringResource(R.string.enter_otp_instruction, phoneNumber),
                                style = MaterialTheme.typography.bodySmall,
                                color = StitchTextSecondary,
                                textAlign = TextAlign.Center
                            )

                            OutlinedTextField(
                                value = otp,
                                onValueChange = { newVal ->
                                    if (newVal.length <= 6 && newVal.all { it.isDigit() }) {
                                        otp = newVal
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.otp_label)) },
                                placeholder = { Text(stringResource(R.string.otp_placeholder)) },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = StitchPrimaryContainer)
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = StitchTextPrimary,
                                    unfocusedTextColor = StitchTextPrimary,
                                    focusedIndicatorColor = StitchPrimaryContainer,
                                    unfocusedIndicatorColor = StitchBorder,
                                    focusedContainerColor = StitchSurfaceLow,
                                    unfocusedContainerColor = StitchSurfaceLow,
                                    focusedLabelColor = StitchPrimaryContainer,
                                    unfocusedLabelColor = StitchTextSecondary,
                                    cursorColor = StitchPrimaryContainer
                                ),
                                textStyle = MaterialTheme.typography.headlineSmall.copy(
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 8.sp
                                )
                            )

                            Button(
                                onClick = {
                                    viewModel.verifyLoginCode("+91$phoneNumber", otp, context)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                enabled = otp.length == 6 && operationState !is OperationState.Loading,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StitchPrimaryContainer,
                                    disabledContainerColor = StitchSurfaceHighest
                                )
                            ) {
                                if (operationState is OperationState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = StitchOnPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(stringResource(R.string.verify_and_login), fontWeight = FontWeight.Bold, color = StitchOnPrimary)
                                }
                            }

                            TextButton(onClick = {
                                isOtpStage = false
                                otp = ""
                                viewModel.requestLoginCode("+91$phoneNumber")
                            }) {
                                Text(stringResource(R.string.resend_otp), color = StitchPrimaryContainer, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
