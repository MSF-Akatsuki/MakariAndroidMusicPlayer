package com.msfakatsuki.musicplayer.ui.play

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.msfakatsuki.musicplayer.databinding.FragmentItemBinding

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class SongRecyclerViewAdapter() :
    ListAdapter<MediaSessionCompat.QueueItem,SongRecyclerViewAdapter.ViewHolder>(
        SongRecyclerViewAdapter.MediaDescriptionCompatComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.idView.text = item.description.title
        holder.albumTextView.text = item.description.extras?.getString("album")?:"NULL"
        holder.artistTextView.text = item.description.extras?.getString("artist")?:"NULL"
    }


    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.title
        val artistTextView: TextView = binding.artist
        val albumTextView: TextView = binding.album

        override fun toString(): String {
            return super.toString()
        }
    }

    class MediaDescriptionCompatComparator : DiffUtil.ItemCallback<MediaSessionCompat.QueueItem>() {
        override fun areItemsTheSame(
            oldItem: MediaSessionCompat.QueueItem,
            newItem: MediaSessionCompat.QueueItem
        ): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(
            oldItem: MediaSessionCompat.QueueItem,
            newItem: MediaSessionCompat.QueueItem
        ): Boolean {
            return oldItem.hashCode()==newItem.hashCode()
        }

    }
}