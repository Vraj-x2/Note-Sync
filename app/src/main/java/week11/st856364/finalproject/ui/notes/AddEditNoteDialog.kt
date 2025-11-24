package week11.st856364.finalproject.ui.notes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import week11.st856364.finalproject.ui.speech.SpeechRecognizerController

@Composable
fun AddEditNoteDialog(
    isOpen: Boolean,
    initialTitle: String = "",
    initialContent: String = "",
    speechController: SpeechRecognizerController?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String) -> Unit
) {
    if (!isOpen) return

    var titleState by remember { mutableStateOf(TextFieldValue(initialTitle)) }
    var contentState by remember { mutableStateOf(TextFieldValue(initialContent)) }
    var isRecording by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(titleState.text, contentState.text)
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text(if (initialTitle.isEmpty()) "New Note" else "Edit Note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titleState,
                    onValueChange = { titleState = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contentState,
                    onValueChange = { contentState = it },
                    label = { Text("Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (!isRecording) {
                                isRecording = true
                                speechController?.startListening(
                                    onResult = { text ->
                                        contentState =
                                            TextFieldValue(contentState.text + " " + text)
                                        isRecording = false
                                    },
                                    onError = {
                                        // you could show snackbar in parent if needed
                                        isRecording = false
                                    }
                                )
                            } else {
                                isRecording = false
                                speechController?.stopListening()
                            }
                        }
                    ) {
                        Text(if (isRecording) "Stop Recording" else "Use Voice")
                    }
                }
            }
        }
    )
}
