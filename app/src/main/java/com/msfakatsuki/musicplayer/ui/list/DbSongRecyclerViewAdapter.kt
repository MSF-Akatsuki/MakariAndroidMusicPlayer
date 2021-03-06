package com.msfakatsuki.musicplayer.ui.list

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
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
) : ListAdapter<RoomMusicItem, DbSongRecyclerViewAdapter.ViewHolder>(MusicItemComparator()) {

    lateinit var parent:ViewGroup
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        this.parent=parent
        return ViewHolder(
            FragmentDbsongItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),parent
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    fun checkAll(){
        TODO("NOT YET IMPLEMENTED")
    }

    fun addAllToPlaylist() {
        this.currentList.forEach {
            addItemToPlayList(it)
        }
    }

    fun addItemToPlayList(item:RoomMusicItem?) {
        item?.let {
            val extra = Bundle()
            extra.putString("artist",it.artist)
            extra.putString("album",it.album)
            extra.putLong("id",it.id.toLong())
            Log.i("dbSRVAdap",it.localMediaUri?:"NULL")
            MediaControllerCompat.getMediaController(parent.context as Activity)?.addQueueItem(
                MediaDescriptionCompat.Builder().run {
                    setMediaUri(Uri.parse(it.localMediaUri))
                    setIconUri(Uri.parse(it.localIconUri?:""))
                    setTitle(it.title)
                    setExtras(extra)
                    setMediaId(it.id.toString())
                    build()
                }
            )
        }
    }

    inner class ViewHolder(binding: FragmentDbsongItemBinding,val parent: ViewGroup) :
        RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.title
        val artistTextView: TextView = binding.artist
        val albumTextView: TextView = binding.album
        val dbSongLayout = binding.layoutDbsong
        var bindedSongItem : RoomMusicItem?=null

        init {
            binding.layoutDbsong.setOnLongClickListener{
                it.showContextMenu()
            }
        }

        fun bind(dbSongItem : RoomMusicItem) {
            bindedSongItem = dbSongItem
            idView.text = dbSongItem.title
            artistTextView.text = dbSongItem.artist
            albumTextView.text = dbSongItem.album
        }

        public fun addToPlaylist() {
            this@DbSongRecyclerViewAdapter.addItemToPlayList(bindedSongItem)
        }




        override fun toString(): String {
            return super.toString() + " '" + artistTextView.text + "'"
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