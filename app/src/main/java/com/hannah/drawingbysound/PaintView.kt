package com.hannah.drawingbysound

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.graphics.Color

class PaintView(c: Context, attrs: AttributeSet): View(c, attrs) {
    private var brushSize: Int = 0 // 筆刷的尺寸
    private var currentColor = Color.RED
    // paint 畫筆設定，Canvas 畫筆，Bitmap 畫紙
    private val myPaint = Paint()
    private lateinit var myBitmap: Bitmap
    private lateinit var myCanvas: Canvas
    private val myBitmapPaint: Paint = Paint()

    private var mathDone = false

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

        canvas.drawBitmap(myBitmap, 0f, 0f, myBitmapPaint)
        
    }
}