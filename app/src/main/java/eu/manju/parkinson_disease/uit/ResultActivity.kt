package eu.manju.parkinson_disease.uit

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import eu.manju.parkinson_disease.R


class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val txtResult = findViewById<TextView>(R.id.txtResult)
        val txtConfidence = findViewById<TextView>(R.id.txtConfidence)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val result = intent.getStringExtra("result")
        val confidence = intent.getStringExtra("confidence")

        txtResult.text = result ?: "No Result"
        txtConfidence.text = "Confidence: ${confidence ?: "N/A"}"

        btnBack.setOnClickListener {
            finish()
        }
    }
}