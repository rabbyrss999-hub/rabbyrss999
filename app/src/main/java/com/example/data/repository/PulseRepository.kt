package com.example.data.repository

import com.example.data.db.PulseDao
import com.example.data.model.QuickNote
import com.example.data.model.TaskItem
import kotlinx.coroutines.flow.Flow

class PulseRepository(private val dao: PulseDao) {
    val allTasks: Flow<List<TaskItem>> = dao.getAllTasks()
    val allNotes: Flow<List<QuickNote>> = dao.getAllNotes()

    suspend fun insertTask(task: TaskItem): Long = dao.insertTask(task)

    suspend fun updateTask(task: TaskItem) = dao.updateTask(task)

    suspend fun deleteTask(taskId: Long) = dao.deleteTaskById(taskId)

    suspend fun insertNote(note: QuickNote): Long = dao.insertNote(note)

    suspend fun updateNote(note: QuickNote) = dao.updateNote(note)

    suspend fun deleteNote(noteId: Long) = dao.deleteNoteById(noteId)
}
