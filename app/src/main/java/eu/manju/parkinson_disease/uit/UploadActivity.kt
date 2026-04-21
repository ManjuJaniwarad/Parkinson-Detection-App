package eu.manju.parkinson_disease.uit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import eu.manju.parkinson_disease.R
import java.io.File

class UploadActivity : AppCompatActivity() {

    private lateinit var imageUri: Uri

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            openPreview(imageUri)
        } else {
            Toast.makeText(this, "Capture failed", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { openPreview(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        findViewById<Button>(R.id.btnCamera).setOnClickListener {
            val file = File(cacheDir, "camera.jpg")
            imageUri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )
            cameraLauncher.launch(imageUri)
        }

        findViewById<Button>(R.id.btnGallery).setOnClickListener {
            galleryLauncher.launch("image/*")
        }
    }

    private fun openPreview(uri: Uri) {
        val intent = Intent(this, PreviewActivity::class.java)
        intent.putExtra("imageUri", uri.toString())
        startActivity(intent)
    }
}