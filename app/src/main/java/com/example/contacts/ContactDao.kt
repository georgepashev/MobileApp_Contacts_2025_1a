package com.example.contacts

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Insert
     fun insert(contact: Contact): Long

    @Update
     fun update(contact: Contact)

    @Delete
     fun delete(contact: Contact)

    @Query("SELECT * FROM contacts")
    fun getAll(): List<Contact>

    @Query("SELECT * from contacts WHERE id = :id LIMIT 1")
    fun getById(id: Int): Contact?
}