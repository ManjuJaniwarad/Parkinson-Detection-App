package eu.manju.parkinson_disease.uit

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import eu.manju.parkinson_disease.R


class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        val imageView = findViewById<ImageView>(R.id.imageView)
        val btnAnalyze = findViewById<Button>(R.id.btnAnalyze)

        val bitmap = intent.getParcelableExtra<Bitmap>("image")
        val uri = intent.getParcelableExtra<Uri>("imageUri")

        // Display image safely
        when {
            bitmap != null -> imageView.setImageBitmap(bitmap)
            uri != null -> imageView.setImageURI(uri)
            else -> imageView.setImageResource(android.R.drawable.ic_menu_report_image)
        }

        btnAnalyze.setOnClickListener {
            val intent = android.content.Intent(this, ResultActivity::class.java)
            intent.putExtra("result", "Parkinson: Detected")
            intent.putExtra("confidence", "85%")

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}