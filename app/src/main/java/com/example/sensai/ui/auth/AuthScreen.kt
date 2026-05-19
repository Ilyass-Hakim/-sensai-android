package com.example.sensai.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sensai.ui.theme.*

// ── Local palette ─────────────────────────────────────────────────────────────
private val Indigo    = Color(0xFF4F46E5)
private val Violet    = Color(0xFF7C3AED)
private val VioletLt  = Color(0xFF9D6FFF)
private val BgBase    = Color(0xFF0D0B1A)
private val BgCard    = Color(0xFF1A1635)
private val BgField   = Color(0xFF211D3E)
private val Border    = Color(0xFF2E2850)
private val TextPri   = Color(0xFFF0EBFF)
private val TextSec   = Color(0xFFB8AEDE)
private val TextMut   = Color(0xFF7A6FA8)
private val ErrorRed  = Color(0xFFEF4444)

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) onAuthSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
    ) {
        // ── Ambient glow blobs ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-60).dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Violet.copy(alpha = 0.18f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Indigo.copy(alpha = 0.14f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        // ── Content ───────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))

            // ── Logo / Brand ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(listOf(Indigo, Violet))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("S", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Gradient text via drawBehind workaround
            Text(
                text = "SensAI",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = VioletLt,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Votre assistant anime intelligent",
                fontSize = 13.sp,
                color = TextMut,
                textAlign = TextAlign.Center,
                letterSpacing = 0.3.sp
            )

            Spacer(Modifier.height(40.dp))

            // ── Pill tab switcher ─────────────────────────────────────────────
            PillTabRow(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it; viewModel.resetState() }
            )

            Spacer(Modifier.height(32.dp))

            // ── Animated form swap ────────────────────────────────────────────
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "form_swap"
            ) { tab ->
                if (tab == 0) {
                    LoginForm(
                        isLoading = uiState is AuthUiState.Loading,
                        errorMessage = (uiState as? AuthUiState.Error)?.message,
                        onLogin = { email, pass -> viewModel.login(email, pass) }
                    )
                } else {
                    RegisterForm(
                        isLoading = uiState is AuthUiState.Loading,
                        errorMessage = (uiState as? AuthUiState.Error)?.message,
                        onRegister = { email, user, pass -> viewModel.register(email, user, pass) }
                    )
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

// ── Pill tab row ──────────────────────────────────────────────────────────────
@Composable
private fun PillTabRow(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(BgCard)
            .border(1.dp, Border, RoundedCornerShape(50))
            .padding(4.dp)
    ) {
        Row {
            listOf("Connexion", "Inscription").forEachIndexed { index, label ->
                val selected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .then(
                            if (selected) Modifier.background(
                                Brush.horizontalGradient(listOf(Indigo, Violet))
                            ) else Modifier
                        )
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                        letterSpacing = 0.4.sp,
                        color = if (selected) Color.White else TextMut
                    )
                }
            }
        }
    }
}

// ── Login form ────────────────────────────────────────────────────────────────
@Composable
fun LoginForm(
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SensAiTextField(value = email, onValueChange = { email = it },
            label = "Email", placeholder = "votre@email.com")
        SensAiTextField(value = password, onValueChange = { password = it },
            label = "Mot de passe", placeholder = "••••••••", isPassword = true)

        errorMessage?.let {
            ErrorBanner(it)
        }

        Spacer(Modifier.height(8.dp))
        MainButton(text = "Se connecter", isLoading = isLoading,
            onClick = { onLogin(email, password) })
    }
}

// ── Register form ─────────────────────────────────────────────────────────────
@Composable
fun RegisterForm(
    isLoading: Boolean,
    errorMessage: String?,
    onRegister: (String, String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SensAiTextField(value = email, onValueChange = { email = it },
            label = "Email", placeholder = "votre@email.com")
        SensAiTextField(value = username, onValueChange = { username = it },
            label = "Nom d'utilisateur", placeholder = "Pseudo")
        SensAiTextField(value = password, onValueChange = { password = it },
            label = "Mot de passe", placeholder = "••••••••", isPassword = true)

        errorMessage?.let {
            ErrorBanner(it)
        }

        Spacer(Modifier.height(8.dp))
        MainButton(text = "S'inscrire", isLoading = isLoading,
            onClick = { onRegister(email, username, password) })
    }
}

// ── Styled text field ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensAiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPassword: Boolean = false
) {
    var focused by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (focused) Violet else Border,
        animationSpec = tween(250),
        label = "border"
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        placeholder = { Text(placeholder, color = TextMut, fontSize = 14.sp) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focused = it.isFocused },
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility
                                      else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = TextMut
                    )
                }
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = borderColor,
            unfocusedBorderColor = borderColor,
            focusedContainerColor   = BgField,
            unfocusedContainerColor = BgField,
            focusedLabelColor    = VioletLt,
            unfocusedLabelColor  = TextSec,
            focusedTextColor     = TextPri,
            unfocusedTextColor   = TextPri,
            cursorColor          = VioletLt
        ),
        shape = RoundedCornerShape(14.dp)
    )
}

// ── CTA button ────────────────────────────────────────────────────────────────
@Composable
fun MainButton(text: String, isLoading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp),
                spotColor = Violet.copy(alpha = 0.5f),
                ambientColor = Violet.copy(alpha = 0.3f))
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (!isLoading) Brush.horizontalGradient(listOf(Indigo, Violet))
                else Brush.horizontalGradient(listOf(BgCard, BgCard))
            )
            .clickable(enabled = !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp),
                color = VioletLt, strokeWidth = 2.dp)
        } else {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold,
                fontSize = 15.sp, letterSpacing = 0.5.sp)
        }
    }
}

// ── Error banner ──────────────────────────────────────────────────────────────
@Composable
private fun ErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ErrorRed.copy(alpha = 0.12f))
            .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("⚠  $message", color = ErrorRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
