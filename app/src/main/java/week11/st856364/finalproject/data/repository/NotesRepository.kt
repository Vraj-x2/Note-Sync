package week11.st856364.finalproject.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import week11.st856364.finalproject.data.model.Note

class NotesRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun notesCollectionPath(): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be logged in")
        // users/{uid}/notes
        return "users/$uid/notes"
    }

    fun listenToNotes(
        onNotesChanged: (List<Note>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration {
        return db.collection(notesCollectionPath())
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(Note::class.java) ?: emptyList()
                onNotesChanged(list)
            }
    }

    fun addNote(note: Note, onResult: (Result<Unit>) -> Unit) {
        db.collection(notesCollectionPath())
            .add(note)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    fun updateNote(note: Note, onResult: (Result<Unit>) -> Unit) {
        if (note.id.isBlank()) {
            onResult(Result.failure(IllegalArgumentException("Note id is empty")))
            return
        }
        db.collection(notesCollectionPath())
            .document(note.id)
            .set(note)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    fun deleteNote(id: String, onResult: (Result<Unit>) -> Unit) {
        if (id.isBlank()) {
            onResult(Result.failure(IllegalArgumentException("Note id is empty")))
            return
        }
        db.collection(notesCollectionPath())
            .document(id)
            .delete()
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }
}
