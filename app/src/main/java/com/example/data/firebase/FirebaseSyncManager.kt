package com.example.data.firebase

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.data.model.AppShortcutItem
import com.example.data.model.QuickNote
import com.example.data.model.TaskItem
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class CloudSyncPayload(
    val wallpaperId: String? = null,
    val wallpaperDim: Float? = null,
    val tasks: List<TaskItem> = emptyList(),
    val notes: List<QuickNote> = emptyList(),
    val shortcuts: List<AppShortcutItem> = emptyList()
)

class FirebaseSyncManager(private val application: Application) {

    private val auth: FirebaseAuth = Firebase.auth

    // Resolves named Firestore Enterprise database ID from firebase_applet_config.xml
    private val databaseId: String = application.getString(R.string.firestore_database_id)
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(databaseId)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastCloudSyncTime = MutableStateFlow(0L)
    val lastCloudSyncTime: StateFlow<Long> = _lastCloudSyncTime.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUser.value = firebaseAuth.currentUser
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    fun cleanup() {
        auth.removeAuthStateListener(authListener)
    }

    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> = withContext(Dispatchers.Main) {
        val credentialManager = CredentialManager.create(context)
        val serverClientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else throw IllegalStateException("default_web_client_id resource not found")
        }

        val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    _currentUser.value = user
                    Result.success(user)
                } else {
                    Result.failure(IllegalStateException("Firebase user is null after sign in"))
                }
            } else {
                Result.failure(IllegalStateException("Unexpected credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("FirebaseSyncManager", "User cancelled Google Sign-In", e)
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.e("FirebaseSyncManager", "Credential Manager sign-in failed", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Authentication failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    suspend fun syncFullBackupToCloud(
        wallpaperId: String,
        wallpaperDim: Float,
        tasks: List<TaskItem>,
        notes: List<QuickNote>,
        shortcuts: List<AppShortcutItem>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext Result.failure(IllegalStateException("User not authenticated with Google"))
        _isSyncing.value = true

        try {
            val userRef = firestore.collection("users").document(user.uid)

            // 1. Sync User Profile & Wallpaper
            val userMap = mutableMapOf<String, Any>(
                "userId" to user.uid,
                "displayName" to (user.displayName ?: "User"),
                "email" to (user.email ?: ""),
                "wallpaperId" to wallpaperId,
                "wallpaperDim" to wallpaperDim.toDouble(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userRef.set(userMap, SetOptions.merge()).await()

            // 2. Sync Tasks
            val tasksColl = userRef.collection("tasks")
            for (task in tasks) {
                val taskDocId = "task_${task.id}"
                val taskMap = mutableMapOf<String, Any>(
                    "id" to taskDocId,
                    "userId" to user.uid,
                    "title" to task.title,
                    "description" to task.description,
                    "category" to task.category,
                    "priority" to task.priority,
                    "isCompleted" to task.isCompleted,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                tasksColl.document(taskDocId).set(taskMap, SetOptions.merge()).await()
            }

            // 3. Sync Notes
            val notesColl = userRef.collection("notes")
            for (note in notes) {
                val noteDocId = "note_${note.id}"
                val noteMap = mutableMapOf<String, Any>(
                    "id" to noteDocId,
                    "userId" to user.uid,
                    "title" to note.title,
                    "content" to note.content,
                    "tag" to note.tag,
                    "colorHex" to note.colorHex,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                notesColl.document(noteDocId).set(noteMap, SetOptions.merge()).await()
            }

            // 4. Sync Shortcuts
            val shortcutsColl = userRef.collection("shortcuts")
            for (shortcut in shortcuts) {
                val shortcutDocId = "shortcut_${shortcut.id}"
                val shortcutMap = mutableMapOf<String, Any>(
                    "id" to shortcutDocId,
                    "userId" to user.uid,
                    "name" to shortcut.name,
                    "urlOrPackage" to shortcut.urlOrPackage,
                    "iconType" to shortcut.iconType,
                    "category" to shortcut.category,
                    "isPinned" to shortcut.isPinned,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                shortcutsColl.document(shortcutDocId).set(shortcutMap, SetOptions.merge()).await()
            }

            val now = System.currentTimeMillis()
            _lastCloudSyncTime.value = now
            _isSyncing.value = false
            Result.success(Unit)
        } catch (e: Exception) {
            _isSyncing.value = false
            Log.e("FirebaseSyncManager", "Cloud sync failed", e)
            Result.failure(e)
        }
    }

    suspend fun pullAllFromCloud(): Result<CloudSyncPayload> = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext Result.failure(IllegalStateException("User not authenticated with Google"))
        _isSyncing.value = true

        try {
            val userRef = firestore.collection("users").document(user.uid)
            val userDoc = userRef.get().await()

            val wallpaperId = userDoc.getString("wallpaperId")
            val wallpaperDim = userDoc.getDouble("wallpaperDim")?.toFloat()

            // Fetch tasks
            val tasksSnapshot = userRef.collection("tasks").get().await()
            val tasks = tasksSnapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                TaskItem(
                    title = title,
                    description = doc.getString("description") ?: "",
                    category = doc.getString("category") ?: "Mobile",
                    priority = doc.getString("priority") ?: "Medium",
                    isCompleted = doc.getBoolean("isCompleted") ?: false
                )
            }

            // Fetch notes
            val notesSnapshot = userRef.collection("notes").get().await()
            val notes = notesSnapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                QuickNote(
                    title = title,
                    content = doc.getString("content") ?: "",
                    tag = doc.getString("tag") ?: "General",
                    colorHex = doc.getString("colorHex") ?: "#06B6D4"
                )
            }

            // Fetch shortcuts
            val shortcutsSnapshot = userRef.collection("shortcuts").get().await()
            val shortcuts = shortcutsSnapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val url = doc.getString("urlOrPackage") ?: return@mapNotNull null
                AppShortcutItem(
                    name = name,
                    urlOrPackage = url,
                    iconType = doc.getString("iconType") ?: "web",
                    category = doc.getString("category") ?: "Favorite",
                    isPinned = doc.getBoolean("isPinned") ?: true
                )
            }

            _isSyncing.value = false
            Result.success(
                CloudSyncPayload(
                    wallpaperId = wallpaperId,
                    wallpaperDim = wallpaperDim,
                    tasks = tasks,
                    notes = notes,
                    shortcuts = shortcuts
                )
            )
        } catch (e: Exception) {
            _isSyncing.value = false
            Log.e("FirebaseSyncManager", "Pull from cloud failed", e)
            Result.failure(e)
        }
    }
}
