package com.music.player.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.music.player.data.model.Banner
import com.music.player.databinding.ItemBannerBinding

class BannerAdapter(private val banners: List<Banner>) : RecyclerView.Adapter<BannerAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemBannerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val banner = banners[position]
        Glide.with(holder.itemView.context).load(banner.imageUrl).into(holder.binding.imageBanner)
    }

    override fun getItemCount(): Int = banners.size
}
