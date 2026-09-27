package com.example.safeher.ui.screens.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safeher.data.model.Contact
import com.example.safeher.utils.NetworkUtils
import com.example.safeher.viewmodel.ContactsViewModel


@Composable
fun ContactsScreen( contactsViewModel: ContactsViewModel = viewModel()){
    var name by rememberSaveable  { mutableStateOf("") }
    var phone by rememberSaveable  { mutableStateOf("") }
    var nameError by rememberSaveable  { mutableStateOf("") }
    var phoneError by rememberSaveable  { mutableStateOf("") }
    var contactToDelete by remember { mutableStateOf<Contact?>(null) }

    val contacts by contactsViewModel.contacts.collectAsState()
    val state by contactsViewModel.state.collectAsState()

    var showNoInternetDialog by rememberSaveable  {
        mutableStateOf(false)
    }
    val context = LocalContext.current


    // Load contacts when screen opens
    LaunchedEffect(Unit) {
        if (NetworkUtils.isInternetAvailable(context)) {
            contactsViewModel.loadContacts()
        } else {
            showNoInternetDialog = true
        }
    }

    if (contactToDelete != null) {
        AlertDialog(
            onDismissRequest = {contactToDelete = null},
            title = { Text("Delete Contact") },
            text = { Text("Are you sure you want to delete ${contactToDelete!!.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!NetworkUtils.isInternetAvailable(context)) {
                            showNoInternetDialog = true
                            return@TextButton
                        }
                        contactsViewModel.deleteContact(contactToDelete!!.id)
                        contactToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { contactToDelete = null }) {
                    Text("Cancel")
                }
            }

        )
    }

    if (showNoInternetDialog) {

        AlertDialog(
            onDismissRequest = {
                showNoInternetDialog = false
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },

            title = {
                Text(
                    text = "No Internet Connection",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Text(
                    "HerBeacon couldn't connect to the internet. Please check your connection and try again."
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showNoInternetDialog = false
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }





    fun validate(): Boolean {
        var valid = true
        if (name.isBlank()) {
            nameError = "Name cannot be empty"
            valid = false
        } else if (!name.trim().all { it.isLetter() || it.isWhitespace() }) {
            nameError = "Name cannot contain special characters"
            valid = false
        } else {
            nameError = ""
        }

        if (phone.isBlank()) {
            phoneError = "Phone cannot be empty"
            valid = false
        } else if (phone.trim().length < 10 || phone.trim().length > 10) {
            phoneError = "Enter a valid phone number"
            valid = false
        } else {
            phoneError = ""
        }
        return valid
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ){ padding->
        Column(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Emergency Contacts",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "These people will be alerted when you press SOS",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = ""
                },
                label = { Text("Contact Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError.isNotEmpty(),
                supportingText = {
                    if (nameError.isNotEmpty()) {
                        Text(text = nameError, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    phoneError = ""
                },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = phoneError.isNotEmpty(),
                supportingText = {
                    if (phoneError.isNotEmpty()) {
                        Text(text = phoneError, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (state is ContactsViewModel.ContactsState.Error) {
                Text(
                    text = (state as ContactsViewModel.ContactsState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
                onClick = {
                    if (!NetworkUtils.isInternetAvailable(context)) {
                        showNoInternetDialog = true
                        return@Button
                    }
                    if (validate()) {
                        contactsViewModel.addContact(name.trim(), phone.trim())
                        name = ""
                        phone = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is ContactsViewModel.ContactsState.Loading
            ) {
                if (state is ContactsViewModel.ContactsState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Add Contact")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (contacts.isEmpty()) {
                Text(
                    text = "No contacts added yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Column{
                   contacts.forEach{ contact ->
                        ContactItem(
                            contact = contact,
                            onDelete = { contactToDelete = contact }
                        )

                    }
                }
            }
        }

    }









    }
    @Composable
    fun ContactItem(contact: Contact, onDelete: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = contact.name, fontWeight = FontWeight.Bold)
                    Text(
                        text = contact.phone,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete contact",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
}