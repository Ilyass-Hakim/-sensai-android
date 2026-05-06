package com.example.sensai.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sensai.ui.theme.*

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onAuthSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgDeep
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "SensAI",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = VioletPrimary
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = VioletPrimary,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = VioletPrimary
                        )
                    }
                },
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; viewModel.resetState() },
                    text = { Text("Connexion", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; viewModel.resetState() },
                    text = { Text("Inscription", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (selectedTab == 0) {
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
    }
}

@Composable
fun LoginForm(
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SensAiTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "votre@email.com"
        )
        SensAiTextField(
            value = password,
            onValueChange = { password = it },
            label = "Mot de passe",
            placeholder = "********",
            isPassword = true
        )

        errorMessage?.let {
            Text(text = it, color = Color.Red, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        MainButton(
            text = "Se connecter",
            isLoading = isLoading,
            onClick = { onLogin(email, password) }
        )
    }
}

@Composable
fun RegisterForm(
    isLoading: Boolean,
    errorMessage: String?,
    onRegister: (String, String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SensAiTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "votre@email.com"
        )
        SensAiTextField(
            value = username,
            onValueChange = { username = it },
            label = "Nom d'utilisateur",
            placeholder = "Pseudo"
        )
        SensAiTextField(
            value = password,
            onValueChange = { password = it },
            label = "Mot de passe",
            placeholder = "********",
            isPassword = true
        )

        errorMessage?.let {
            Text(text = it, color = Color.Red, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        MainButton(
            text = "S'inscrire",
            isLoading = isLoading,
            onClick = { onRegister(email, username, password) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensAiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = TextMuted) },
        modifier = Modifier
            .fillMaxWidth()
            .background(BgElevated, RoundedCornerShape(12.dp)),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VioletPrimary,
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = VioletPrimary,
            unfocusedLabelColor = TextSecondary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = VioletPrimary
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun MainButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
