package eu.manju.parkinson_disease.uit

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
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
    private var bitmapInput: Bitmap? = null
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        val imageView = findViewById<ImageView>(R.id.imageView)
        val btnAnalyze = findViewById<Button>(R.id.btnAnalyze)

        interpreter = Interpreter(loadModelFile(this))

        uri = intent.getStringExtra("imageUri")?.toUri()

        // 🔹 Decode image safely
        bitmapInput = try {
            uri?.let {
                contentResolver.openInputStream(it)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
        } catch (e: Exception) {
            null
        }

        // 🔹 Fix rotation (camera images)
        bitmapInput = bitmapInput?.let {
            if (uri != null) fixRotation(it, uri!!) else it
        }

        if (bitmapInput != null) {
            imageView.setImageBitmap(bitmapInput)
        } else {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnAnalyze.setOnClickListener {

            val bitmap = bitmapInput
            if (bitmap == null) {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

                // 🔹 BASIC FILTER (reject blank/random)
                if (!isBasicDrawing(resized)) {
                    showInvalid()
                    return@setOnClickListener
                }

                // 🔹 Convert to RGB buffer
                val buffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
                buffer.order(ByteOrder.nativeOrder())

                for (y in 0 until 224) {
                    for (x in 0 until 224) {
                        val p = resized.getPixel(x, y)
                        buffer.putFloat((p shr 16 and 0xFF) / 255f)
                        buffer.putFloat((p shr 8 and 0xFF) / 255f)
                        buffer.putFloat((p and 0xFF) / 255f)
                    }
                }

                val output = Array(1) { FloatArray(1) }
                interpreter.run(buffer, output)

                val value = output[0][0]

                // 🔴 KEY FIX: reject non-spiral using model confusion
                if (value in 0.45..0.60) {
                    showInvalid()
                    return@setOnClickListener
                }

                val result = if (value > 0.6) {
                    "Parkinson's Detected"
                } else {
                    "Healthy"
                }

                val intent = Intent(this, ResultActivity::class.java)
                intent.putExtra("result", result)
                intent.putExtra("confidence", "${(value * 100).toInt()}%")
                startActivity(intent)

            } catch (e: Exception) {
                Toast.makeText(this, "Error processing image", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showInvalid() {
        Toast.makeText(
            this,
            "Only spiral images can be captured or uploaded",
            Toast.LENGTH_LONG
        ).show()
        finish()
    }

    private fun isBasicDrawing(bitmap: Bitmap): Boolean {
        var dark = 0
        var total = 0

        for (y in 0 until bitmap.height step 8) {
            for (x in 0 until bitmap.width step 8) {
                val p = bitmap.getPixel(x, y)
                val b = ((p shr 16 and 0xFF) +
                        (p shr 8 and 0xFF) +
                        (p and 0xFF)) / 3

                if (b < 180) dark++
                total++
            }
        }

        return (dark.toFloat() / total) > 0.01
    }

    private fun fixRotation(bitmap: Bitmap, uri: Uri): Bitmap {
        return try {
            contentResolver.openInputStream(uri)?.use {
                val exif = ExifInterface(it)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )

                val matrix = Matrix()

                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                }

                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } ?: bitmap
        } catch (e: Exception) {
            bitmap
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        interpreter.close()
    }
}