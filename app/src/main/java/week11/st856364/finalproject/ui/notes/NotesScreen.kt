package week11.st856364.finalproject.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import week11.st856364.finalproject.data.model.Note
import week11.st856364.finalproject.navigation.Routes
import week11.st856364.finalproject.ui.speech.SpeechRecognizerController
import week11.st856364.finalproject.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    notesViewModel: NotesViewModel,
    uiState: UiState,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val notes by notesViewModel.filteredNotes.collectAsState()
    val searchText by notesViewModel.searchText.collectAsState()
    val userEmail by notesViewModel.userEmail.collectAsState()

    val context = LocalContext.current
    val speechController = remember { SpeechRecognizerController(context) }

    var isDialogOpen by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }

    // All vs pinned filter
    var showPinnedOnly by remember { mutableStateOf(false) }
    val displayedNotes = remember(notes, showPinnedOnly) {
        if (showPinnedOnly) notes.filter { it.pinned } else notes
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp)
            ) {
                // Drawer header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userEmail.firstOrNull()?.uppercase() ?: "U",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(userEmail, fontSize = 16.sp)
                }

                DrawerMenuItem("Profile", Icons.Default.Person) {
                    scope.launch { drawerState.close() }
                    onNavigate(Routes.PROFILE)
                }
                DrawerMenuItem("Change Password", Icons.Default.Lock) {
                    scope.launch { drawerState.close() }
                    onNavigate(Routes.CHANGE_PASSWORD)
                }
                DrawerMenuItem("Export Notes", Icons.Default.FileDownload) {
                    scope.launch { drawerState.close() }
                    onNavigate(Routes.EXPORT_NOTES)
                }
                DrawerMenuItem("Help / About", Icons.Default.Info) {
                    scope.launch { drawerState.close() }
                    onNavigate(Routes.HELP)
                }
                DrawerMenuItem("Logout", Icons.Default.ExitToApp) {
                    scope.launch { drawerState.close() }
                    onLogout()
                }
            }
        }
    ) {

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color(0xFFF7F3FF),

            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Hi, $userEmail", fontSize = 14.sp)
                            Text(
                                "Your Notes",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },

            floatingActionButton = {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        editingNote = null
                        isDialogOpen = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add note", tint = Color.White)
                }
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // SEARCH BAR – big rounded capsule
                var active by remember { mutableStateOf(false) }
                var query by remember { mutableStateOf(searchText) }

                SearchBar(
                    query = query,
                    onQueryChange = {
                        query = it
                        notesViewModel.updateSearch(it) // live filter
                    },
                    onSearch = {
                        notesViewModel.updateSearch(query)
                        active = false
                    },
                    active = active,
                    onActiveChange = { active = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text("Search notes…") },
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(26.dp)),
                    tonalElevation = 8.dp
                ) {
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilterTab(
                        text = "All Notes",
                        selected = !showPinnedOnly,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) { showPinnedOnly = false }

                    Spacer(Modifier.width(8.dp))

                    FilterTab(
                        text = "Pinned",
                        selected = showPinnedOnly,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) { showPinnedOnly = true }
                }

                Spacer(Modifier.height(16.dp))

                // NOTES LIST
                if (displayedNotes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (notes.isEmpty()) "No notes yet. Tap + to create one."
                            else "No notes match this view."
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(displayedNotes, key = { it.id }) { note ->

                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        val deletedNote = note
                                        notesViewModel.deleteNote(note.id)

                                        scope.launch {
                                            val res = snackbarHostState.showSnackbar(
                                                message = "Note deleted",
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
                                            .background(Color(0xFFE57373)),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            tint = Color.White,
                                            contentDescription = null,
                                            modifier = Modifier.padding(end = 22.dp)
                                        )
                                    }
                                },
                                content = {
                                    NoteCardUI(
                                        note = note,
                                        onClick = {
                                            editingNote = note
                                            isDialogOpen = true
                                        },
                                        onTogglePin = { notesViewModel.togglePin(note) },
                                        onTranslate = { targetLang ->
                                            notesViewModel.changeNoteLanguage(
                                                note,
                                                targetLanguageCode = targetLang
                                            ) { success ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        if (success) "Translated!"
                                                        else "Translation failed"
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

                if (uiState is UiState.Error) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

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
                    isDialogOpen = false
                }
            )
        }
    }
}

/* Drawer item helper */
@Composable
private fun DrawerMenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(title) },
        selected = false,
        icon = { Icon(icon, contentDescription = null) },
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}


@Composable
private fun FilterTab(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) {
        Brush.horizontalGradient(
            listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                Color.Transparent,
                Color.Transparent
            )
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Gray,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}


@Composable
fun NoteCardUI(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onTranslate: (String) -> Unit = {}   
) {
    var langMenuExpanded by remember { mutableStateOf(false) }

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

    val bgColor = Color(note.color.toInt())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        note.title.ifBlank { "Untitled" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        note.content,
                        maxLines = 4,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(6.dp))

                    if (note.pinned) {
                        Text(
                            text = "PINNED",
                            fontSize = 10.sp,
                            color = Color(0xFF4E342E),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFF59D))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "Pin",
                        tint = if (note.pinned) Color.Black else Color.DarkGray
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // TRANSLATE DROPDOWN (bigger and clearer)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.8f))
                    .clickable { langMenuExpanded = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Translate to:", fontSize = 12.sp, color = Color.DarkGray)
                Spacer(Modifier.width(6.dp))

                // Show current language name
                val currentLabel = languageOptions
                    .firstOrNull { it.second == note.languageCode }
                    ?.first ?: "Select"

                Text(
                    currentLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = langMenuExpanded,
                onDismissRequest = { langMenuExpanded = false }
            ) {
                languageOptions.forEach { (label, code) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            langMenuExpanded = false
                            onTranslate(code)   
                        }
                    )
                }
            }
        }
    }
}
