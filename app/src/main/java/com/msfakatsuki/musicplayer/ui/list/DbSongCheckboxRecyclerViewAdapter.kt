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
import android.widget.CheckBox
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.msfakatsuki.musicplayer.database.music.RoomMusicItem
import com.msfakatsuki.musicplayer.databinding.FragmentDbSongCheckBinding

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class DbSongCheckboxRecyclerViewAdapter(
) : ListAdapter<RoomMusicItem, DbSongCheckboxRecyclerViewAdapter.ViewHolder>(DbSongCheckboxRecyclerViewAdapter.MusicItemComparator()) {

    lateinit var parent:ViewGroup
    var toggleButtonStateList:MutableList<Boolean>?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        this.parent=parent
        return ViewHolder(
            FragmentDbSongCheckBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),parent
        )

    }

    fun addSelectedToPlaylist(){
        toggleButtonStateList?.forEachIndexed { index, b ->
            if (b) {
                addItemToPlaylist(getItem(index))
            }
        }
    }

    fun addItemToPlaylist(item:RoomMusicItem?) {
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

    fun checkAll(){
        TODO("NOT YET IMPLEMENTED")
    }

    override fun submitList(list: List<RoomMusicItem>?) {
        super.submitList(list)
        list?.let {
            toggleButtonStateList = MutableList(it.size,{false})
        }
    }

    fun getSelectedList() : List<RoomMusicItem> {
        val retList = mutableListOf<RoomMusicItem>()
        toggleButtonStateList?.forEachIndexed { index, b ->
            if (b) {
                retList.add(getItem(index))
            }
        }
        return retList
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(binding: FragmentDbSongCheckBinding,val parent: ViewGroup) :
        RecyclerView.ViewHolder(binding.root) {

        val tvTitle = binding.title
        val tvAlbum = binding.album
        val tvArtist = binding.artist
        val checkbox = binding.btnCheckSelect

        var bindedSongItem : RoomMusicItem?=null

        init {
            binding.layoutDbsongCheck.setOnLongClickListener{
                it.showContextMenu()
            }
        }

        override fun toString(): String {
            return super.toString() + " '"
        }

        fun bind(dbSongItem : RoomMusicItem) {
            bindedSongItem = dbSongItem
            tvTitle.text = dbSongItem.title
            tvArtist.text = dbSongItem.artist
            tvAlbum.text = dbSongItem.album

            checkbox.isChecked = this@DbSongCheckboxRecyclerViewAdapter.toggleButtonStateList?.get(absoluteAdapterPosition)
                ?:false
            checkbox.setOnClickListener {
                val view = it as CheckBox
                this@DbSongCheckboxRecyclerViewAdapter.toggleButtonStateList?.set(absoluteAdapterPosition,view.isChecked)
            }
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