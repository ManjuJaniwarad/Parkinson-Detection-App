package eu.manju.parkinson_disease.uit

import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import eu.manju.parkinson_disease.R
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.graphics.ImageDecoder
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

        // Display image
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

            // STEP 1: Resize
            val resized = Bitmap.createScaledBitmap(bitmapInput, 224, 224, true)

            // STEP 2: Clean image (white background)
            val processedBitmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(processedBitmap)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(resized, 0f, 0f, null)

            val finalBitmap = processedBitmap

            // STEP 3: Validate image
            if (!isHandwriting(finalBitmap)) {
                Toast.makeText(
                    this,
                    "Please upload a valid handwriting/spiral image",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // STEP 4: Convert to grayscale
            val grayBitmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888)

            for (y in 0 until 224) {
                for (x in 0 until 224) {
                    val pixel = finalBitmap.getPixel(x, y)

                    val r = (pixel shr 16 and 0xFF)
                    val g = (pixel shr 8 and 0xFF)
                    val b = (pixel and 0xFF)

                    val gray = (r + g + b) / 3

                    grayBitmap.setPixel(x, y, Color.rgb(gray, gray, gray))
                }
            }

            // STEP 5: Convert to ByteBuffer
            val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
            byteBuffer.order(ByteOrder.nativeOrder())

            for (y in 0 until 224) {
                for (x in 0 until 224) {
                    val pixel = grayBitmap.getPixel(x, y)

                    val r = (pixel shr 16 and 0xFF) / 255.0f
                    val g = (pixel shr 8 and 0xFF) / 255.0f
                    val b = (pixel and 0xFF) / 255.0f

                    byteBuffer.putFloat(r)
                    byteBuffer.putFloat(g)
                    byteBuffer.putFloat(b)
                }
            }

            // STEP 6: Run model
            val interpreter = Interpreter(loadModelFile(this))
            val output = Array(1) { FloatArray(1) }

            interpreter.run(byteBuffer, output)

            val resultValue = output[0][0]

            // STEP 7: Interpret result
            val resultText = when {
                resultValue > 0.6 -> "Parkinson's Detected"
                resultValue < 0.4 -> "Healthy"
                else -> "Uncertain - Try Again"
            }

            val confidence = (resultValue * 100).toInt().toString() + "%"

            // Move to ResultActivity
            val intent = android.content.Intent(this, ResultActivity::class.java)
            intent.putExtra("result", resultText)
            intent.putExtra("confidence", confidence)
            startActivity(intent)
        }
    }

    // Validation function
    fun isHandwriting(bitmap: Bitmap): Boolean {
        var darkPixels = 0
        var sampledPixels = 0

        for (y in 0 until bitmap.height step 5) {
            for (x in 0 until bitmap.width step 5) {

                val pixel = bitmap.getPixel(x, y)

                val r = (pixel shr 16 and 0xFF)
                val g = (pixel shr 8 and 0xFF)
                val b = (pixel and 0xFF)

                val brightness = (r + g + b) / 3

                if (brightness < 120) darkPixels++

                sampledPixels++
            }
        }

        val ratio = darkPixels.toFloat() / sampledPixels

        return ratio > 0.02 && ratio < 0.4
    }
}

