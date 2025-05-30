package dev.wangchao.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class NoiseLevelActivity : AppCompatActivity() {
    private val TAG = "NoiseLevelActivity"
    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1001
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var bufferSize = 0
    private lateinit var noiseLevelTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noise_level)

        noiseLevelTextView = findViewById(R.id.noiseLevelTextView)

        // 检查并请求录音权限
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_REQUEST_CODE
            )
        } else {
            startRecording()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
            } else {
                // 权限被拒绝
                noiseLevelTextView.text = "未授予录音权限"
            }
        }
    }

    private fun startRecording() {
        // 计算缓冲区大小
        bufferSize = AudioRecord.getMinBufferSize(
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        // 再次检查权限
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 权限未授予，提示用户或退出
            Log.e(TAG, "录音权限未授予，无法启动录音")
            noiseLevelTextView.text = "请授予录音权限"
            return
        }

        // 初始化AudioRecord
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        // 开始录音线程
        isRecording = true
        Thread {
            val buffer = ShortArray(bufferSize)
            audioRecord?.startRecording()

            while (isRecording) {
                val bytesRead = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (bytesRead > 0) {
                    val db = calculateDecibel(buffer, bytesRead)
                    runOnUiThread {
                        noiseLevelTextView.text = "噪音水平: ${db} dB"
                    }
                }
            }

            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        }.start()
    }

    private fun calculateDecibel(buffer: ShortArray, bytesRead: Int): Double {
        var sum = 0.0
        for (i in 0 until bytesRead) {
            sum += buffer[i] * buffer[i].toDouble()
        }

        val rms = Math.sqrt(sum / bytesRead)
        // 参考值为1，这是一个简化的计算
        return 20 * Math.log10(rms / 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRecording = false
    }
}
