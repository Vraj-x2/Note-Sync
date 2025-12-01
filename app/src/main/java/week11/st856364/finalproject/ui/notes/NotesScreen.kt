package week11.st856364.finalproject.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import week11.st856364.finalproject.data.model.Note
import week11.st856364.finalproject.ui.speech.SpeechRecognizerController
import week11.st856364.finalproject.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    notesViewModel: NotesViewModel,
    uiState: UiState,
    onLogout: () -> Unit
) {
    val notes by notesViewModel.filteredNotes.collectAsState()
    val searchText by notesViewModel.searchText.collectAsState()
    val userEmail by notesViewModel.userEmail.collectAsState()

    val context = LocalContext.current
    val speechController = remember { SpeechRecognizerController(context) }

    var isDialogOpen by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ===============================
    // Navigation Drawer
    // ===============================
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {

                Text("Menu", modifier = Modifier.padding(16.dp))

                NavigationDrawerItem(
                    label = { Text("Profile") },
                    selected = false,
                    onClick = {},
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },

            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Hi, $userEmail", fontSize = 14.sp)
                            Text("Welcome Back!", fontSize = 20.sp)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.ExitToApp, "Logout")
                        }
                    }
                )
            },

            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingNote = null
                        isDialogOpen = true
                    }
                ) { Text("+") }
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {

                // ============================
                // SearchBar
                // ============================
                var active by remember { mutableStateOf(false) }
                var query by remember { mutableStateOf(searchText) }

                SearchBar(
                    query = query,
                    onQueryChange = { newText ->
                        query = newText
                        notesViewModel.updateSearch(newText)
                    },
                    onSearch = {
                        notesViewModel.updateSearch(query)
                        active = false
                    },
                    active = active,
                    onActiveChange = { active = it },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("Search notes…") },
                    modifier = Modifier.fillMaxWidth()
                ) {}

                Spacer(Modifier.height(16.dp))

                Text(
                    "All Notes",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(12.dp))

                // ============================
                // NOTES LIST
                // ============================
                if (notes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No notes yet. Tap + to add.")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {

                        items(notes, key = { it.id }) { note ->

                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {

                                        val deletedNote = note
                                        notesViewModel.deleteNote(note.id)

                                        scope.launch {
                                            val res = snackbarHostState.showSnackbar(
                                                "Note deleted",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )

                                            if (res == SnackbarResult.ActionPerformed) {
                                                notesViewModel.addOrUpdateNote(
                                                    id = deletedNote.id,
                                                    title = deletedNote.title,
                                                    content = deletedNote.content,
                                                    color = deletedNote.color,
                                                    languageCode = deletedNote.languageCode,
                                                    pinned = deletedNote.pinned
                                                )
                                            }
                                        }
                                    }
                                    true
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFFE57373))
                                            .clip(MaterialTheme.shapes.medium),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            Icons.Default.Delete, null,
                                            tint = Color.White,
                                            modifier = Modifier.padding(end = 24.dp)
                                        )
                                    }
                                },
                                content = {
                                    NoteItem(
                                        note = note,
                                        onClick = {
                                            editingNote = note
                                            isDialogOpen = true
                                        },
                                        onDelete = { notesViewModel.deleteNote(note.id) },
                                        onTogglePin = { notesViewModel.togglePin(note) },
                                        onChangeLanguage = { targetLang ->
                                            notesViewModel.changeNoteLanguage(
                                                note,
                                                targetLanguageCode = targetLang
                                            ) { success ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        if (success) "Translated!" else "Translation failed"
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // ============================
            // ADD / EDIT DIALOG
            // ============================
            AddEditNoteDialog(
                isOpen = isDialogOpen,
                initialTitle = editingNote?.title.orEmpty(),
                initialContent = editingNote?.content.orEmpty(),
                initialColor = editingNote?.color,
                initialLanguage = editingNote?.languageCode ?: "en",
                speechController = speechController,

                onDismiss = { isDialogOpen = false },

                onConfirm = { ttl, cont, col, lang ->
                    notesViewModel.addOrUpdateNote(
                        id = editingNote?.id,
                        title = ttl,
                        content = cont,
                        color = col,
                        languageCode = lang,
                        pinned = editingNote?.pinned ?: false
                    )
                }
            )
        }
    }
}
