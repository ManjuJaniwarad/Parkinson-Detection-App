package eu.manju.parkinson_disease.uit

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import eu.manju.parkinson_disease.R
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PreviewActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        val imageView = findViewById<ImageView>(R.id.imageView)
        val btnAnalyze = findViewById<Button>(R.id.btnAnalyze)

        // Load model
        interpreter = Interpreter(loadModelFile(this))

        val uri = intent.getStringExtra("imageUri")?.toUri()

        // Load bitmap safely
        val bitmap: Bitmap? = uri?.let {
            contentResolver.openInputStream(it)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }

        if (bitmap == null) {
            Toast.makeText(this, "Image load failed", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        imageView.setImageBitmap(bitmap)

        btnAnalyze.setOnClickListener {

            val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

            val buffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
            buffer.order(ByteOrder.nativeOrder())

            // ✅ SAFE RGB extraction (FIXED)
            for (y in 0 until 224) {
                for (x in 0 until 224) {

                    val pixel = resized.getPixel(x, y)

                    val r = ((pixel shr 16) and 0xFF).toFloat() / 255f
                    val g = ((pixel shr 8) and 0xFF).toFloat() / 255f
                    val b = (pixel and 0xFF).toFloat() / 255f

                    buffer.putFloat(r)
                    buffer.putFloat(g)
                    buffer.putFloat(b)
                }
            }

            val output = Array(1) { FloatArray(3) }
            interpreter.run(buffer, output)

            val probs = output[0]
            val maxIndex = probs.indices.maxByOrNull { probs[it] } ?: -1

            val result = when (maxIndex) {
                0 -> "Healthy"
                1 -> {
                    Toast.makeText(this, "Only spiral images allowed", Toast.LENGTH_LONG).show()
                    finish()
                    return@setOnClickListener
                }
                2 -> "Parkinson's Detected"
                else -> "Unknown"
            }

            val confidence = (probs[maxIndex] * 100).toInt()

            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("result", result)
            intent.putExtra("confidence", "$confidence%")

            startActivity(intent)
            overridePendingTransition(R.anim.zoom_in, R.anim.fade_slide)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        interpreter.close()
    }
}