package com.hannah.drawingbysound

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.hannah.drawingbysound.ui.theme.DrawingbysoundTheme

class MainActivity : AppCompatActivity() {

    companion object {
        private val AUDIO_REQUEST_CODE = 1
    }

    private lateinit var paintView: PaintView
    private lateinit var resultText: TextView
    private lateinit var commandBtn: Button
    private lateinit var saveBtn: Button
    private lateinit var loadBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var clearBtn: MaterialButton
    private lateinit var fillBtn: MaterialButton
    private lateinit var penBtn: MaterialButton
    private lateinit var eraserBtn: MaterialButton
    private lateinit var blackBtn: MaterialButton
    private lateinit var whiteBtn: MaterialButton
    private lateinit var grayBtn: MaterialButton
    private lateinit var redBtn: MaterialButton
    private lateinit var blueBtn: MaterialButton
    private lateinit var yellowBtn: MaterialButton
    private lateinit var greenBtn: MaterialButton
    private lateinit var magentaBtn: MaterialButton
    private lateinit var modeArray: Array<MaterialButton>
    private lateinit var btnArray: Array<MaterialButton>
    private lateinit var colorArray: Array<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        findViewById()
        paintView.useProgressBar(progressBar)

        // 設定 modeArray, btnArray
        modeArray = arrayOf(fillBtn, penBtn, eraserBtn)
        btnArray = arrayOf(blackBtn, whiteBtn, grayBtn, redBtn, blueBtn, yellowBtn, greenBtn, magentaBtn)
        colorArray = arrayOf(Color.BLACK, Color.WHITE, Color.GRAY, Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.MAGENTA )
        setModeBorder(penBtn)

        if (isRecordAudioPermissionGranted()) {
            // permission is give
            Toast.makeText(this, resources.getString(R.string.sound_permission_given), Toast.LENGTH_SHORT).show()
            // set up sound work
        }


        clearBtn.setOnClickListener {
            paintView.clear()
        }
        fillBtn.setOnClickListener {
            setModeBorder(fillBtn)
            paintView.changeMode(-1)
        }
        penBtn.setOnClickListener {
            setModeBorder(penBtn)
            paintView.changeMode(1)
        }
        eraserBtn.setOnClickListener {
            paintView.changeMode(0)
            paintView.changeBruchColor(Color.WHITE)
            setModeBorder(eraserBtn)
            setBtnBorder(whiteBtn)
        }

        for (index in btnArray.indices) {
            btnArray[index].setOnClickListener {
                setBtnBorder(it as MaterialButton)
                paintView.changeBruchColor(colorArray[index])

                if (paintView.getMode() == 0) {
                    setModeBorder(penBtn)
                    paintView.changeMode(1)
                }
            }
        }
    }

    private fun setBtnBorder(button: MaterialButton) {
        // 清除按鈕外觀
        for (btn in btnArray) {
            btn.strokeWidth = 0
        }
        button.strokeWidth = 10
        button.strokeColor = ColorStateList.valueOf(Color.GRAY)
    }

    private fun setModeBorder(button: MaterialButton) {
        // 清除按鈕外觀
        for (btn in modeArray) {
            btn.strokeWidth = 0
        }
        button.strokeWidth = 10
        button.strokeColor = ColorStateList.valueOf(Color.GRAY)
    }

    private fun isRecordAudioPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                return true
            } else {
                // 向使用者取得權限
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), AUDIO_REQUEST_CODE)
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AUDIO_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // set up sound work
                } else {
                    resultText.text = resources.getString(R.string.denied_result)
                    Toast.makeText(this, resources.getString(R.string.denied_result_alert), Toast.LENGTH_SHORT).show()
                    commandBtn.setOnClickListener {
                        Toast.makeText(this, resources.getString(R.string.denied_result), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun findViewById() {
        paintView = findViewById(R.id.paint_view)
        resultText = findViewById(R.id.command_result)
        commandBtn = findViewById(R.id.command_btn)
        saveBtn = findViewById(R.id.save_btn)
        loadBtn = findViewById(R.id.load_btn)
        progressBar = findViewById(R.id.progressBar)
        clearBtn = findViewById(R.id.clear_btn)
        fillBtn = findViewById(R.id.fill_btn)
        penBtn = findViewById(R.id.pen_btn)
        eraserBtn = findViewById(R.id.eraser_btn)
        // 圓形按鈕
        blackBtn = findViewById(R.id.black_btn)
        whiteBtn = findViewById(R.id.white_btn)
        grayBtn = findViewById(R.id.gray_btn)
        redBtn = findViewById(R.id.red_btn)
        blueBtn = findViewById(R.id.blue_btn)
        yellowBtn = findViewById(R.id.yellow_btn)
        greenBtn = findViewById(R.id.green_btn)
        magentaBtn = findViewById(R.id.magenta_btn)
    }
}

