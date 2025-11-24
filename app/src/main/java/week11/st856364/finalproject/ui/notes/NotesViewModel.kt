package week11.st856364.finalproject.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import week11.st856364.finalproject.data.model.Note
import week11.st856364.finalproject.data.repository.NotesRepository
import week11.st856364.finalproject.utils.UiState

class NotesViewModel(
    private val notesRepository: NotesRepository = NotesRepository()
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private var listenerRegistration: ListenerRegistration? = null

    fun startListening() {
        if (listenerRegistration != null) return  // already listening
        listenerRegistration = notesRepository.listenToNotes(
            onNotesChanged = { list -> _notes.value = list },
            onError = { e -> _uiState.value = UiState.Error(e.message ?: "Error loading notes") }
        )
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    fun addOrUpdateNote(
        id: String?,
        title: String,
        content: String
    ) {
        if (title.isBlank() && content.isBlank()) {
            _uiState.value = UiState.Error("Note is empty")
            return
        }
        _uiState.value = UiState.Loading
        val note = Note(
            id = id ?: "",
            title = title.ifBlank { "Untitled" },
            content = content,
            timestamp = System.currentTimeMillis()
        )
        val callback: (Result<Unit>) -> Unit = { result ->
            result
                .onSuccess { _uiState.value = UiState.Success() }
                .onFailure { e ->
                    _uiState.value =
                        UiState.Error(e.message ?: "Failed to save note")
                }
        }

        if (id.isNullOrBlank()) {
            notesRepository.addNote(note, callback)
        } else {
            notesRepository.updateNote(note, callback)
        }
    }

    fun deleteNote(id: String) {
        _uiState.value = UiState.Loading
        notesRepository.deleteNote(id) { result ->
            result
                .onSuccess { _uiState.value = UiState.Success() }
                .onFailure { e ->
                    _uiState.value =
                        UiState.Error(e.message ?: "Failed to delete note")
                }
        }
    }
}
