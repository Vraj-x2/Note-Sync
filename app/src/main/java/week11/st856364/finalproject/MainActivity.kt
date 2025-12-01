package week11.st856364.finalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import week11.st856364.finalproject.navigation.AppNavGraph
import week11.st856364.finalproject.ui.auth.AuthViewModel
import week11.st856364.finalproject.ui.notes.NotesViewModel
import week11.st856364.finalproject.ui.theme.NoteSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NoteSyncTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val notesViewModel: NotesViewModel = viewModel()

                Surface {
                    AppNavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        notesViewModel = notesViewModel
                    )
                }
            }
        }
    }
}
