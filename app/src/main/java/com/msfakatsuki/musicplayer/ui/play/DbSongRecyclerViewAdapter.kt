package com.msfakatsuki.musicplayer.ui.play

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.msfakatsuki.musicplayer.database.music.RoomMusicItem
import com.msfakatsuki.musicplayer.databinding.FragmentDbsongItemBinding

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class DbSongRecyclerViewAdapter(
    // private val values: List<PlaceholderItem>
) : ListAdapter<RoomMusicItem,DbSongRecyclerViewAdapter.ViewHolder>(DbSongRecyclerViewAdapter.MusicItemComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentDbsongItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }


    inner class ViewHolder(binding: FragmentDbsongItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content
        val dbSongLayout: LinearLayout = binding.layoutDbsong

        var bindedSongItem : RoomMusicItem?=null



        init {
            dbSongLayout.setOnClickListener {
                binding.root.context
            }
        }

        fun bind(dbSongItem : RoomMusicItem) {
            bindedSongItem = dbSongItem
            idView.text = dbSongItem.title
            contentView.text = dbSongItem.artist
        }

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }

    }

    class MusicItemComparator : DiffUtil.ItemCallback<RoomMusicItem>() {
        override fun areItemsTheSame(oldItem: RoomMusicItem, newItem: RoomMusicItem): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(oldItem: RoomMusicItem, newItem: RoomMusicItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

}