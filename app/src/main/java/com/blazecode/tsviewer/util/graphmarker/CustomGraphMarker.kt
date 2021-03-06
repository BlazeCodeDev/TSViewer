package com.blazecode.tsviewer.util.graphmarker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.widget.TextView
import androidx.core.view.isVisible
import com.blazecode.tsviewer.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.*


class CustomGraphMarker(context: Context, layoutResource: Int):  MarkerView(context, layoutResource) {
    val text_date_time = findViewById<TextView>(R.id.textView_date_time)
    val text_names = findViewById<TextView>(R.id.textView_names)

    override fun refreshContent(entry: Entry?, highlight: Highlight?) {

        text_date_time.text = convertUnixToTime(entry!!.data.toString().split(";")[0].toLong())
        if(entry.data.toString().split(";")[1].split(",").isNotEmpty()) {
            text_names.text = entry.data.toString().split(";")[1]
        } else {
            text_names.isVisible = false
        }

        text_names.width = context.resources.configuration.densityDpi / 3 * 2                       //Two Thirds of the t
        super.refreshContent(entry, highlight)
    }

    override fun getOffsetForDrawingAtPoint(xpos: Float, ypos: Float): MPPointF {
        return MPPointF(-width / 2f, -height - 10f)
    }

    override fun draw(canvas: Canvas, posx: Float, posy: Float) {
        var posx = posx
        val size = Point()
        val width: Int = size.x
        val w = getWidth()
        val posy = posy / 4 * 3
        if (width - posx - w < w) {
            posx -= w.toFloat()
        }
        canvas.translate(posx, posy)
        draw(canvas)
    }

    private fun convertUnixToTime(unix: Long): String {
        val date = Date(unix)
        val simpleDateFormat = SimpleDateFormat("dd.MM.yy - HH:mm")
        simpleDateFormat.timeZone = TimeZone.getDefault()
        return simpleDateFormat.format(date)
    }
}