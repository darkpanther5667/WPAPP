package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoggedIn: () -> Unit,
    onRegisterStore: () -> Unit,
) {
    val operationState by viewModel.operationState.collectAsState()
    val authToken by viewModel.authToken.collectAsState()

    var storeId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var stage by remember { mutableStateOf(1) } // 1=request, 2=verify

    val scroll = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Logo animation
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(authToken) {
        val token = viewModel.consumeAuthToken()
        if (!token.isNullOrBlank()) {
            SessionManager.setToken(context, token)
            onLoggedIn()
        }
    }

    LaunchedEffect(operationState) {
        val msg = (operationState as? OperationState.Success)?.message ?: return@LaunchedEffect
        if (msg.contains("Code sent")) stage = 2
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Grahbook", fontWeight = FontWeight.ExtraBold, color = Color(0xFF25d366))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF191c1e)
                ),
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = Color(0xFFe0e3e5),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFF25d366).copy(alpha = 0.03f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scroll)
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // Logo with animation
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .scale(scale)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF25d366), Color(0xFF128c7e))
                            )
                        )
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Storefront,
                        contentDescription = "Grahbook Logo",
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }

                // Welcome text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Welcome Back!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191c1e)
                    )
                    Text(
                        text = "Login to manage your business",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3c4a3d)
                    )
                }

                // Instructions card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF25d366).copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF25d366).copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color(0xFF25d366),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Enter your Store ID and WhatsApp number. We'll send a login code on WhatsApp.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF191c1e),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Input fields
                OutlinedTextField(
                    value = storeId,
                    onValueChange = { storeId = it.trim() },
                    label = { Text("Store ID") },
                    placeholder = { Text("Enter your store ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF25d366),
                        unfocusedBorderColor = Color(0xFFe0e3e5),
                        focusedLabelColor = Color(0xFF25d366)
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Store, contentDescription = null, tint = Color(0xFF25d366))
                    }
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.filter { ch -> ch.isDigit() || ch == '+' } },
                    label = { Text("WhatsApp number") },
                    placeholder = { Text("Enter WhatsApp number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF25d366),
                        unfocusedBorderColor = Color(0xFFe0e3e5),
                        focusedLabelColor = Color(0xFF25d366)
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF25d366))
                    }
                )

                if (stage == 2) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.filter { ch -> ch.isDigit() }.take(6) },
                        label = { Text("6-digit code") },
                        placeholder = { Text("Enter verification code") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF25d366),
                            unfocusedBorderColor = Color(0xFFe0e3e5),
                            focusedLabelColor = Color(0xFF25d366)
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF25d366))
                        }
                    )
                }

                // Main action button
                Button(
                    onClick = {
                        if (stage == 1) {
                            viewModel.requestLoginCode(storeId, phone)
                        } else {
                            viewModel.verifyLoginCode(storeId, phone, code, context)
                        }
                    },
                    enabled = operationState !is OperationState.Loading &&
                        storeId.isNotBlank() &&
                        phone.length >= 8 &&
                        (stage == 1 || code.length == 6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25d366)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (operationState is OperationState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(24.dp)
                                .height(24.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Icon(
                        if (stage == 1) Icons.AutoMirrored.Filled.Message else Icons.Default.Lock,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (stage == 1) "Send Code" else "Verify & Login", fontWeight = FontWeight.Bold)
                }

                if (stage == 2) {
                    TextButton(
                        onClick = { stage = 1; code = "" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change number / resend", color = Color(0xFF25d366))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Divider(color = Color(0xFFe0e3e5))
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onRegisterStore,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Register new store", color = Color(0xFF3c4a3d), fontWeight = FontWeight.Medium)
                }

                // Version info
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF3c4a3d),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }

            when (operationState) {
                is OperationState.Success -> {
                    val msg = (operationState as OperationState.Success).message
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        action = {
                            TextButton(onClick = { viewModel.resetOperationState() }) { 
                                Text("OK", color = Color.White) 
                            }
                        },
                        containerColor = Color(0xFF25d366),
                        contentColor = Color.White
                    ) { Text(msg) }
                }
                is OperationState.Error -> {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        action = {
                            TextButton(onClick = { viewModel.resetOperationState() }) { 
                                Text("Dismiss", color = Color.White) 
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    ) { Text((operationState as OperationState.Error).message) }
                }
                else -> Unit
            }
        }
    }
}