package com.example.quanlychitieu.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.quanlychitieu.model.Note

class NotesViewModel : ViewModel() {
    private val dbRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("notes")

    private val _notesByDate = MutableStateFlow<Map<String, List<Note>>>(emptyMap())
    val notesByDate: StateFlow<Map<String, List<Note>>> = _notesByDate

    init {
        fetchNotes()
    }

    private fun fetchNotes() {
        viewModelScope.launch {
            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val map = mutableMapOf<String, List<Note>>()
                    for (dateSnap in snapshot.children) {
                        val noteList = mutableListOf<Note>()
                        for (noteSnap in dateSnap.children) {
                            val note = noteSnap.getValue(Note::class.java)
                            if (note != null) noteList.add(note)
                        }
                        map[dateSnap.key ?: ""] = noteList
                    }
                    _notesByDate.value = map
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
}

