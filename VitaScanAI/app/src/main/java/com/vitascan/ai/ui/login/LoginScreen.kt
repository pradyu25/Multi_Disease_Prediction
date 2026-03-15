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
            .background(GhostWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            // Minimalist Logo / branding
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = PureBlack
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("V", color = PureWhite, style = MaterialTheme.typography.displayMedium)
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                "VITASCAN AI",
                style = MaterialTheme.typography.displayMedium,
                color = PureBlack,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            Text(
                "PRECISION DIAGNOSTICS PLATFORM",
                style = MaterialTheme.typography.labelSmall,
                color = MediumGray,
                letterSpacing = 1.sp
            )
            
            Spacer(Modifier.height(48.dp))

            // Credentials
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value        = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label        = { Text("Email", style = MaterialTheme.typography.labelMedium) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine  = true,
                    modifier    = Modifier.fillMaxWidth(),
                    shape       = RoundedCornerShape(16.dp),
                    colors      = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = PureBlack,
                        unfocusedBorderColor = BorderGray,
                        focusedLabelColor    = PureBlack
                    )
                )

                OutlinedTextField(
                    value        = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label        = { Text("Password", style = MaterialTheme.typography.labelMedium) },
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
                    shape       = RoundedCornerShape(16.dp),
                    colors      = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = PureBlack,
                        unfocusedBorderColor = BorderGray,
                        focusedLabelColor    = PureBlack
                    )
                )
            }

            // Error
            AnimatedVisibility(visible = state.error != null) {
                state.error?.let {
                    Text(it, color = PureBlack, 
                         style = MaterialTheme.typography.bodySmall,
                         modifier = Modifier.padding(top = 16.dp))
                }
            }

            Spacer(Modifier.height(40.dp))

            // Login button
            Button(
                onClick   = viewModel::login,
                modifier  = Modifier.fillMaxWidth().height(60.dp),
                enabled   = !state.isLoading,
                shape     = RoundedCornerShape(16.dp),
                colors    = ButtonDefaults.buttonColors(containerColor = PureBlack)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color  = PureWhite,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 1.dp
                    )
                } else {
                    Text("SECURE LOGIN", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
            
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("NEEDS AN ACCOUNT? ", color = MediumGray, style = MaterialTheme.typography.labelSmall)
                Text(
                    "REGISTER",
                    color      = PureBlack,
                    fontWeight = FontWeight.Black,
                    style      = MaterialTheme.typography.labelSmall,
                    modifier   = Modifier.clickable(onClick = onNavigateToSignup)
                )
            }
        }
    }
}
