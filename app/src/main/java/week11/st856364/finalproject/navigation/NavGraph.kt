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
import week11.st856364.finalproject.ui.settings.*
import week11.st856364.finalproject.utils.UiState

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT = "forgot"
    const val NOTES = "notes"
    const val PROFILE = "profile"

    const val UPDATE_EMAIL = "update_email"
    const val CHANGE_PASSWORD = "change_password"
    const val EXPORT_NOTES = "export_notes"
    const val HELP = "help"
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

        // LOGIN
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                uiState = uiState,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgot = { navController.navigate(Routes.FORGOT) }
            )
        }

        // REGISTER
        composable(Routes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                uiState = uiState,
                onNavigateBackToLogin = { navController.popBackStack() }
            )
        }

        // FORGOT
        composable(Routes.FORGOT) {
            ForgotPasswordScreen(
                authViewModel = authViewModel,
                uiState = uiState,
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // NOTES
        composable(Routes.NOTES) {
            if (authState is AuthState.Authenticated) {
                notesViewModel.startListening()
            }

            val notesUiState by notesViewModel.uiState.collectAsState()

            NotesScreen(
                notesViewModel = notesViewModel,
                uiState = notesUiState,
                onLogout = {
                    notesViewModel.stopListening()
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.NOTES) { inclusive = true }
                    }
                },
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }



        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.EXPORT_NOTES) {
            ExportNotesScreen(
                notesViewModel = notesViewModel,
                onBack = { navController.popBackStack() }
            )
        }
// PROFILE SCREEN
        composable(Routes.PROFILE) {
            ProfileScreen(
                authViewModel = authViewModel,
                notesViewModel = notesViewModel,
                onBack = { navController.popBackStack() }
            )
        }



        composable(Routes.HELP) {
            HelpAboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
