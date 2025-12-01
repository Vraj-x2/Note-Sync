package week11.st856364.finalproject.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import week11.st856364.finalproject.data.model.Note
import week11.st856364.finalproject.data.repository.NotesRepository
import week11.st856364.finalproject.network.GoogleTranslateService
import week11.st856364.finalproject.utils.UiState

class NotesViewModel(
    private val notesRepository: NotesRepository = NotesRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail

    private var listenerRegistration: ListenerRegistration? = null

    // Search filtering
    val filteredNotes = combine(_notes, _searchText) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.title.contains(query, true) || it.content.contains(query, true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun startListening() {
        stopListening()

        _userEmail.value = auth.currentUser?.email.orEmpty()

        listenerRegistration = notesRepository.listenToNotes(
            onNotesChanged = { list ->
                _notes.value = list.sortedWith(
                    compareByDescending<Note> { it.pinned }.thenByDescending { it.timestamp }
                )
            },
            onError = { e ->
                _uiState.value = UiState.Error(e.message ?: "Failed to load notes")
            }
        )
    }


    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    /** Update search query */
    fun updateSearch(text: String) {
        _searchText.value = text
    }

    /** Add or update note */
    fun addOrUpdateNote(
        id: String?,
        title: String,
        content: String,
        color: Long,
        languageCode: String,
        pinned: Boolean
    ) {
        if (title.isBlank() && content.isBlank()) {
            _uiState.value = UiState.Error("Note cannot be empty")
            return
        }

        _uiState.value = UiState.Loading

        val note = Note(
            id = id ?: "",
            title = title.ifBlank { "Untitled" },
            content = content,
            color = color,
            pinned = pinned,
            languageCode = languageCode,
            timestamp = System.currentTimeMillis()
        )

        val callback: (Result<Unit>) -> Unit = {
            it.onSuccess {
                _uiState.value = UiState.Success("Saved")
                resetUiState()
            }.onFailure { e ->
                _uiState.value = UiState.Error(e.message ?: "Failed to save note")
            }
        }

        if (id.isNullOrBlank()) notesRepository.addNote(note, callback)
        else notesRepository.updateNote(note, callback)
    }

    /** Delete note */
    fun deleteNote(id: String) {
        _uiState.value = UiState.Loading
        notesRepository.deleteNote(id) { result ->
            result.onSuccess {
                _uiState.value = UiState.Success("Deleted")
                resetUiState()
            }.onFailure { e ->
                _uiState.value = UiState.Error(e.message ?: "Failed to delete note")
            }
        }
    }

    /** Toggle pin */
    fun togglePin(note: Note) {
        addOrUpdateNote(
            id = note.id,
            title = note.title,
            content = note.content,
            color = note.color,
            languageCode = note.languageCode,
            pinned = !note.pinned
        )
    }


    fun changeNoteLanguage(
        note: Note,
        targetLanguageCode: String,
        onResult: (Boolean) -> Unit
    ) {
        GoogleTranslateService.translate(
            text = note.content,
            targetLang = targetLanguageCode
        ) { translated ->
            if (translated.isNullOrBlank()) {
                onResult(false)
                return@translate
            }

            val updatedNote = note.copy(
                content = translated,
                languageCode = targetLanguageCode,
                timestamp = System.currentTimeMillis()
            )

            notesRepository.updateNote(updatedNote) {
                onResult(it.isSuccess)
            }
        }
    }

    private fun resetUiState() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(250)
            _uiState.value = UiState.Idle
        }
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}
