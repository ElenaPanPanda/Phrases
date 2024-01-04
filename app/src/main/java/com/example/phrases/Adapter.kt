package com.example.phrases

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Adapter() : RecyclerView.Adapter<Adapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val phrase: TextView = view.findViewById(R.id.phrase_tv)
        val author: TextView = view.findViewById(R.id.author_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_phrase, parent, false)
        )
    }

    override fun getItemCount(): Int = PhrasesList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.phrase.text = PhrasesList[position].phrase
        holder.author.text = PhrasesList[position].author
    }
}