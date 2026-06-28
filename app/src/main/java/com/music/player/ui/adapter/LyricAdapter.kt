package com.music.player.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.music.player.R
import com.music.player.databinding.ItemLyricLineBinding
import com.music.player.viewmodel.LyricLine

class LyricAdapter(private val lyrics: List<LyricLine>) : RecyclerView.Adapter<LyricAdapter.ViewHolder>() {

    private var highlightIndex = -1

    class ViewHolder(val binding: ItemLyricLineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLyricLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val line = lyrics[position]
        holder.binding.textLyricLine.text = line.text

        val isHighlighted = position == highlightIndex
        holder.binding.textLyricLine.setTextSize(
            android.util.TypedValue.COMPLEX_UNIT_SP,
            if (isHighlighted) 18f else 14f
        )
        holder.binding.textLyricLine.setTextColor(
            ContextCompat.getColor(
                holder.itemView.context,
                if (isHighlighted) R.color.primary else R.color.text_hint
            )
        )
    }

    override fun getItemCount(): Int = lyrics.size

    fun setHighlightIndex(index: Int) {
        if (index == highlightIndex) return
        val old = highlightIndex
        highlightIndex = index
        if (old >= 0) notifyItemChanged(old)
        if (index >= 0) notifyItemChanged(index)
    }
}
