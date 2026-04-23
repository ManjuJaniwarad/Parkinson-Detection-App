package eu.manju.parkinson_disease.uit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import eu.manju.parkinson_disease.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStart = findViewById<Button>(R.id.btnStart)

        btnStart.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
            overridePendingTransition(R.anim.zoom_in, R.anim.fade_slide)
        }
    }
}