package com.anwesh.uiprojects.linedotrotview

/**
 * Created by anweshmishra on 10/10/19.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val nodes : Int = 5
val parts : Int = 5
val finalDeg : Float = 180f
val scGap : Float = 0.01f
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#673AB7")
val backColor : Int = Color.parseColor("#BDBDBD")
val rFactor : Float = 4f
val delay : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Canvas.drawLineDot(i : Int, sc1 : Float, sc2 : Float, size : Float, paint : Paint) {
    val r : Float = size / rFactor
    val deg : Float = finalDeg / (parts - 1)
    save()
    rotate(deg * i)
    for (j in 0..1) {
        drawCircle(0f, -size * j * sc1.divideScale(i, parts), r, paint)
    }
    drawLine(0f, -r, 0f, -r - (size - 2 * r) * sc2.divideScale(i, parts), paint)
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

class LineDotRotView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LDRNode(var i : Int, val state : State = State()) {

        private var next : LDRNode? = null
        private var prev : LDRNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = LDRNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawLDRNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LDRNode {
            var curr : LDRNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LineDotRot(var i : Int) {

        private val root : LDRNode = LDRNode(0)
        private var curr : LDRNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : LineDotRotView) {

        private val animator : Animator = Animator(view)
        private val ldr : LineDotRot = LineDotRot(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            ldr.draw(canvas, paint)
            animator.animate {
                ldr.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            ldr.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : LineDotRotView {
            val view : LineDotRotView = LineDotRotView(activity)
            activity.setContentView(view)
            return view
        }
    }
}