package com.example.contacts
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder

import com.example.contacts.AppDatabase
import com.example.contacts.AppDataStore
import com.example.contacts.SyncManager

import java.util.concurrent.TimeUnit

class SyncWorker(appCtx: Context, params: WorkerParameters)
    : CoroutineWorker(appCtx,  params) {

    override suspend fun doWork(): Result = try {
        val db = AppDatabase.get(applicationContext)
        val dao = db.contactDao()
        val sync = SyncManager(dao)
// AppDataStore is your wrapper for DataStore<Long>

        val ds = AppDataStore(applicationContext)
        when (sync.syncOnce(ds).isSuccess) {
            true -> Result.success()
            else -> Result.retry()
        }
    } catch (e: Exception) { Result.retry() }
}
fun schedulePeriodicSync(context: Context) {
    val req = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.MINUTES)

        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "cloud-sync", ExistingPeriodicWorkPolicy.KEEP, req
    )
}
fun enqueueOneTimeSync(context: Context) {
    val once = OneTimeWorkRequestBuilder<SyncWorker>().build()
    WorkManager.getInstance(context).enqueue(once)
}