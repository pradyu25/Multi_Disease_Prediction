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
            Text(
                "REGISTRATION",
                style = MaterialTheme.typography.labelSmall,
                color = MediumGray,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "Create Profile",
                style = MaterialTheme.typography.displayMedium,
                color = PureBlack,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            
            Spacer(Modifier.height(40.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf(
                    Triple(state.name, viewModel::onNameChange, "Full Name"),
                    Triple(state.email, viewModel::onEmailChange, "Email Address"),
                    Triple(state.password, viewModel::onPasswordChange, "Password"),
                    Triple(state.confirmPassword, viewModel::onConfirmPasswordChange, "Confirm Password")
                ).forEachIndexed { index, triple ->
                    val (value, onChange, label) = triple
                    val isPassword = index >= 2
                    OutlinedTextField(
                        value         = value,
                        onValueChange = onChange,
                        label         = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email
                        ),
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(16.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = PureBlack,
                            unfocusedBorderColor = BorderGray,
                            focusedLabelColor    = PureBlack
                        )
                    )
                }
            }

            AnimatedVisibility(state.error != null) {
                state.error?.let { err ->
                    Text(err, color = PureBlack, 
                         style = MaterialTheme.typography.bodySmall,
                         modifier = Modifier.padding(top = 16.dp))
                }
            }

            Spacer(Modifier.height(40.dp))

            Button(
                onClick   = viewModel::signup,
                enabled   = !state.isLoading,
                modifier  = Modifier.fillMaxWidth().height(60.dp),
                shape     = RoundedCornerShape(16.dp),
                colors    = ButtonDefaults.buttonColors(containerColor = PureBlack)
            ) {
                if (state.isLoading) CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(24.dp), strokeWidth = 1.dp)
                else Text("CREATE ACCOUNT", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }

            Spacer(Modifier.height(24.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("ALREADY REGISTERED? ", color = MediumGray, style = MaterialTheme.typography.labelSmall)
                Text(
                    "LOG IN", 
                    color = PureBlack, 
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.clickable(onClick = onNavigateToLogin)
                )
            }
        }
    }
}
