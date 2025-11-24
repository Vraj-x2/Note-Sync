package week11.st856364.finalproject.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import week11.st856364.finalproject.ui.auth.*
import week11.st856364.finalproject.ui.notes.NotesScreen
import week11.st856364.finalproject.ui.notes.NotesViewModel
import week11.st856364.finalproject.utils.UiState

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT = "forgot"
    const val NOTES = "notes"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    notesViewModel: NotesViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val uiState by authViewModel.uiState.collectAsState(initial = UiState.Idle)

    val startDestination =
        if (authState is AuthState.Authenticated) Routes.NOTES else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                uiState = uiState,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgot = { navController.navigate(Routes.FORGOT) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                uiState = uiState,
                onNavigateBackToLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.FORGOT) {
            ForgotPasswordScreen(
                authViewModel = authViewModel,
                uiState = uiState,
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.NOTES) {
            val notesUiState by notesViewModel.uiState.collectAsState()
            NotesScreen(
                notesViewModel = notesViewModel,
                uiState = notesUiState,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.NOTES) { inclusive = true }
                    }
                }
            )
        }
    }
}
