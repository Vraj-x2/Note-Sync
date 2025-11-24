package week11.st856364.finalproject.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st856364.finalproject.utils.UiState

@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    uiState: UiState,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Reset Password", fontSize = 24.sp)

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    authViewModel.sendResetPassword(email.trim())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send reset email")
            }

            TextButton(onClick = onBackToLogin) {
                Text("Back to Login")
            }

            when (uiState) {
                is UiState.Error -> {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }

                is UiState.Success -> {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                }

                else -> {}
            }
        }
    }
}
