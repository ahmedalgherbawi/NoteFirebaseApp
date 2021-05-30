package com.example.notefirebaseapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notefirebaseapp.R
import com.example.notefirebaseapp.model.Note
import kotlinx.android.synthetic.main.note_item_layout.view.*

class NoteAdapter(
    private val notes: MutableList<Note>,
    private val listener: Listener
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    interface Listener {
        fun onUpdate(note: Note)
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.note_item_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.itemView.apply {
            item_tv_title.text = note.title
            item_tv_desc.text = note.desc
            setOnClickListener {
                listener.onUpdate(note)
            }
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }

}
