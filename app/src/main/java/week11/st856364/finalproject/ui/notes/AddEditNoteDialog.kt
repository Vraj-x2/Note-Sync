package week11.st856364.finalproject.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st856364.finalproject.network.GoogleTranslateService
import week11.st856364.finalproject.ui.speech.SpeechRecognizerController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteDialog(
    isOpen: Boolean,
    initialTitle: String,
    initialContent: String,
    initialColor: Long?,
    initialLanguage: String,
    speechController: SpeechRecognizerController,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long, String) -> Unit
) {
    if (!isOpen) return

    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }
    var selectedColor by remember { mutableStateOf(initialColor ?: 0xFFFFF59D) }
    var selectedLanguage by remember { mutableStateOf(initialLanguage) }

    var langExpanded by remember { mutableStateOf(false) }

    var isListening by remember { mutableStateOf(false) }
    var isTranslating by remember { mutableStateOf(false) }

    val languageOptions = listOf(
        "English" to "en",
        "Hindi" to "hi",
        "Spanish" to "es",
        "French" to "fr",
        "German" to "de",
        "Arabic" to "ar",
        "Chinese" to "zh",
        "Korean" to "ko",
        "Japanese" to "ja",
        "Ukrainian" to "uk"
    )

    val colorOptions = listOf(
        0xFFFFCDD2, // red
        0xFFFFF9C4, // yellow
        0xFFC8E6C9, // green
        0xFFBBDEFB, // blue
        0xFFD1C4E9, // purple
        0xFFFFF59D  // soft yellow
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialTitle.isEmpty()) "New Note" else "Edit Note",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp, max = 520.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                // COLOR PICKER
                Text("Color", fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorOptions.forEach { clr ->
                        val col = Color(clr.toInt())
                        val selected = clr == selectedColor

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) Color.Black else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    selectedColor = clr
                                }
                        )
                    }
                }

                // LANGUAGE DROPDOWN (larger row)
                Column {
                    Text("Note Language", fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F0FF))
                            .clickable { langExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val label = languageOptions.firstOrNull { it.second == selectedLanguage }?.first
                            ?: "English"
                        Text(label, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }

                    androidx.compose.material3.DropdownMenu(
                        expanded = langExpanded,
                        onDismissRequest = { langExpanded = false }
                    ) {
                        languageOptions.forEach { (label, code) ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedLanguage = code
                                    langExpanded = false
                                }
                            )
                        }
                    }
                }

                // VOICE INPUT CARD
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEDE7FF))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Voice Input",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "Speak and we'll transcribe & translate to the selected language.",
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                if (!isListening) {
                                    isListening = true
                                    isTranslating = true

                                    speechController.startListening(
                                        language = selectedLanguage,
                                        onResult = { spoken ->
                                            // Translate spoken → selected language
                                            GoogleTranslateService.translate(
                                                text = spoken,
                                                targetLang = selectedLanguage
                                            ) { translated ->
                                                if (!translated.isNullOrBlank()) {
                                                    content += "\n$translated"
                                                }
                                                isListening = false
                                                isTranslating = false
                                            }
                                        },
                                        onError = {
                                            isListening = false
                                            isTranslating = false
                                        }
                                    )
                                } else {
                                    speechController.stopListening()
                                    isListening = false
                                    isTranslating = false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = null
                            )
                        }
                    }

                    if (isTranslating) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(title, content, selectedColor, selectedLanguage)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
