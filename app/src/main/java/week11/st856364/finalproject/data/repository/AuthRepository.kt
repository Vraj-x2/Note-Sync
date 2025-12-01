package week11.st856364.finalproject.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // -----------------------------
    // SIGN IN
    // -----------------------------
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> =
        suspendCancellableCoroutine { cont ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        cont.resume(Result.success(user))
                    } else {
                        cont.resume(Result.failure(Exception("User is null after sign-in")))
                    }
                }
                .addOnFailureListener { e ->
                    cont.resume(Result.failure(e))
                }
        }

    // -----------------------------
    // SIGN UP
    // -----------------------------
    suspend fun signUp(email: String, password: String): Result<FirebaseUser> =
        suspendCancellableCoroutine { cont ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        cont.resume(Result.success(user))
                    } else {
                        cont.resume(Result.failure(Exception("User is null after sign-up")))
                    }
                }
                .addOnFailureListener { e ->
                    cont.resume(Result.failure(e))
                }
        }

    // -----------------------------
    // PASSWORD RESET
    // -----------------------------
    fun sendPasswordReset(email: String, onResult: (Result<Unit>) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    // -----------------------------
    // Re-Authenticate (helps for delete/update)
    // -----------------------------
    fun reAuthenticate(email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(Result.failure(Exception("No logged-in user")))
            return
        }

        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)

        user.reauthenticate(credential)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    // -----------------------------
    // SIGN OUT
    // -----------------------------
    fun signOut() {
        auth.signOut()
    }
}
