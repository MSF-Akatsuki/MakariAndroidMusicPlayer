package com.msfakatsuki.musicplayer.util.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.ContextMenu
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.msfakatsuki.musicplayer.R

/**
 * TODO: document your custom view class.
 */
class ContextMenuRecyclerView(context:Context,attrs:AttributeSet) : RecyclerView(context,attrs) {

    var mContextMenuInfo = RecyclerViewContextMenuInfo()

    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo {
        Log.i("gg","getContextMenuInfo")
        return mContextMenuInfo
    }


    override fun showContextMenuForChild(originalView: View?): Boolean {
        Log.i("gg","showContextMenuForChild")
        val position = originalView?.let { getChildAdapterPosition(it) }?:-1
        if (position >= 0) {
            mContextMenuInfo.setData(position)
            return super.showContextMenuForChild(originalView)
        }
        return false
    }

    class RecyclerViewContextMenuInfo(
        var position:Int=-1,
    ):ContextMenu.ContextMenuInfo {

        fun setData(position: Int) {
            this.position=position
        }
    }

}