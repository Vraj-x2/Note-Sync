package week11.st856364.finalproject.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    var isListening by remember { mutableStateOf(false) }
    var translatingSpeech by remember { mutableStateOf(false) }

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

    val colorOptions = listOf(
        0xFFFFCDD2,
        0xFFFFF9C4,
        0xFFC8E6C9,
        0xFFBBDEFB,
        0xFFD1C4E9,
        0xFFFFF59D
    )

    var langMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTitle.isEmpty()) "Add Note" else "Edit Note") },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 420.dp)
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

                Text("Choose Color", fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    colorOptions.forEach { clr ->
                        val c = Color(clr.toInt())
                        val isSelected = clr == selectedColor

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    if (isSelected) 3.dp else 1.dp,
                                    if (isSelected) Color.Black else Color.Gray,
                                    CircleShape
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

                ExposedDropdownMenuBox(
                    expanded = langMenuExpanded,
                    onExpandedChange = { langMenuExpanded = !langMenuExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = languageOptions.first { it.second == selectedLanguage }.first,
                        onValueChange = {},
                        label = { Text("Note Language") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = langMenuExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = langMenuExpanded,
                        onDismissRequest = { langMenuExpanded = false }
                    ) {
                        languageOptions.forEach { (label, code) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedLanguage = code
                                    langMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voice Input", fontSize = 15.sp)

                    IconButton(
                        onClick = {
                            if (!isListening) {
                                isListening = true
                                translatingSpeech = true

                                speechController.startListening(
                                    language = selectedLanguage,
                                    onResult = { spoken ->
                                        GoogleTranslateService.translate(
                                            spoken, selectedLanguage
                                        ) { translated ->
                                            if (!translated.isNullOrBlank())
                                                content += "\n$translated"

                                            isListening = false
                                            translatingSpeech = false
                                        }
                                    },
                                    onError = {
                                        isListening = false
                                        translatingSpeech = false
                                    }
                                )
                            } else {
                                speechController.stopListening()
                                isListening = false
                                translatingSpeech = false
                            }
                        }
                    ) {
                        if (isListening)
                            Icon(Icons.Default.Stop, null)
                        else
                            Icon(Icons.Default.Mic, null)
                    }
                }

                if (translatingSpeech)
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(title, content, selectedColor, selectedLanguage)
                }
            ) { Text("Save") }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
