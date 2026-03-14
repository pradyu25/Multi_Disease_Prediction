package com.vitascan.ai.ui.signup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitascan.ai.ui.theme.*

@Composable
fun SignupScreen(
    viewModel: SignupViewModel,
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onSignupSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(HealthGreenDark, HealthGreen, HealthGreenLight)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.PersonAdd, null, tint = Color.White, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(8.dp))
            Text("Create Account", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Join VitaScan AI today", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.75f))
            Spacer(Modifier.height(32.dp))

            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(24.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(Modifier.padding(28.dp)) {
                    listOf(
                        Triple(state.name, viewModel::onNameChange, "Full Name") to Icons.Default.Person,
                        Triple(state.email, viewModel::onEmailChange, "Email Address") to Icons.Default.Email,
                        Triple(state.password, viewModel::onPasswordChange, "Password") to Icons.Default.Lock,
                        Triple(state.confirmPassword, viewModel::onConfirmPasswordChange, "Confirm Password") to Icons.Default.Lock
                    ).forEachIndexed { index, (triple, icon) ->
                        val (value, onChange, label) = triple
                        val isPassword = index >= 2
                        OutlinedTextField(
                            value         = value,
                            onValueChange = onChange,
                            label         = { Text(label) },
                            leadingIcon   = { Icon(icon, null, tint = HealthGreen) },
                            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email
                            ),
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(12.dp)
                        )
                        if (index < 3) Spacer(Modifier.height(12.dp))
                    }

                    AnimatedVisibility(state.error != null) {
                        state.error?.let { err ->
                            Spacer(Modifier.height(8.dp))
                            Text(err, color = RiskHigh, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick   = viewModel::signup,
                        enabled   = !state.isLoading,
                        modifier  = Modifier.fillMaxWidth().height(52.dp),
                        shape     = RoundedCornerShape(12.dp),
                        colors    = ButtonDefaults.buttonColors(containerColor = HealthGreen)
                    ) {
                        if (state.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        else Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text("Already have an account? ", color = NeutralGray)
                        Text("Sign In", color = HealthGreen, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable(onClick = onNavigateToLogin))
                    }
                }
            }
        }
    }
}
