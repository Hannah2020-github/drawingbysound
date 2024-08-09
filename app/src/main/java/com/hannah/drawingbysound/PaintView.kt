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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PaintView(c: Context, attrs: AttributeSet): View(c, attrs) {
    private var brushSize: Int = 0 // 筆刷筆尖的尺寸
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
    // 貝茲曲線的變數
    private var mX = 0f
    private var mY = 0f

    // single thread executor
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        myPaint.isDither = true
        myPaint.style = Paint.Style.STROKE
        myPaint.strokeCap = Paint.Cap.ROUND // 頭與尾線段設定為圓潤
        myPaint.strokeJoin = Paint.Join.ROUND // 拐角處(例：三角形)設定為圓潤

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
//            Log.d("ABC", "123 ===> paths.size: ${paths.size}")
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
                touchUp()
                invalidate() // post a block of work((指 onDraw method)) to main thread's message queue.
                post {
                    newPath = false
                }
            }
        }

        return true
    }

    private fun touchStart(x: Float, y: Float) {
        brushSize = 10
        myPath = Path()
        myPath.moveTo(x, y) // p1
        paths.add(FingerPath(currentColor, brushSize, myPath))
        mX = x // 在 p1 的點上
        mY = y // 在 p1 的點上
    }

    private fun touchMove(x: Float, y: Float) {
//        var pointPaint = Paint()
//        pointPaint.color = Color.BLACK
//        pointPaint.strokeWidth = 10f // 點的寬度(線與線之間的點)
//        myCanvas.drawPoint(x, y , pointPaint)

        // 手指滑到 p2 時，執行 quadTo method，(x + mX) / 2, (y + mY) / 2 帶入 p2 點，位置會在p1 與 p2 的中心點
        myPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2) // 畫出 p1 到 p1 與 p2 的中心點的直線(一開始繪製會是直線)
//        myPath.lineTo(x, y) // lineTo 為繪製線段，當畫弧度線條時，線段會有菱有角。
        mX = x // 在 p2 的點上
        mY = y // 在 p2 的點上
    }

    private fun touchUp() {
        myPath.lineTo(mX, mY)
    }

    fun clear() {
        executor.execute {
            // paths needs to be cleared.
            paths.clear()

            // each pixel in myBitmap need to be set
            for (i in 0 until myBitmap.width) {
                for (j in 0 until myBitmap.height) {
                    myBitmap.setPixel(i, j ,Color.WHITE)
                }
            }
            postInvalidate()
        }
    }
}