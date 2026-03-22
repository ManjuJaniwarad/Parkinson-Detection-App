package eu.manju.parkinson_disease.uit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import eu.manju.parkinson_disease.R

class UploadActivity : AppCompatActivity() {

    private val CAMERA_REQUEST = 1
    private val GALLERY_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        val btnCamera = findViewById<Button>(R.id.btnCamera)
        val btnGallery = findViewById<Button>(R.id.btnGallery)

        // Camera
        btnCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAMERA_REQUEST)
        }

        // Gallery
        btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {

            val intent = Intent(this, PreviewActivity::class.java)

            if (requestCode == CAMERA_REQUEST) {
                val bitmap = data.extras?.get("data") as? android.graphics.Bitmap
                if (bitmap != null) {
                    intent.putExtra("image", bitmap)
                }
            }

            if (requestCode == GALLERY_REQUEST) {
                val uri: Uri? = data.data
                if (uri != null) {
                    intent.putExtra("imageUri", uri)
                }
            }

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}