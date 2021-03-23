package com.example.notefirebaseapp.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notefirebaseapp.R
import com.example.notefirebaseapp.model.Note
import com.example.notefirebaseapp.ui.adapter.NoteAdapter
import com.example.notefirebaseapp.ui.adapter.OnNoteClickLisener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_all_notes.*
import kotlinx.android.synthetic.main.add_dialoge_layout.*
import kotlinx.android.synthetic.main.add_dialoge_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AllNotesActivity : AppCompatActivity() {

    val noteCollectioRef = Firebase.firestore.collection("notes")
    val authRef = FirebaseAuth.getInstance()
    lateinit var curentUserId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_notes)



        intent.getStringExtra("userId")?.also {
            curentUserId = it
        }
        realTimeUpdates()


        allNotes_btn_add.setOnClickListener {
            createAddDialoge()
        }


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.note_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.note_menu_delete_all -> deleteAllNote()
            R.id.note_menu_sign_out -> {
                authRef.signOut()
                Intent(this, LoginActivity::class.java).also {
                    startActivity(it)
                }
                Toast.makeText(this, "Sign out successfully", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }


    fun deleteAllNote() {
        CoroutineScope(IO).launch {
            try {
                val noteQuery = noteCollectioRef.whereEqualTo("userId", curentUserId).get().await()
                for (doc in noteQuery) {
                    noteCollectioRef.document(doc.id).delete().await()
                }
                withContext(Main) {
                    Toast.makeText(this@AllNotesActivity, "All notes deleted", Toast.LENGTH_LONG)
                        .show()
                }

            } catch (e: Exception) {
                withContext(Main) {
                    Toast.makeText(this@AllNotesActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    fun deleteNote(title: String, desc: String) {
        CoroutineScope(IO).launch {
            try {
                val noteQuery = noteCollectioRef.whereEqualTo("title", title)
                    .whereEqualTo("desc", desc)
                    .whereEqualTo("userId", curentUserId)
                    .get().await()
                for (doc in noteQuery) {
                    noteCollectioRef.document(doc.id).delete().await()
                }
                withContext(Main) {
                    Toast.makeText(this@AllNotesActivity, "Note deleted", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                withContext(Main) {
                    Toast.makeText(this@AllNotesActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    fun realTimeUpdates() {
        noteCollectioRef.whereEqualTo("userId", curentUserId).addSnapshotListener { value, error ->
            error?.also {
                Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
            }
            value?.also {
                var notes = mutableListOf<Note>()
                for (doc in it) {
                    val note = doc.toObject<Note>()
                    notes.add(note)
                }
                setAdapter(notes)
            }


        }

    }

    fun getAllNotes() {
        CoroutineScope(IO).launch {
            try {
                val noteQuery = noteCollectioRef
                    .whereEqualTo("userId", curentUserId)
                    .get().await()
                noteQuery.documents
                var notes: MutableList<Note> = mutableListOf()
                for (doc in noteQuery.documents) {
                    val note = doc.toObject<Note>()
                    note?.also {
                        notes.add(it)
                    }
                }
                withContext(Main) {
                    setAdapter(notes)
                }

            } catch (e: Exception) {
                withContext(Main) {
                    Toast.makeText(this@AllNotesActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    fun updateNote(oldTitle: String, oldeDesc: String, newTilte: String, newDesc: String) =
        CoroutineScope(IO).launch {
            try {
                val noteQuery = noteCollectioRef.whereEqualTo("title", oldTitle)
                    .whereEqualTo("desc", oldeDesc)
                    .whereEqualTo("userId", curentUserId)
                    .get().await()
                for (i in noteQuery) {
                    val noteMap = mutableMapOf<String, Any>()
                    noteMap["title"] = newTilte
                    noteMap["desc"] = newDesc
                    noteCollectioRef.document(i.id).set(
                        noteMap, SetOptions.merge()
                    ).await()
                }
                withContext(Main) {
                    Toast.makeText(this@AllNotesActivity, "Note updated", Toast.LENGTH_LONG).show()
                }


            } catch (e: Exception) {
                withContext(Main) {
                    Toast.makeText(this@AllNotesActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }


        }

    fun setAdapter(notes: MutableList<Note>) {
        val adapter = NoteAdapter(notes, object : OnNoteClickLisener {
            override fun onUpdete(note: Note) {

                createUpdateDialoge(note.title, note.desc)
            }
        })
        allNotes_rv.adapter = adapter
        allNotes_rv.layoutManager = LinearLayoutManager(this)
    }

    fun createUpdateDialoge(oldTitle: String, oldDesc: String) {
        val view = layoutInflater.inflate(R.layout.add_dialoge_layout, null, false)
        val dialoge = AlertDialog.Builder(this).setView(view).show()
        view.dialoge_btn_add.text = "update"
        view.dialoge_et_title.setText(oldTitle)
        view.dialoge_et_desc.setText(oldDesc)
        view.dialoge_btn_delete.visibility = View.VISIBLE
        view.dialoge_btn_cancel.setOnClickListener {
            dialoge.dismiss()
        }
        view.dialoge_btn_delete.setOnClickListener {
            deleteNote(oldTitle, oldDesc)
            dialoge.dismiss()
        }
        view.dialoge_btn_add.setOnClickListener {
            val newTitle = view.dialoge_et_title.text.toString()
            val newDesc = view.dialoge_et_desc.text.toString()
            if (newTitle.isNotBlank() && newDesc.isNotBlank()) {
                updateNote(
                    oldTitle = oldTitle,
                    oldeDesc = oldDesc,
                    newTilte = newTitle,
                    newDesc = newDesc
                )
                dialoge.dismiss()
            } else Toast.makeText(this, "Enter title and description", Toast.LENGTH_SHORT).show()
        }
    }

    fun addNote(title: String, desc: String) {
        if (title.isNotBlank() && desc.isNotBlank()) {
            val note = Note(title, desc, curentUserId)
            CoroutineScope(IO).launch {
                try {
                    noteCollectioRef.add(note).await()
                    withContext(Main) {
                        Toast.makeText(
                            this@AllNotesActivity,
                            "Note add successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    withContext(Main) {
                        Toast.makeText(this@AllNotesActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }

            }

        } else {
            Toast.makeText(this, "Enter title and description", Toast.LENGTH_SHORT).show()
        }
    }

    fun createAddDialoge() {
        val view = layoutInflater.inflate(R.layout.add_dialoge_layout, null, false)
        val dialoge = AlertDialog.Builder(this).setView(view).show()

        view.dialoge_btn_cancel.setOnClickListener {
            dialoge.dismiss()
        }
        view.dialoge_btn_add.setOnClickListener {
            val title = view.dialoge_et_title.text.toString()
            val desc = view.dialoge_et_desc.text.toString()
            if (title.isNotBlank() && desc.isNotBlank()) {
                addNote(title, desc)
                dialoge.dismiss()
            } else
                Toast.makeText(this, "Enter title and description", Toast.LENGTH_SHORT).show()
        }
    }


}