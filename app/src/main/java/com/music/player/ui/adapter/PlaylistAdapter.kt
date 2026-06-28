package com.music.player.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.music.player.data.model.Playlist
import com.music.player.databinding.ItemPlaylistBinding

class PlaylistAdapter(
    private val playlists: List<Playlist>,
    private val onClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.binding.textName.text = playlist.name
        holder.binding.textPlayCount.text = formatPlayCount(playlist.playCount)
        Glide.with(holder.itemView.context).load(playlist.coverImgUrl).into(holder.binding.imageCover)
        holder.itemView.setOnClickListener { onClick(playlist) }
    }

    override fun getItemCount(): Int = playlists.size

    private fun formatPlayCount(count: Long): String {
        return when {
            count >= 100_000_000 -> String.format("%.1f亿", count / 100_000_000.0)
            count >= 10_000 -> String.format("%.1f万", count / 10_000.0)
            else -> count.toString()
        }
    }
}
