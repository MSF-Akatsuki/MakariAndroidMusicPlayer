package com.msfakatsuki.musicplayer.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class ProgressBarView(context: Context,attrs: AttributeSet) : View(context,attrs)  {

    public var currentPosition : Long = 0L
    public var duration : Long = 0L

    public val progress: Float get() {
        val tmp = if (duration==0L) 0F else currentPosition * 1.0F / duration
        return if (tmp < 0F) 0F
            else if (tmp > 1F) 1F
            else tmp
    }
    private val paintBar= Paint()
    private val paintCircle = Paint()

    init {
        paintBar.isAntiAlias = true
        paintBar.style = Paint.Style.STROKE
        paintBar.color = Color.parseColor("#7F3F00")
        paintBar.strokeWidth = 5F

        paintCircle.isAntiAlias = true
        paintCircle.style = Paint.Style.FILL_AND_STROKE
        paintCircle.color = Color.parseColor("#003F70")
        paintCircle.strokeWidth = 1F
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.drawLine(
                0F,height/2F,
                progress*width,height/2F,
                paintBar
            )
            it.drawCircle(progress*width,height/2F,4.3F,paintCircle)
        }
    }

}