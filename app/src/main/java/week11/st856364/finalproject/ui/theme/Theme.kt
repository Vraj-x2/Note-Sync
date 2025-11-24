package week11.st856364.finalproject.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// ---------- COLORS ----------
private val LightColors = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val DarkColors = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

// ---------- THEME FUNCTION ----------
// IMPORTANT: name MUST be NoteSyncTheme (NOT NotesSyncTheme)
@Composable
fun NoteSyncTheme(
    darkTheme: Boolean = false,   // keep it simple, no dynamic color needed
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
