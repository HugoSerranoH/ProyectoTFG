package com.example.proyectotfg

import android.database.Cursor
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView

class ConsultaCarrera : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consulta_carrera)

        val userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        val dbHelper = BaseDatosEjemplo(this, "ProyectoTFG", null, 1)
        val db = dbHelper.readableDatabase

        val idCarrera = intent.getIntExtra("id_carrera", -1)
        val nombreCarrera = intent.getStringExtra("nombre_carrera") ?: "Sin nombre"
        val textViewModificandoNombreCarrera = findViewById<TextView>(R.id.textViewModificandoNombreCarrera)
        val textoarriba = SpannableString("Modificando $nombreCarrera")
        textoarriba.setSpan(StyleSpan(Typeface.BOLD), 0, textoarriba.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        textViewModificandoNombreCarrera.text = textoarriba

        val fragmento = intent.getStringExtra("fragmento") ?: "AnadirParticipante"

        if (idCarrera == -1) {
            Log.e("ERROR", "ConsultaCarrera: No se recibió un ID de carrera válido")
            return
        }

        var idDeporte = -1
        val cursor: Cursor = db.rawQuery(
            "SELECT id_deporte FROM carreras WHERE id = ?",
            arrayOf(idCarrera.toString())
        )

        if (cursor.moveToFirst()) {
            idDeporte = cursor.getInt(0)
        }
        cursor.close()

        if (idDeporte != -1) {
            userViewModel.setDeporteSeleccionado(idDeporte, "Deporte obtenido de BD")
        }

        userViewModel.setCarreraSeleccionada(idCarrera, nombreCarrera)

        Log.i("DEBUG", "ConsultaCarrera: ID deporte -> $idDeporte, ID carrera -> $idCarrera")


        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Map para los iconos en cada deporte
        val iconosDeporte = mapOf(
            1 to R.drawable.bicycle_icon,  // ID 1 -> Icono de ciclismo
            2 to R.drawable.sprint_icon,
            3 to R.drawable.forklift_icon,
            4 to R.drawable.useradd_icon
        )


        val menuItem = bottomNavView.menu.findItem(R.id.nav_anadir_participante)
        menuItem.setIcon(iconosDeporte[idDeporte] ?: R.drawable.useradd_icon)

        // Map para los colores en cada deporte
        val coloresFondo = mapOf(
            1 to R.color.light_grey,
            2 to R.color.light_brown,
            3 to R.color.lime
        )

        bottomNavView.setBackgroundColor(resources.getColor(coloresFondo[idDeporte] ?: R.color.deep_purple, null))



        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_anadir_participante -> cargarFragmento(AnadirParticipante())
                R.id.nav_modificar_resultados -> cargarFragmento(ModificarResultados())
                R.id.nav_ver_carrera -> cargarFragmento(VerResultados())
            }
            true
        }


        when (fragmento) {
            "ModificarResultados" -> {
                cargarFragmento(ModificarResultados())
                bottomNavView.selectedItemId = R.id.nav_modificar_resultados
            }
            "VerCarrera" -> {
                cargarFragmento(VerResultados())
                bottomNavView.selectedItemId = R.id.nav_ver_carrera
            }
            else -> {
                cargarFragmento(AnadirParticipante())
                bottomNavView.selectedItemId = R.id.nav_anadir_participante
            }
        }
    }

    private fun cargarFragmento(fragmento: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerViewConsultaCarrera, fragmento)
            .commit()
    }
}