package com.zando.app.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zando.app.ui.components.CategoryChip
import com.zando.app.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var activeTab by remember { mutableStateOf("sign_in") }

    // Sign-in state
    var signInEmail by remember { mutableStateOf("") }
    var signInPassword by remember { mutableStateOf("") }

    // Register state
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            onLoginSuccess()
            viewModel.resetSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(60.dp))

        Text(
            text = "ZANDO",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = "Fashion for everyone",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                label = "Sign In",
                selected = activeTab == "sign_in",
                onClick = { activeTab = "sign_in" },
                modifier = Modifier.weight(1f)
            )
            CategoryChip(
                label = "Register",
                selected = activeTab == "register",
                onClick = { activeTab = "register" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        uiState.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error,
                 style = MaterialTheme.typography.bodySmall,
                 modifier = Modifier.padding(bottom = 8.dp))
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))
        }

        AnimatedVisibility(visible = activeTab == "sign_in") {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ZandoTextField(signInEmail, { signInEmail = it }, "Email address", keyboardType = KeyboardType.Email)
                ZandoTextField(signInPassword, { signInPassword = it }, "Password", isPassword = true)
                ZandoPrimaryButton(
                    text = "Sign In",
                    onClick = { viewModel.signIn(signInEmail, signInPassword) }
                )
            }
        }

        AnimatedVisibility(visible = activeTab == "register") {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ZandoTextField(regName, { regName = it }, "Full name")
                ZandoTextField(regEmail, { regEmail = it }, "Email address", keyboardType = KeyboardType.Email)
                ZandoTextField(regPassword, { regPassword = it }, "Password", isPassword = true)
                ZandoPrimaryButton(
                    text = "Create Account",
                    onClick = { viewModel.signUp(regName, regEmail, regPassword) }
                )
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun ZandoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else
            androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
private fun ZandoPrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
