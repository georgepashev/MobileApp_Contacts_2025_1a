package com.example.contacts


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

import com.example.contacts.Contact
import com.example.contacts.ContactDao
import com.example.contacts.AppDataStore
import com.google.firebase.Timestamp
import java.util.Date


class SyncManager(private val dao: ContactDao) {
    // requires: implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
    suspend fun pushDirty() = withContext(Dispatchers.IO) {
        val dirty = dao.getDirty()
        for (e in dirty) {
            if (e.firebaseId == null) {
                val docRef = Cloud.contacts.document()
                Cloud.db.runTransaction { tx ->
                    tx.set(docRef, e.toCloudMap())
                }.await()
                val snap = docRef.get().await()
                val cc = snap.toCloudContact() ?: continue
                dao.markPushed(e.id, docRef.id, cc.version, cc.updatedAt)
            } else {
                val docRef = Cloud.contacts.document(e.firebaseId)
                Cloud.db.runTransaction { tx ->
                    val cur = tx.get(docRef)
                    val curVer = cur.getLong("version") ?: 0L
                    if (e.version >= curVer) tx.update(docRef, e.toCloudMap())
                }.await()
                val snap = docRef.get().await()
                val cc = snap.toCloudContact() ?: continue
                dao.markPushed(e.id, docRef.id, cc.version, cc.updatedAt)
            }
        }
    }
    suspend fun pullSince(lastSyncAt: Long): Long = withContext(Dispatchers.IO) {
        var maxSeen = lastSyncAt
        val q = Cloud.contacts.whereGreaterThan("updatedAt", Timestamp(Date(lastSyncAt)))
        val snaps = q.get().await()
        for (doc in snaps.documents) {
            val cc = doc.toCloudContact() ?: continue
            maxSeen = longArrayOf(maxSeen, cc.updatedAt).max()
            val local = dao.findByFirebaseId(doc.id)
            if (local == null || cc.version > local.version) {
                val e = Contact(
                    id = local?.id ?: 0,
                    name = cc.name,
                    phone = cc.phone,
                    email = cc.email,
                    address = cc.address,
                    photoUrl = cc.photoUrl,
                    photoPath = cc.photoPath,

                    firebaseId = doc.id,
                    version = cc.version,
                    updatedAt = cc.updatedAt,
                    deleted = cc.deleted,
                    dirty = false
                )
                if (local == null) dao.insert(e) else dao.update(e.copy(id = local.id))
            }
        }
        maxSeen
    }
    suspend fun syncOnce(ds: AppDataStore): Result<Unit> = try {
        pushDirty()
        val last = ds.getLong("lastSyncAt", 0L)
        val newLast = pullSince(last)
        ds.putLong("lastSyncAt", newLast)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}