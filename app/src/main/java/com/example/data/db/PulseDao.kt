package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppShortcutItem
import com.example.data.model.QuickNote
import com.example.data.model.TaskItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PulseDao {
    // Tasks
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskItem>)

    @Update
    suspend fun updateTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    @Query("SELECT * FROM tasks ORDER BY id ASC")
    suspend fun getAllTasksList(): List<TaskItem>

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    // Quick Notes
    @Query("SELECT * FROM quick_notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<QuickNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: QuickNote): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<QuickNote>)

    @Update
    suspend fun updateNote(note: QuickNote)

    @Query("DELETE FROM quick_notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Long)

    @Query("SELECT * FROM quick_notes ORDER BY id ASC")
    suspend fun getAllNotesList(): List<QuickNote>

    @Query("DELETE FROM quick_notes")
    suspend fun clearAllNotes()

    // App & Link Shortcuts
    @Query("SELECT * FROM app_shortcuts ORDER BY isPinned DESC, createdAt ASC")
    fun getAllShortcuts(): Flow<List<AppShortcutItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: AppShortcutItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcuts(shortcuts: List<AppShortcutItem>)

    @Update
    suspend fun updateShortcut(shortcut: AppShortcutItem)

    @Query("DELETE FROM app_shortcuts WHERE id = :id")
    suspend fun deleteShortcutById(id: Long)

    @Query("SELECT * FROM app_shortcuts ORDER BY id ASC")
    suspend fun getAllShortcutsList(): List<AppShortcutItem>

    @Query("DELETE FROM app_shortcuts")
    suspend fun clearAllShortcuts()
}
