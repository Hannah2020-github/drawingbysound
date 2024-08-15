package com.hannah.drawingbysound

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.util.TypedValue
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
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var listener: Listener
    private var isListening = false

    inner class Listener(val c: Context) : RecognitionListener {
        private fun restoreCmdBtnStyling() {
            commandBtn.text = resources.getString(R.string.sound_btn_text)

            // 從 theme.xml 用 programmatically 取得顏色設定
            val typedValue = TypedValue()
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
            val textColor = ContextCompat.getColor(c, typedValue.resourceId)
            commandBtn.setTextColor(textColor)

            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
            val backgroundColor = ContextCompat.getColor(c, typedValue.resourceId)
            commandBtn.setBackgroundColor(backgroundColor)
        }
        override fun onReadyForSpeech(params: Bundle?) {}

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            restoreCmdBtnStyling()
        }

        // 如果使用者在一段時間內沒有說出任何話，則會執行 onError()
        override fun onError(error: Int) {
            restoreCmdBtnStyling()
            resultText.text = resources.getString(R.string.no_sound_text)
        }

        // 聲音辨識的結果
        override fun onResults(results: Bundle) {
            val data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            resultText.text = "${resources.getString(R.string.soundResult)} ${data!![0]}"

            val commandString = data[0].split(" ")
            val colors = ArrayList<String>()
            var penCommand = false
            var fillCommand = false
            for (i in commandString.indices) {
                val currentString = commandString[i].lowercase()
                when (currentString) {
                    "clear" -> {
                        paintView.clear()
                        return
                    }
                    "eraser", "erase", "chaser" -> {
                        paintView.changeMode(0)
                        paintView.changeBruchColor(Color.WHITE)
                        setModeBorder(eraserBtn)
                        setBtnBorder(whiteBtn)
                        return
                    }
                    "fill", "fell", "feel", "fail", "phil" -> {
                        fillCommand = true
                    }
                    "pen", "pain", "pane", "pan" -> {
                        penCommand = true
                    }
                    "blue", "blu", "black", "white", "green", "yellow", "red", "magenta", "ring" -> {
                        colors.add(commandString[i].lowercase())
                    }
                }
            }

            if (penCommand && fillCommand) {
                Toast.makeText(c, resources.getString(R.string.pen_fill_together), Toast.LENGTH_SHORT).show()
                return
            }else if (penCommand) {
                setModeBorder(penBtn)
                paintView.changeMode(1)
            }else if (fillCommand) {
                setModeBorder(fillBtn)
                paintView.changeMode(-1)
            }

            if (colors.size > 1) {
                Toast.makeText(c, resources.getString(R.string.multi_color_detection), Toast.LENGTH_SHORT).show()
            }else if (colors.size == 1) {
                // parse color
                when (colors[0]) {
                    "black" -> {
                        setBtnBorder(blackBtn)
                        paintView.changeBruchColor(Color.BLACK)
                    }
                    "white" -> {
                        setBtnBorder(whiteBtn)
                        paintView.changeBruchColor(Color.WHITE)
                    }
                    "gray" -> {
                        setBtnBorder(grayBtn)
                        paintView.changeBruchColor(Color.GRAY)
                    }
                    "red" -> {
                        setBtnBorder(redBtn)
                        paintView.changeBruchColor(Color.RED)
                    }
                    "blue" -> {
                        setBtnBorder(blueBtn)
                        paintView.changeBruchColor(Color.BLUE)
                    }
                    "yellow" -> {
                        setBtnBorder(yellowBtn)
                        paintView.changeBruchColor(Color.YELLOW)
                    }
                    "green", "ring" -> {
                        setBtnBorder(greenBtn)
                        paintView.changeBruchColor(Color.GREEN)
                    }
                    "magenta" -> {
                        setBtnBorder(magentaBtn)
                        paintView.changeBruchColor(Color.MAGENTA)
                    }
                }

                // check the current mdoe
                if (paintView.getMode() == 0) {
                    setModeBorder(penBtn)
                    paintView.changeMode(1)
                }

            }
        }

        override fun onPartialResults(partialResults: Bundle?) {}

        override fun onEvent(eventType: Int, params: Bundle?) {}

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        findViewById()
        paintView.useProgressBar(progressBar)

        // 設定 modeArray, btnArray
        modeArray = arrayOf(fillBtn, penBtn, eraserBtn)
        btnArray =
            arrayOf(blackBtn, whiteBtn, grayBtn, redBtn, blueBtn, yellowBtn, greenBtn, magentaBtn)
        colorArray = arrayOf(
            Color.BLACK,
            Color.WHITE,
            Color.GRAY,
            Color.RED,
            Color.BLUE,
            Color.YELLOW,
            Color.GREEN,
            Color.MAGENTA
        )
        setModeBorder(penBtn)

        if (isRecordAudioPermissionGranted()) {
            // permission is give
            Toast.makeText(
                this,
                resources.getString(R.string.sound_permission_given),
                Toast.LENGTH_SHORT
            ).show()
            setSoundWork()
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
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            } else {
                // 向使用者取得權限
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), AUDIO_REQUEST_CODE)
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AUDIO_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setSoundWork()
                } else {
                    resultText.text = resources.getString(R.string.denied_result)
                    Toast.makeText(
                        this,
                        resources.getString(R.string.denied_result_alert),
                        Toast.LENGTH_SHORT
                    ).show()
                    commandBtn.setOnClickListener {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.denied_result),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun setSoundWork() {
        // SpeechRecognizer: 聲音辨識器，可設定語言、結果等
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        // RecognitionListener: 聲音辨識器的進度與結果
        listener = Listener(this)
        speechRecognizer.setRecognitionListener(listener)

        resultText.text = "Sound Recognition result is here:"
        commandBtn.setOnClickListener {
            if (!isListening) {
                isListening = true

                commandBtn.text = resources.getString(R.string.receiving_sound_command)
                commandBtn.setTextColor(Color.WHITE)
                commandBtn.setBackgroundColor(Color.BLACK)

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
//                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "cmn-Hant-TW")
//                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "cmn-HanS-CN")
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                intent.putExtra(RecognizerIntent.EXTRA_RESULTS, 5)
                speechRecognizer.startListening(intent) // 開始聲音辨識功能
            }
            isListening = false

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

