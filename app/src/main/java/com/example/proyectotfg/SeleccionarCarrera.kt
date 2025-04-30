package com.example.proyectotfg

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SeleccionarCarrera : AppCompatActivity() {
    private lateinit var botonElegirModo :Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_carrera)
        botonElegirModo = findViewById(R.id.buttonElegirDeporte)
        val idDeporte = intent.getIntExtra("id_deporte", -1)
        val nombreUsuario = intent.getStringExtra("nombre_usuario") ?: "usuario"
//        Log.i("DEBUG", "ID del deporte recibido en Seleccionar_Carrera: $idDeporte")

        botonElegirModo.setOnClickListener {
            val intent = Intent(this, EligeModo::class.java).apply {
                putExtra("nombre_usuario", nombreUsuario)
            }

            startActivity(intent)

        }
        val fragment = ListaCarreras().apply {
            arguments = Bundle().apply {
                putInt("id_deporte", idDeporte)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .commit()
    }
}