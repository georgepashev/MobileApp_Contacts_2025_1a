package com.example.contacts

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

object Cloud {
    val db: FirebaseFirestore by lazy {
        val inst = FirebaseFirestore.getInstance()
        inst.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        inst
    }
    val contacts get() = db.collection("contacts")
}
fun Contact.toCloudMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "phone" to phone,
    "email" to email,

    "address" to address,
    "photoUrl" to photoUrl,
    "photoPath" to photoPath,
    "version" to version,
    "deleted" to deleted,
    "updatedAt" to FieldValue.serverTimestamp(),
    "localId" to id
)
data class CloudContact(
    val id: String, // Firestore docId
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val photoUrl: String?,
    val photoPath: String?,
    val version: Long,
    val deleted: Boolean,
    val updatedAt: Long
)
fun DocumentSnapshot.toCloudContact(): CloudContact? {
    val name = getString("name") ?: return null
    val phone = getString("phone") ?: return null
    val email = getString("email") ?: return null
    val address = getString("address") ?: return null
    val photoUrl = getString("photoUrl")
    val photoPath = getString("photoPath")
    val version = getLong("version") ?: 0L
    val deleted = getBoolean("deleted") ?: false
    val ts = getTimestamp("updatedAt")?.toDate()?.time ?: 0L
    return CloudContact(id, name, phone, email, address, photoUrl, photoPath, version, deleted,
        ts)
}