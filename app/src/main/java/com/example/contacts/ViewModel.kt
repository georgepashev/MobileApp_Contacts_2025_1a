package com.example.contacts
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.launch

class ContactViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).contactDao()

    val contacts = dao.getAll()

    fun add(contact: Contact) = viewModelScope.launch {
        var t = Thread{ dao.insert(contact)}
        t.start()

    }
    fun update(contact: Contact) = viewModelScope.launch {
        var t = Thread{ dao.update(contact) }
        t.start()
    }
    fun delete(contact: Contact) = viewModelScope.launch {
        var t = Thread{dao.delete(contact)}
        t.start()
    }
}