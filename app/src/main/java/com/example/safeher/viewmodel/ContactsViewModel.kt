package com.example.safeher.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.safeher.data.firebase.RealtimeDatabaseHelper
import com.example.safeher.data.model.Contact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ContactsViewModel : ViewModel() {
    private val dbHelper = RealtimeDatabaseHelper()

    sealed class ContactsState {
        object Idle : ContactsState()
        object Loading : ContactsState()
        object Success : ContactsState()
        data class Error(val message: String) : ContactsState()
    }

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts : StateFlow<List<Contact>> = _contacts

    private val _state = MutableStateFlow<ContactsState>(ContactsState.Idle)
    val state: StateFlow<ContactsState> = _state

    fun loadContacts(){
        _state.value = ContactsState.Loading
        dbHelper.getContacts(
            onResult = { list ->
                _contacts.value = list
                _state.value = ContactsState.Idle
            },
            onFailure = { e->
                _state.value = ContactsState.Error(e)
            }
        )

    }

    fun addContact(name: String, phone: String) {
        _state.value = ContactsState.Loading
        dbHelper.addContact(
            name = name,
            phone = phone,
            onSuccess = {
                loadContacts() // refresh list
            },
            onFailure = { error ->
                _state.value = ContactsState.Error(error)
            }
        )
    }

    fun deleteContact(contactId: String) {
        dbHelper.deleteContact(
            contactId = contactId,
            onSuccess = { loadContacts() },
            onFailure = { e ->
                _state.value = ContactsState.Error(e)
            }
        )
    }

}