package com.music.player.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.music.player.R
import com.music.player.data.model.Song
import com.music.player.databinding.ItemSongBinding
import com.music.player.viewmodel.MusicViewModel

class SongAdapter(
    private val songs: List<Song>,
    private val viewModel: MusicViewModel,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    private var currentSongId: Long = -1

    class ViewHolder(val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]
        holder.binding.textIndex.text = "${position + 1}"
        holder.binding.textSongName.text = song.name
        holder.binding.textArtist.text = song.artistNames
        holder.binding.textDuration.text = song.durationFormatted

        val isActive = song.id == currentSongId
        holder.binding.textSongName.setTextColor(
            ContextCompat.getColor(
                holder.itemView.context,
                if (isActive) R.color.primary else R.color.text_primary
            )
        )

        holder.itemView.setOnClickListener { onClick(position) }
    }

    override fun getItemCount(): Int = songs.size

    fun setCurrentSongId(id: Long?) {
        val old = currentSongId
        currentSongId = id ?: -1
        songs.forEachIndexed { index, song ->
            if (song.id == old || song.id == currentSongId) {
                notifyItemChanged(index)
            }
        }
    }
}
