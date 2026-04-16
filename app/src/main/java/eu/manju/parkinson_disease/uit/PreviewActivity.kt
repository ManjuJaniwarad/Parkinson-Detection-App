package eu.manju.parkinson_disease.uit

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import eu.manju.parkinson_disease.R
import android.graphics.ImageDecoder
import android.os.Build
import androidx.annotation.RequiresApi
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import androidx.core.graphics.get
import androidx.core.graphics.scale
import androidx.core.net.toUri


class PreviewActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        val imageView = findViewById<ImageView>(R.id.imageView)
        val btnAnalyze = findViewById<Button>(R.id.btnAnalyze)

        val bitmap = intent.getParcelableExtra<Bitmap>("image")
        val uri = intent.getStringExtra("imageUri")?.toUri()

        // Display image safely
        when {
            bitmap != null -> imageView.setImageBitmap(bitmap)
            uri != null -> imageView.setImageURI(uri)
            else -> imageView.setImageResource(android.R.drawable.ic_menu_report_image)
        }

        btnAnalyze.setOnClickListener {

            val bitmapInput: Bitmap? = when {
                bitmap != null -> bitmap
                uri != null -> ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(contentResolver, uri)
                )
                else -> null
            }

            if (bitmapInput == null) return@setOnClickListener

            // Resize
            val resized = bitmapInput.scale(224, 224)

            // Convert to ByteBuffer
            val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
            byteBuffer.order(ByteOrder.nativeOrder())

            for (y in 0 until 224) {
                for (x in 0 until 224) {
                    val pixel = resized[x, y]

                    val r = (pixel shr 16 and 0xFF) / 255.0f
                    val g = (pixel shr 8 and 0xFF) / 255.0f
                    val b = (pixel and 0xFF) / 255.0f

                    byteBuffer.putFloat(r)
                    byteBuffer.putFloat(g)
                    byteBuffer.putFloat(b)
                }
            }

            // Run model
            val interpreter = Interpreter(loadModelFile(this))
            val output = Array(1) { FloatArray(1) }

            interpreter.run(byteBuffer, output)

            val resultValue = output[0][0]

            val resultText = when {
                resultValue > 0.7 -> "Parkinson's Detected"
                resultValue < 0.6 -> "Healthy"
                else -> "Uncertain"
            }

            val confidence = (resultValue * 100).toInt().toString() + "%"

            val intent = android.content.Intent(this, ResultActivity::class.java)
            intent.putExtra("result", resultText)
            intent.putExtra("confidence", confidence)

            startActivity(intent)
        }
    }
}