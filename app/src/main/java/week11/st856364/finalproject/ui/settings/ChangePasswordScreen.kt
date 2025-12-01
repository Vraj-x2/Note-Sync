package week11.st856364.finalproject.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import week11.st856364.finalproject.ui.auth.AuthViewModel
import week11.st856364.finalproject.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    val uiState by authViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
        ) {

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    authViewModel.changePassword(newPassword)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is UiState.Loading
            ) {
                if (uiState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Password")
                }
            }

            Spacer(Modifier.height(8.dp))

            when (uiState) {
                is UiState.Error ->
                    Text((uiState as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error)

                is UiState.Success ->
                    Text((uiState as UiState.Success).message,
                        color = MaterialTheme.colorScheme.primary)

                else -> {}
            }
        }
    }
}
