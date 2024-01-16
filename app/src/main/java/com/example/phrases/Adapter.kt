package com.example.phrases

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Adapter(
    data: List<Phrase>
) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    var data: List<Phrase> = data
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val quote: TextView = view.findViewById(R.id.phrase_tv)
        val author: TextView = view.findViewById(R.id.author_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_phrase, parent, false)
        )
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val phrase = data[position]
        holder.quote.text = phrase.quote
        holder.author.text = phrase.author
    }
}