package com.msfakatsuki.musicplayer.ui.list

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.msfakatsuki.musicplayer.database.music.SingleStringObject
import com.msfakatsuki.musicplayer.databinding.FragmentMusicSearchBinding

import com.msfakatsuki.musicplayer.ui.play.placeholder.PlaceholderContent.PlaceholderItem

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MusicSearchRecyclerViewAdapter(
) : ListAdapter<SingleStringObject, MusicSearchRecyclerViewAdapter.ViewHolder>(SingleStringComparator()) {

    var toggleButtonStateList : MutableList<Boolean>?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentMusicSearchBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item.toString())
    }

    override fun submitList(list: List<SingleStringObject>?) {
        super.submitList(list)
        list?.let {
            toggleButtonStateList = MutableList(it.size,{false})
        }
    }

    fun getSelectedList() : List<SingleStringObject> {
        val retList = mutableListOf<SingleStringObject>()
        toggleButtonStateList?.forEachIndexed { index, b ->
            if (b) {
                retList.add(getItem(index))
            }
        }
        return retList
    }

    inner class ViewHolder(val binding: FragmentMusicSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber

        override fun toString(): String {
            return super.toString()
        }

        fun bind(text: String) {
            binding.itemNumber.text=text
            binding.btnCheckSelect.isChecked = this@MusicSearchRecyclerViewAdapter.toggleButtonStateList?.get(absoluteAdapterPosition)
                ?:false
            binding.btnCheckSelect.setOnClickListener{
                val view = it as CheckBox
                this@MusicSearchRecyclerViewAdapter.toggleButtonStateList?.set(absoluteAdapterPosition,view.isChecked)
            }
        }


    }

    class SingleStringComparator : DiffUtil.ItemCallback<SingleStringObject>() {
        override fun areItemsTheSame(oldItem: SingleStringObject, newItem: SingleStringObject): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(oldItem: SingleStringObject, newItem: SingleStringObject): Boolean {
            return oldItem.toString()==newItem.toString()
        }

    }



}