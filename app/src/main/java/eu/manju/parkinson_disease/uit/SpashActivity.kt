package eu.manju.parkinson_disease.uit

//import android.R
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import eu.manju.parkinson_disease.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)
        val logo = findViewById<ImageView>(R.id.logo)
        val anim = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        logo.startAnimation(anim)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2500) // 2.5 seconds
    }
}