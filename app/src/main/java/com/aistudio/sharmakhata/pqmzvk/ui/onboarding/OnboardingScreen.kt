package com.aistudio.sharmakhata.pqmzvk.ui.onboarding

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.sharmakhata.pqmzvk.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.aistudio.sharmakhata.pqmzvk.R
import kotlinx.coroutines.delay

// Brand gold accent for onboarding (no theme equivalent)
private val OnboardingGold = Color(0xFFFFD54F)

data class OnboardingState(
    val shopName: String = "",
    val category: String = "",
    val language: String = "Hinglish"
)

@Composable
fun OnboardingScreen(
    shopName: String = "",
    onComplete: (OnboardingState) -> Unit,
    onSkip: () -> Unit = {}
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val state = remember { mutableStateOf(OnboardingState(shopName = shopName)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Brand800, Saffron500),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top bar with skip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    TextButton(onClick = { currentStep-- }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.back), color = Color.White, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                if (currentStep < 3) {
                    TextButton(onClick = onSkip) {
                        Text(stringResource(R.string.skip_label), color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Segmented progress bar
            SegmentedProgressBar(
                totalSteps = 4,
                currentStep = currentStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Step content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                when (currentStep) {
                    0 -> WelcomeStep()
                    1 -> WhatsAppBotStep()
                    2 -> ShopSetupStep(state)
                    3 -> ReadyStep(state, onComplete = { onComplete(state.value) })
                }
            }

            // Bottom navigation
            if (currentStep < 3) {
                val canProceed = when (currentStep) {
                    2 -> state.value.shopName.isNotBlank()
                    else -> true
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Button(
                        onClick = { if (canProceed) currentStep++ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = canProceed,
                        shape = RoundedCornerShape(GrahbookRadius.lg),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Brand800,
                            disabledContainerColor = Color.White.copy(alpha = 0.3f),
                            disabledContentColor = Brand800.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            if (currentStep == 0) stringResource(R.string.chalo_shuru_karein) else stringResource(R.string.aage_button),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentedProgressBar(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSteps) { index ->
            val isCompleted = index < currentStep
            val isCurrent = index == currentStep
            val animProgress = remember { Animatable(0f) }

            LaunchedEffect(isCompleted, isCurrent) {
                if (isCompleted || isCurrent) {
                    animProgress.snapTo(0f)
                    animProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    )
                } else {
                    animProgress.snapTo(0f)
                }
            }

            val fillFraction by animProgress.asState()
            val color = if (isCompleted || isCurrent)
                Color.White else Color.White.copy(alpha = 0.3f)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fillFraction)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
            }
        }
    }
}

// === STEP 1: Welcome ===

@Composable
private fun WelcomeStep() {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon row
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(36.dp).scale(scaleAnim)
            )
            Icon(
                Icons.AutoMirrored.Outlined.ReceiptLong,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp)
            )
            Icon(
                Icons.Default.CurrencyRupee,
                contentDescription = null,
                tint = OnboardingGold,
                modifier = Modifier.size(48.dp).offset(y = floatOffset.dp)
            )
            Icon(
                Icons.AutoMirrored.Outlined.Chat,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp)
            )
            Icon(
                Icons.Outlined.Groups,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(36.dp).scale(scaleAnim)
            )
        }

        Spacer(Modifier.height(40.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_welcome_subtitle),
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // Feature chips
        FeatureChip(stringResource(R.string.feature_bill_send), Icons.AutoMirrored.Outlined.Send)
        Spacer(Modifier.height(8.dp))
        FeatureChip(stringResource(R.string.feature_digital_khata), Icons.Outlined.AccountBalance)
        Spacer(Modifier.height(8.dp))
        FeatureChip(stringResource(R.string.feature_whatsapp_reminder), Icons.Outlined.Notifications)
    }
}

