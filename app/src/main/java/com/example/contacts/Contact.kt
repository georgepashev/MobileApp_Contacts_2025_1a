package com.example.contacts

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val photoUrl: String?,
    val photoPath: String?,
// --- sync fields ---
    val firebaseId: String? = null, // Firestore document id (null until first push)
    val version: Long = 0L, // increment on every local change
    val updatedAt: Long = System.currentTimeMillis(),
    val dirty: Boolean = true, // mark local changes for push
    val deleted: Boolean = false // soft delete
)