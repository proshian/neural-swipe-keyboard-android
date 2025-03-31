package com.example.neuralSwipeKeyboardProject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CandidateAdapter(
    private var candidates: List<String> = emptyList(),
    private val onCandidateSelected: (String) -> Unit = {}
) : RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder>() {

    class CandidateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.candidate_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_candidate, parent, false)
        return CandidateViewHolder(view)
    }

    override fun onBindViewHolder(holder: CandidateViewHolder, position: Int) {
        holder.textView.text = candidates[position]
        holder.itemView.setOnClickListener {
            onCandidateSelected(candidates[position])
        }
    }

    override fun getItemCount() = candidates.size

    fun updateCandidates(newCandidates: List<String>) {
        candidates = newCandidates
        notifyDataSetChanged()
    }
}