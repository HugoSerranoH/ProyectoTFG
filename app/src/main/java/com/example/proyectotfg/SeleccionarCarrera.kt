package com.example.proyectotfg

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class SeleccionarCarrera : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_carrera)

        val idDeporte = intent.getIntExtra("id_deporte", -1)
        Log.i("DEBUG", "ID del deporte recibido en Seleccionar_Carrera: $idDeporte")


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