package com.example.contacts

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Insert fun insert(contact: Contact): Long
    @Update fun update(contact: Contact)
    @Delete fun delete(contact: Contact)
    @Query("SELECT * FROM contacts")
    fun getAll(): List<Contact>
    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    fun getById(id: Int): Contact?
    // --- sync helpers ---
    @Query("SELECT * FROM contacts WHERE dirty = 1")
    fun getDirty(): List<Contact>
    @Query("SELECT * FROM contacts WHERE firebaseId = :firebaseId LIMIT 1")
    fun findByFirebaseId(firebaseId: String): Contact?
    @Query("UPDATE contacts SET deleted = 1, dirty = 1, updatedAt = :now WHERE id = :id")
    fun softDelete(id: Int, now: Long = System.currentTimeMillis())
    @Query("UPDATE contacts SET dirty = 0, firebaseId = :firebaseId, version = :version, updatedAt = :updatedAt WHERE id = :id")
        fun markPushed(id: Int, firebaseId: String, version: Long, updatedAt: Long)
}