package com.hannah.drawingbysound

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.graphics.Color
import android.graphics.Path
import android.util.Log
import android.view.MotionEvent

class PaintView(c: Context, attrs: AttributeSet): View(c, attrs) {
    private var brushSize: Int = 0 // 筆刷的尺寸
    private var currentColor = Color.RED
    // paint 畫筆設定，Canvas 畫筆，Bitmap 畫紙
    private val myPaint = Paint()
    private lateinit var myBitmap: Bitmap
    private lateinit var myCanvas: Canvas
    private val myBitmapPaint: Paint = Paint()
    private lateinit var myPath: Path
    private val paths: ArrayList<FingerPath> = ArrayList()

    private var mathDone = false
    private var newPath = false // 繪製新線條

    init {
        myPaint.isDither = true
        myPaint.style = Paint.Style.STROKE

        myBitmapPaint.isDither = true
    }

    override fun onDraw(canvas: Canvas) {
        if (!mathDone) {
            // Bitmap 需在此才會獲取到螢幕上的尺寸(寬、高)
            myBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            myBitmap.eraseColor(Color.WHITE) // 設定初始的顏色
            myCanvas = Canvas(myBitmap)
            mathDone = true
        }

        // 如果畫面上有新的 path，則將 path 畫在 myBitmap 上
        if (newPath) {
            Log.d("ABC", "123 ===> paths.size: ${paths.size}")
            myPaint.color = paths[paths.size - 1].color
            myPaint.strokeWidth = paths[paths.size - 1].strokeWidth.toFloat()
            myCanvas.drawPath(paths[paths.size - 1].path, myPaint)
        }
        canvas.drawBitmap(myBitmap, 0f, 0f, myBitmapPaint)

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                newPath = true
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp(x, y)
                invalidate()
                newPath = false
            }
        }

        return true
    }

    private fun touchStart(x: Float, y: Float) {
        brushSize = 20
        myPath = Path()
        myPath.moveTo(x, y)
        paths.add(FingerPath(currentColor, brushSize, myPath))
    }

    private fun touchMove(x: Float, y: Float) {
        myPath.lineTo(x, y)
    }

    private fun touchUp(x: Float, y: Float) {
        myPath.lineTo(x, y)
    }
}