@Composable
private fun FeatureChip(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null, tint = OnboardingGold, modifier = Modifier.size(20.dp))
        Text(text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

// === STEP 2: WhatsApp Bot ===

@Composable
private fun WhatsAppBotStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.onboarding_bot_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_bot_subtitle),
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // Animated chat bubble
        var showBubble1 by remember { mutableStateOf(false) }
        var showBubble2 by remember { mutableStateOf(false) }
        var showCheck1 by remember { mutableStateOf(false) }
        var showCheck2 by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            showBubble1 = true
            delay(600)
            showCheck1 = true
            delay(800)
            showBubble2 = true
            delay(600)
            showCheck2 = true
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppDark)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.onboarding_bot_name), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.size(8.dp).clip(CircleShape).background(WhatsAppGreen))
                }

                Spacer(Modifier.height(16.dp))

                // Chat bubble 1
                AnimatedVisibility(
                    visible = showBubble1,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = fadeOut()
                ) {
                    ChatBubble("Ramesh ne 500 rupye diye", isUser = false)
                }

                AnimatedVisibility(
                    visible = showCheck1,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it })
                ) {
                    Row(
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(14.dp))
                        Text(stringResource(R.string.payment_recorded_check), color = WhatsAppGreen, fontSize = 11.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Chat bubble 2
                AnimatedVisibility(
                    visible = showBubble2,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = fadeOut()
                ) {
                    ChatBubble("Suresh ko 1200 ka bill bhejo", isUser = false)
                }

                AnimatedVisibility(
                    visible = showCheck2,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it })
                ) {
                    Row(
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(14.dp))
                        Text(stringResource(R.string.invoice_sent_check), color = WhatsAppGreen, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Info card
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text("✨", fontSize = 16.sp)
            Text(stringResource(R.string.no_app_needed), color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun ChatBubble(text: String, isUser: Boolean) {
    Box(
        modifier = Modifier
            .widthIn(max = 260.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = if (isUser) 4.dp else 16.dp, bottomStart = if (isUser) 16.dp else 4.dp))
            .background(if (isUser) WhatsAppLight else Color.White)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        )
    }
}

// === STEP 3: Shop Setup ===

@Composable
private fun ShopSetupStep(state: MutableState<OnboardingState>) {
    val categories = listOf("Kirana", "Medical", "Kapda", "Electronics", "Other")
    val languages = listOf("Hindi", "English", "Hinglish")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.shop_setup_title),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.shop_setup_subtitle),
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        // Shop name
        OutlinedTextField(
            value = state.value.shopName,
            onValueChange = { state.value = state.value.copy(shopName = it) },
            label = { Text(stringResource(R.string.shop_name_label), color = Color.White.copy(alpha = 0.7f)) },
            placeholder = { Text(stringResource(R.string.shop_name_placeholder), color = Color.White.copy(alpha = 0.4f)) },
            leadingIcon = { Icon(Icons.Outlined.Store, contentDescription = null, tint = OnboardingGold) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White.copy(alpha = 0.6f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                cursorColor = Color.White,
                focusedLabelColor = Color.White.copy(alpha = 0.8f),
                unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        // Category picker
        Text(
            text = stringResource(R.string.category_label),
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = state.value.category == cat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color.White else Color.White.copy(alpha = 0.1f)
                        )
                        .clickable { state.value = state.value.copy(category = cat) }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Brand800 else Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Language preference
        Text(
            text = stringResource(R.string.language_label),
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            languages.forEach { lang ->
                val isSelected = state.value.language == lang
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) OnboardingGold else Color.White.copy(alpha = 0.1f)
                        )
                        .clickable { state.value = state.value.copy(language = lang) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lang,
                        color = if (isSelected) Brand800 else Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// === STEP 4: Ready ===

@Composable
private fun ReadyStep(state: MutableState<OnboardingState>, onComplete: () -> Unit) {
    var showContent by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showContent = true
        delay(500)
        showButton = true
    }

    val scale by animateFloatAsState(
        targetValue = if (showContent) 1f else 0.95f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "check_scale"
    )
    val checkAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(200, easing = LinearOutSlowInEasing),
        label = "check_alpha"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated checkmark — Emil: never start from scale(0); use 0.95 + opacity
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(scale)
                .graphicsLayer { alpha = checkAlpha }
                .clip(CircleShape)
                .background(RupeeGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it })
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.onboarding_ready_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.onboarding_ready_subtitle),
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                // Show shop name summary
                if (state.value.shopName.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Store,
                                contentDescription = null,
                                tint = OnboardingGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = state.value.shopName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = state.value.category.ifBlank { "General Store" },
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = OnboardingGold, modifier = Modifier.size(16.dp))
                Text(
                    stringResource(R.string.onboarding_social_proof),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it })
        ) {
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Brand800
                )
            ) {
                Text(
                    text = stringResource(R.string.chalo_shuru_karte_hain),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        }
    }
}
