package week11.st856364.finalproject.data.model

import com.google.firebase.firestore.DocumentId

data class Note(
    @DocumentId val id: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
