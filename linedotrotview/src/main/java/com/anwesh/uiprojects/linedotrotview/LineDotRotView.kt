package com.anwesh.uiprojects.linedotrotview

/**
 * Created by anweshmishra on 10/10/19.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Canvas
import android.graphics.Color

val nodes : Int = 5
val parts : Int = 3
val scGap : Float = 0.01f
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#673AB7")
val backColor : Int = Color.parseColor("#BDBDBD")
val rFactor : Float = 2.9f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Canvas.drawLineDot(i : Int, sc1 : Float, sc2 : Float, size : Float, paint : Paint) {
    val r : Float = size / rFactor
    val deg : Float = 90f / (parts - 1)
    save()
    rotate(deg * i)
    for (j in 0..1) {
        drawCircle(0f, -size * j * sc1.divideScale(i, parts), r, paint)
    }
    drawLine(0f, -r, 0f, -r - (size - 2 * r) * sc2, paint)
    restore()
}

fun Canvas.drawLineDots(sc1 : Float, sc2 : Float, size : Float, paint : Paint) {
    for (j in 0..(parts - 1)) {
        drawLineDot(j, sc1, sc2, size, paint)
    }
}

fun Canvas.drawLDRNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.style = Paint.Style.STROKE 
    save()
    translate(w / 2, gap * (i + 1))
    drawLineDots(sc1, sc2, size, paint)
    restore()
}
