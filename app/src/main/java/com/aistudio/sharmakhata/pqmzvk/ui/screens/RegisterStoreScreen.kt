package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
fun RegisterStoreScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val operationState by viewModel.operationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var businessName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isExistingStore by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        SessionManager.load(context)
        val storedPhone = SessionManager.phone
        if (!storedPhone.isNullOrBlank()) {
            val digits = storedPhone.filter { it.isDigit() }
            if (digits.length >= 10) {
                phone = digits.takeLast(10)
            }
        }
    }

    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is OperationState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                onSuccess()
            }
            is OperationState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                isExistingStore = state.message.contains("already exists", ignoreCase = true) ||
                                  state.message.contains("already registered", ignoreCase = true)
                viewModel.resetOperationState()
            }
            else -> {}
        }
    }

    val isValid = businessName.isNotBlank() && ownerName.isNotBlank() && phone.length >= 10 &&
                  password.length >= 4 && password == confirmPassword

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.register_store_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StitchBg,
                    titleContentColor = StitchTextPrimary,
                    navigationIconContentColor = StitchTextPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(StitchBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Logo
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(StitchSurfaceHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(StitchPrimaryContainer.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.app_logo_text),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchPrimaryContainer
                        )
                    }
                }

                Text(
                    text = if (isExistingStore) stringResource(R.string.store_already_registered) else stringResource(R.string.setup_your_business),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary
                )

                Text(
                    text = if (isExistingStore)
                        stringResource(R.string.store_already_registered_desc)
                    else
                        stringResource(R.string.setup_business_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = StitchTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Form Card
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
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.business_name_label)) },
                            placeholder = { Text(stringResource(R.string.business_name_placeholder)) },
                            leadingIcon = { Icon(Icons.Outlined.Store, contentDescription = null, tint = StitchPrimaryContainer) },
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

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.owner_name_label)) },
                            placeholder = { Text(stringResource(R.string.owner_name_placeholder)) },
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = StitchPrimaryContainer) },
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

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { newVal ->
                                if (newVal.length <= 10 && newVal.all { it.isDigit() }) {
                                    phone = newVal
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.phone_number_star_label)) },
                            placeholder = { Text(stringResource(R.string.phone_placeholder)) },
                            leadingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(R.string.country_code), fontWeight = FontWeight.Bold, color = StitchPrimaryContainer, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    HorizontalDivider(modifier = Modifier.width(1.dp).height(20.dp), color = StitchBorder)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Outlined.Phone, contentDescription = null, tint = StitchPrimaryContainer, modifier = Modifier.size(18.dp))
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
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

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.password_star_label)) },
                            placeholder = { Text(stringResource(R.string.password_min_char)) },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = StitchPrimaryContainer) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = StitchTextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.confirm_password_label)) },
                            placeholder = { Text(stringResource(R.string.confirm_password_placeholder)) },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = StitchPrimaryContainer) },
                            isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                            supportingText = {
                                if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                                    Text(stringResource(R.string.passwords_do_not_match), color = MaterialTheme.colorScheme.error)
                                }
                            },
                            visualTransformation = PasswordVisualTransformation(),
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

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.email_optional_label)) },
                            placeholder = { Text(stringResource(R.string.email_placeholder)) },
                            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = StitchPrimaryContainer) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
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

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.address_optional_label)) },
                            placeholder = { Text(stringResource(R.string.address_placeholder)) },
                            leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = StitchPrimaryContainer) },
                            minLines = 2,
                            maxLines = 3,
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

                        OutlinedTextField(
                            value = gstin,
                            onValueChange = { newVal ->
                                if (newVal.length <= 15 && newVal.all { it.isLetterOrDigit() }) {
                                    gstin = newVal.uppercase()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.gstin_optional_label)) },
                            placeholder = { Text(stringResource(R.string.gstin_placeholder)) },
                            leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null, tint = StitchPrimaryContainer) },
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

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                isExistingStore = false
                                viewModel.registerStore(
                                    storeName = businessName,
                                    ownerName = ownerName,
                                    phone = "+91$phone",
                                    email = email.ifBlank { "" },
                                    address = address.ifBlank { null },
                                    gstin = gstin.ifBlank { null },
                                    password = password,
                                    context = context
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = isValid && operationState !is OperationState.Loading,
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
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.register_continue), fontWeight = FontWeight.Bold, color = StitchOnPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
