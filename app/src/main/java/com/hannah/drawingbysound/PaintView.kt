package com.hannah.drawingbysound

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.graphics.Color
import android.graphics.Path
import android.graphics.Point
import android.util.Log
import android.view.MotionEvent
import android.widget.ProgressBar
import androidx.core.graphics.get
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PaintView(c: Context, attrs: AttributeSet): View(c, attrs) {
    private var brushSize: Int = 0 // 筆刷筆尖的尺寸
    private var currentColor = Color.RED
    // 1 Pen mode, -1 fill mode, 0 eraser
    private var mode = 1

    // paint 畫筆設定，Canvas 畫筆，Bitmap 畫紙
    private val myPaint = Paint()
    private lateinit var myBitmap: Bitmap
    private lateinit var myCanvas: Canvas
    private val myBitmapPaint: Paint = Paint()
    private lateinit var myPath: Path
    private val paths: ArrayList<FingerPath> = ArrayList()
    private lateinit var progressBar: ProgressBar

    private var mathDone = false
    private var newPath = false // 繪製新線條
    // 貝茲曲線的變數
    private var mX = 0f
    private var mY = 0f

    // single thread executor
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        myPaint.isAntiAlias = false // 去除線段鋸齒狀
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
            MotionEvent.ACTION_DOWN -> if (mode >= 0){
                newPath = true
                touchStart(x, y)
                invalidate()
            }else {
                val fillPoint = Point(x.toInt(),y.toInt())
                val sourceColor = myBitmap.getPixel(x.toInt(), y.toInt())
                val targetColor = currentColor
                fillWork(myBitmap, fillPoint, sourceColor, targetColor)
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode >= 0) {
                    touchMove(x, y)
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (mode >= 0) {
                    touchUp()
                    invalidate() // post a block of work((指 onDraw method)) to main thread's message queue.
                    post {
                        newPath = false
                    }
                }
            }
        }
        return true
    }

    private fun touchStart(x: Float, y: Float) {
        brushSize = if (mode == 0) 40 else 10
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
            post {
                progressBar.visibility = VISIBLE
            }
            // paths needs to be cleared.
            paths.clear()

            // each pixel in myBitmap need to be set
            for (i in 0 until myBitmap.width) {
                for (j in 0 until myBitmap.height) {
                    myBitmap.setPixel(i, j ,Color.WHITE)
                }
                // 從左至右更新螢幕的 bitmap
                post {
                    invalidate()
                }
            }
            post {
                progressBar.visibility = INVISIBLE
            }
            postInvalidate()
        }
    }

    fun useProgressBar(bar: ProgressBar) {
        progressBar = bar
        progressBar.visibility = INVISIBLE
    }

    fun changeMode(input: Int) {
        mode = input
    }

    fun getMode(): Int {
        return mode
    }

    fun fillWork(bmp: Bitmap, pt: Point, sourceColor: Int, targetColor: Int) {
        executor.execute {
            post {
                progressBar.visibility = VISIBLE
            }
            floodFill(bmp, pt, sourceColor, targetColor)
            post {
                progressBar.visibility = INVISIBLE
            }
        }
    }

    fun floodFill(bmp: Bitmap, pt: Point, sourceColor: Int, targetColor: Int) {
        var myNode: Point? = pt
        val width = bmp.width
        val height = bmp.height

        // 點選要填充的地方的顏色與當下的顏色一樣，則不執行填充
        // 如果當下顏色不等於要填充色，執行 if 內程式碼
        if (sourceColor != targetColor) {
            val queue: Queue<Point> = LinkedList()
            do {
                var x = myNode!!.x
                var y = myNode!!.y
                // 左邊的顏色(x - 1)
                while (x > 0 && bmp.getPixel(x - 1, y) == sourceColor) {
                    // 將欲填充的目標點，鎖定為填充框的最左側邊緣
                    x--
                }
                var spanUp = false
                var spanDown = false
                while (x < width && bmp.getPixel(x, y) == sourceColor) {
                    bmp.setPixel(x, y , targetColor)
                    if (!spanUp && y > 0 && bmp.getPixel(x, y - 1) == sourceColor) {
                        queue.add(Point(x, y - 1))
                        spanUp = true
                    }else if (spanUp && y > 0 && bmp.get(x, y - 1) != sourceColor) {
                        spanUp = false
                    }

                    if (!spanDown && y < height - 1 && bmp.getPixel(x, y + 1) == sourceColor) {
                        queue.add(Point(x, y + 1))
                        spanDown = true
                    }else if (spanDown && y < height -1 && bmp.get(x, y + 1) != sourceColor) {
                        spanDown = false
                    }
                    x++
                }
                postInvalidate()
                myNode = queue.poll()
            }while (myNode != null)
        }
    }

    fun changeBruchColor(color: Int) {
        currentColor = color
    }
}