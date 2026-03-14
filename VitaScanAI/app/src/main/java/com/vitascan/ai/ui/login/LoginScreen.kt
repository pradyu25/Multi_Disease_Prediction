package com.vitascan.ai.ui.login

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitascan.ai.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MedicalBlueDark, MedicalBlue, MedicalBlueLight)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / branding
            Icon(
                imageVector = Icons.Default.MonitorHeart,
                contentDescription = "VitaScan AI",
                tint = Color.White,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "VitaScan AI",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Medical Intelligence Platform",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(40.dp))

            // Card
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(24.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(Modifier.padding(28.dp)) {
                    Text(
                        "Welcome Back",
                        style     = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color     = NeutralGrayDark
                    )
                    Text(
                        "Sign in to your account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralGray
                    )
                    Spacer(Modifier.height(24.dp))

                    // Email
                    OutlinedTextField(
                        value        = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label        = { Text("Email Address") },
                        leadingIcon  = { Icon(Icons.Default.Email, null, tint = MedicalBlue) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction    = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine  = true,
                        modifier    = Modifier.fillMaxWidth(),
                        shape       = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(16.dp))

                    // Password
                    OutlinedTextField(
                        value        = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label        = { Text("Password") },
                        leadingIcon  = { Icon(Icons.Default.Lock, null, tint = MedicalBlue) },
                        trailingIcon = {
                            IconButton(onClick = viewModel::togglePasswordVisibility) {
                                Icon(
                                    if (state.passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null
                                )
                            }
                        },
                        visualTransformation = if (state.passwordVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            viewModel.login()
                        }),
                        singleLine  = true,
                        modifier    = Modifier.fillMaxWidth(),
                        shape       = RoundedCornerShape(12.dp)
                    )

                    // Error
                    AnimatedVisibility(visible = state.error != null) {
                        state.error?.let {
                            Spacer(Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, null, tint = RiskHigh)
                                    Spacer(Modifier.width(8.dp))
                                    Text(it, color = RiskHigh, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Login button
                    Button(
                        onClick   = viewModel::login,
                        modifier  = Modifier.fillMaxWidth().height(52.dp),
                        enabled   = !state.isLoading,
                        shape     = RoundedCornerShape(12.dp),
                        colors    = ButtonDefaults.buttonColors(containerColor = MedicalBlue)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color  = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Don't have an account? ", color = NeutralGray)
                        Text(
                            "Sign Up",
                            color      = MedicalBlue,
                            fontWeight = FontWeight.SemiBold,
                            modifier   = Modifier.clickable(onClick = onNavigateToSignup)
                        )
                    }
                }
            }
        }
    }
}
