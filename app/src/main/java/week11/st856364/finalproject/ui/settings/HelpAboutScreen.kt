package week11.st856364.finalproject.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpAboutScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help / About") },
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
                .fillMaxSize()
        ) {

            Text("NoteSync", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "A smart multilingual notes app with voice input and Firebase sync.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(20.dp))

            FeatureCard(
                icon = Icons.Default.ColorLens,
                title = "Colorful notes",
                body = "Organize your thoughts with colored note tags and a clean layout."
            )

            Spacer(Modifier.height(12.dp))

            FeatureCard(
                icon = Icons.Default.Language,
                title = "Per-note translation",
                body = "Each note can have its own language (English, Hindi, Ukrainian, and more)."
            )

            Spacer(Modifier.height(12.dp))

            FeatureCard(
                icon = Icons.Default.Mic,
                title = "Voice to text",
                body = "Tap the mic, speak, and NoteSync will transcribe and translate into your note."
            )

            Spacer(Modifier.height(12.dp))

            FeatureCard(
                icon = Icons.Default.Security,
                title = "Secure sync",
                body = "Firebase Auth sign-in plus Firestore storage keep your notes backed up."
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "Tip: Long-swipe a note to delete it, and tap the pin icon to keep important notes at the top.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun FeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null)
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(body, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
