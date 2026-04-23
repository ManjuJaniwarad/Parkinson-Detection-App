package eu.manju.parkinson_disease.uit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
            Toast.makeText(this, "Camera failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        val fab = findViewById<FloatingActionButton>(R.id.btnCamera)

        fab.setOnClickListener {

            val file = File.createTempFile("camera_", ".jpg", cacheDir)

            imageUri = FileProvider.getUriForFile(
                this,
                "eu.manju.parkinson_disease.provider",
                file
            )

            cameraLauncher.launch(imageUri)
        }
    }

    private fun openPreview(uri: Uri) {
        val intent = Intent(this, PreviewActivity::class.java)
        intent.putExtra("imageUri", uri.toString())
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivity(intent)
        overridePendingTransition(R.anim.zoom_in, R.anim.fade_slide)
    }
}