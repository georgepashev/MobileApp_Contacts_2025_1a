package com.example.contacts
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.launch

class ContactViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).contactDao()

    val contacts = dao.getAll().asLiveData()

    fun add(contact: Contact) = viewModelScope.launch { dao.insert(contact) }
    fun update(contact: Contact) = viewModelScope.launch { dao.update(contact) }
    fun delete(contact: Contact) = viewModelScope.launch { dao.delete(contact) }
}