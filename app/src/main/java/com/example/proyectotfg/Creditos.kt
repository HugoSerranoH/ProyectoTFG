package com.example.proyectotfg

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Creditos : AppCompatActivity() {
    private lateinit var botongithub : ImageView
    private lateinit var botonlinkedin : ImageView
    private lateinit var botoncifp : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_creditos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        botongithub = findViewById(R.id.imageViewgithubicon)
        botonlinkedin = findViewById(R.id.imageViewlinkedinicon)
        botoncifp = findViewById(R.id.imageViewlogocifp)

        botongithub.setOnClickListener {
            val githubUrl = "https://github.com/HugoSerranoH/ProyectoTFG"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
            startActivity(intent)
        }

        botonlinkedin.setOnClickListener {
            val linkedinUrl = "https://www.linkedin.com/in/hugo-serrano-hernández-baaa512a6"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkedinUrl))
            startActivity(intent)
        }

        botoncifp.setOnClickListener {
            val linkedinUrl = "http://cifpjuandeherrera.centros.educa.jcyl.es/sitio/"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkedinUrl))
            startActivity(intent)
        }

    }
}