package com.aistudio.sharmakhata.pqmzvk.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.aistudio.sharmakhata.pqmzvk.R
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.MainViewModel
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.OperationState
import com.aistudio.sharmakhata.pqmzvk.util.FormValidators
import com.aistudio.sharmakhata.pqmzvk.util.PhoneVisualTransformation
import com.aistudio.sharmakhata.pqmzvk.util.SessionManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

// ── Brand Pink Palette ────────────────────────────────────────────────
private val PinkPrimary   = Color(0xFFE91E8C)   // vivid hot pink
private val PinkLight     = Color(0xFFF06292)   // light pink
private val PinkDark      = Color(0xFFC2185B)   // deep pink
private val PinkSurface   = Color(0xFFFCE4EC)   // blush tint
private val PinkOnPrimary = Color.White

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
    val scope = rememberCoroutineScope()

    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isOtpStage by remember { mutableStateOf(false) }
    var storeNotFound by remember { mutableStateOf(false) }
    var loginMode by remember { mutableStateOf("password") }
    var passwordVisible by remember { mutableStateOf(false) }
    var googleLoading by remember { mutableStateOf(false) }

    // Pulsing animation for logo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseScale"
    )

    LaunchedEffect(Unit) {
        SessionManager.load(context)
        viewModel.resetOperationState()
        val storedPhone = SessionManager.phone
        if (!storedPhone.isNullOrBlank()) {
            val digits = storedPhone.filter { it.isDigit() }
            if (digits.length >= 10) phoneNumber = digits.takeLast(10)
        }
    }

    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is OperationState.Success -> {
                when (state.message) {
                    "Logged in" -> onLoggedIn()
                    "Register"  -> onRegisterStore()
                    "Code sent on WhatsApp" -> {
                        isOtpStage = true
                        storeNotFound = false
                        viewModel.resetOperationState()
                    }
                    else -> viewModel.resetOperationState()
                }
            }
            is OperationState.Error -> {
                val message = state.message
                storeNotFound = message.contains("No store found", ignoreCase = true) ||
                        message.contains("not authorized", ignoreCase = true) ||
                        message.contains("No account found", ignoreCase = true)
                val actionLabel = if (storeNotFound) "Register" else null
                val result = snackbarHostState.showSnackbar(
                    message = message, actionLabel = actionLabel, withDismissAction = true
                )
                if (result == SnackbarResult.ActionPerformed && actionLabel == "Register") onRegisterStore()
                viewModel.resetOperationState()
                googleLoading = false
            }
            else -> {}
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFFF0F5)) // blush white background
        ) {
            // ── Decorative background blobs ──────────────────────────────
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(x = (-80).dp, y = (-80).dp)
                    .clip(CircleShape)
                    .background(PinkLight.copy(alpha = 0.25f))
                    .blur(60.dp)
            )
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 60.dp, y = 60.dp)
                    .clip(CircleShape)
                    .background(PinkPrimary.copy(alpha = 0.15f))
                    .blur(50.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.height(48.dp))

                // ── Hero Section ─────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size((80 * pulseScale).dp)
                        .shadow(elevation = 20.dp, shape = RoundedCornerShape(24.dp), ambientColor = PinkPrimary.copy(alpha = 0.4f))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PinkLight, PinkPrimary, PinkDark),
                                start = Offset(0f, 0f), end = Offset(200f, 200f)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Grahbook",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1A0010),
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "Smart billing for Indian businesses",
                    fontSize = 14.sp,
                    color = Color(0xFF9C3060),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(32.dp))

                // ── Login Card ───────────────────────────────────────────
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(24.dp, RoundedCornerShape(24.dp), ambientColor = PinkPrimary.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AnimatedContent(targetState = isOtpStage, label = "loginStage") { inOtp ->
                            if (!inOtp) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        "Welcome back 👋",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A0010)
                                    )
                                    Text(
                                        "Sign in to manage your store",
                                        fontSize = 13.sp,
                                        color = Color(0xFF9C3060)
                                    )

                                    // ── Google Sign-In Button ──────────────────────
                                    OutlinedButton(
                                        onClick = {
                                            googleLoading = true
                                            scope.launch {
                                                try {
                                                    val credentialManager = CredentialManager.create(context)
                                                    val googleIdOption = GetGoogleIdOption.Builder()
                                                        .setFilterByAuthorizedAccounts(false)
                                                        .setServerClientId(context.getString(R.string.default_web_client_id))
                                                        .setAutoSelectEnabled(false)
                                                        .build()
                                                    val request = GetCredentialRequest.Builder()
                                                        .addCredentialOption(googleIdOption)
                                                        .build()
                                                    val result = credentialManager.getCredential(context = context, request = request)
                                                    val credential = result.credential
                                                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                                    val idToken = googleIdTokenCredential.idToken

                                                    // Sign in with Firebase then pass ID token to our backend
                                                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                                                    FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                                                        .addOnSuccessListener { authResult ->
                                                            authResult.user?.getIdToken(true)?.addOnSuccessListener { tokenResult ->
                                                                val firebaseIdToken = tokenResult.token ?: idToken
                                                                viewModel.loginWithGoogle(firebaseIdToken, context)
                                                            }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            googleLoading = false
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("Google Sign-In failed: ${e.message}")
                                                            }
                                                        }
                                                } catch (e: GetCredentialException) {
                                                    googleLoading = false
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Google Sign-In error: ${e.message}")
                                                    }
                                                } catch (e: Exception) {
                                                    googleLoading = false
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Google Sign-In failed: ${e.message}")
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE0E0E0)),
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                                        enabled = !googleLoading && operationState !is OperationState.Loading
                                    ) {
                                        if (googleLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PinkPrimary, strokeWidth = 2.dp)
                                        } else {
                                            // Google "G" logo colored
                                            Text(
                                                "G",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF4285F4),
                                                modifier = Modifier.padding(end = 10.dp)
                                            )
                                            Text(
                                                "Continue with Google",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF3C4043)
                                            )
                                        }
                                    }

                                    // ── Divider ────────────────────────────────────
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Divider(modifier = Modifier.weight(1f), color = Color(0xFFEEE0E8))
                                        Text(
                                            "  or sign in with phone  ",
                                            fontSize = 12.sp,
                                            color = Color(0xFF9C3060).copy(alpha = 0.6f)
                                        )
                                        Divider(modifier = Modifier.weight(1f), color = Color(0xFFEEE0E8))
                                    }

                                    // ── Phone Input ─────────────────────────────────
                                    OutlinedTextField(
                                        value = phoneNumber,
                                        onValueChange = { newVal ->
                                            if (newVal.length <= 10 && newVal.all { it.isDigit() }) {
                                                phoneNumber = newVal; storeNotFound = false
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Mobile Number") },
                                        placeholder = { Text("10-digit number") },
                                        leadingIcon = {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp)) {
                                                Text("+91", fontWeight = FontWeight.Bold, color = PinkPrimary, fontSize = 14.sp)
                                                Spacer(Modifier.width(6.dp))
                                                Box(Modifier.width(1.dp).height(20.dp).background(Color(0xFFEEE0E8)))
                                                Spacer(Modifier.width(8.dp))
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                                        visualTransformation = PhoneVisualTransformation(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        isError = storeNotFound || (phoneNumber.isNotEmpty() && phoneNumber.length < 10),
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = PinkPrimary,
                                            unfocusedIndicatorColor = Color(0xFFEEE0E8),
                                            focusedContainerColor = PinkSurface.copy(alpha = 0.3f),
                                            unfocusedContainerColor = Color(0xFFFAFAFA),
                                            focusedLabelColor = PinkPrimary,
                                            cursorColor = PinkPrimary
                                        )
                                    )

                                    // ── Login Mode Toggle ───────────────────────────
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF5E6ED))
                                            .padding(3.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        listOf("password" to "Password", "otp" to "OTP").forEach { (mode, label) ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (loginMode == mode) Color.White else Color.Transparent)
                                                    .clickable { loginMode = mode }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    label,
                                                    fontWeight = if (loginMode == mode) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (loginMode == mode) PinkPrimary else Color(0xFF9C3060).copy(alpha = 0.5f),
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }

                                    if (loginMode == "password") {
                                        OutlinedTextField(
                                            value = password,
                                            onValueChange = { password = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = { Text("Password") },
                                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = PinkPrimary) },
                                            trailingIcon = {
                                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                    Icon(
                                                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                        null, tint = Color(0xFF9C3060).copy(alpha = 0.5f)
                                                    )
                                                }
                                            },
                                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = TextFieldDefaults.colors(
                                                focusedIndicatorColor = PinkPrimary,
                                                unfocusedIndicatorColor = Color(0xFFEEE0E8),
                                                focusedContainerColor = PinkSurface.copy(alpha = 0.3f),
                                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                                focusedLabelColor = PinkPrimary,
                                                cursorColor = PinkPrimary
                                            )
                                        )

                                        Button(
                                            onClick = {
                                                storeNotFound = false
                                                viewModel.loginWithPassword("+91$phoneNumber", password, context)
                                            },
                                            modifier = Modifier.fillMaxWidth().height(52.dp),
                                            enabled = phoneNumber.length == 10 && password.length >= 4 && operationState !is OperationState.Loading,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PinkPrimary,
                                                disabledContainerColor = PinkLight.copy(alpha = 0.4f)
                                            )
                                        ) {
                                            if (operationState is OperationState.Loading && !googleLoading) {
                                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                                            } else {
                                                Icon(Icons.Default.Login, null, modifier = Modifier.size(18.dp), tint = Color.White)
                                                Spacer(Modifier.width(8.dp))
                                                Text("Sign In", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                storeNotFound = false
                                                viewModel.requestLoginCode("+91$phoneNumber")
                                            },
                                            modifier = Modifier.fillMaxWidth().height(52.dp),
                                            enabled = phoneNumber.length == 10 && operationState !is OperationState.Loading,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PinkPrimary,
                                                disabledContainerColor = PinkLight.copy(alpha = 0.4f)
                                            )
                                        ) {
                                            if (operationState is OperationState.Loading) {
                                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                                            } else {
                                                Text("Send OTP via WhatsApp", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                            }
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("New to Grahbook?", color = Color(0xFF9C3060).copy(alpha = 0.7f), fontSize = 13.sp)
                                        TextButton(onClick = onRegisterStore) {
                                            Text("Register Store", color = PinkPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            } else {
                                // ── OTP Verification Stage ─────────────────────────
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Icon(Icons.Default.Shield, null, tint = PinkPrimary, modifier = Modifier.size(40.dp))
                                    Text("Verify OTP", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A0010))
                                    Text(
                                        "Enter the 6-digit code sent to +91 $phoneNumber",
                                        fontSize = 13.sp, color = Color(0xFF9C3060), textAlign = TextAlign.Center
                                    )

                                    OutlinedTextField(
                                        value = otp,
                                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otp = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("OTP Code") },
                                        placeholder = { Text("——————") },
                                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = PinkPrimary) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center, letterSpacing = 8.sp),
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = PinkPrimary,
                                            unfocusedIndicatorColor = Color(0xFFEEE0E8),
                                            focusedContainerColor = PinkSurface.copy(alpha = 0.3f),
                                            unfocusedContainerColor = Color(0xFFFAFAFA),
                                            focusedLabelColor = PinkPrimary,
                                            cursorColor = PinkPrimary
                                        )
                                    )

                                    Button(
                                        onClick = { viewModel.verifyLoginCode("+91$phoneNumber", otp, context) },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        enabled = otp.length == 6 && operationState !is OperationState.Loading,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                                    ) {
                                        if (operationState is OperationState.Loading) {
                                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(18.dp), tint = Color.White)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Verify & Sign In", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Didn't receive it?", color = Color(0xFF9C3060).copy(alpha = 0.6f), fontSize = 13.sp)
                                        TextButton(onClick = {
                                            isOtpStage = false; otp = ""
                                            viewModel.requestLoginCode("+91$phoneNumber")
                                        }) {
                                            Text("Resend OTP", color = PinkPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
                Text(
                    "Secured by Grahbook · v2.0",
                    fontSize = 11.sp,
                    color = Color(0xFF9C3060).copy(alpha = 0.4f)
                )
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}
