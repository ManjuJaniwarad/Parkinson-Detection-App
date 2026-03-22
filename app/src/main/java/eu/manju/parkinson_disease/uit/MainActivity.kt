package eu.manju.parkinson_disease.uit

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import eu.manju.parkinson_disease.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStart = findViewById<Button>(R.id.btnStart)
        val anim = AnimationUtils.loadAnimation(this, R.anim.scale)

        btnStart.setOnClickListener {
            it.startAnimation(anim)

            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)

            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}