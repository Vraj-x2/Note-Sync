package week11.st856364.finalproject.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st856364.finalproject.data.model.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onChangeLanguage: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,                
                onClick = onClick
            ),
        tonalElevation = if (note.pinned) 4.dp else 1.dp,
        color = Color(note.color.toInt())
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(
                    "[${note.languageCode.uppercase()}]",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = .5f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                Spacer(Modifier.weight(1f))

                IconButton(onClick = onTogglePin) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "Pin",
                        tint = if (note.pinned) Color.Black else Color.DarkGray
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                note.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                note.content,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

            val languageOptions = listOf(
                "English" to "en",
                "Spanish" to "es",
                "French" to "fr",
                "Arabic" to "ar",
                "Hindi" to "hi",
                "Chinese" to "zh",
                "German" to "de",
                "Korean" to "ko",
                "Japanese" to "ja",
                "Ukrainian" to "uk"
            )

            var expanded by remember { mutableStateOf(false) }

            val selected =
                languageOptions.firstOrNull { it.second == note.languageCode }
                    ?: languageOptions.first()

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {

                OutlinedTextField(
                    value = selected.first,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Translate note to") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    languageOptions.forEach { (label, code) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                expanded = false
                                if (code != note.languageCode) {
                                    onChangeLanguage(code)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
