package week11.st856364.finalproject

import android.app.Application
import com.google.firebase.FirebaseApp

class NoteSyncApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
