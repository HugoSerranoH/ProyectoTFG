package com.example.proyectotfg

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class CrearCarrera : Fragment() {
    private lateinit var dbHelper: BaseDatosEjemplo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_crear_carrera, container, false)

        // Inicializar la base de datos
        dbHelper = BaseDatosEjemplo(requireContext(), "ProyectoTFG", null, 1)


        val editTextNombreCarrera = view.findViewById<EditText>(R.id.editTextTextNombreCarrera)
        val editTextLocalidad = view.findViewById<EditText>(R.id.editTextTextLocalidad)
        val buttonGuardarCarrera = view.findViewById<Button>(R.id.buttonGuardarCarrera)

        // Obtener el ID del deporte del argumento
        val idDeporte = arguments?.getInt("id_deporte", -1) ?: -1
        Log.i("DEBUG", "ID del deporte recibido en Crear_CarreraFragment: $idDeporte")

        buttonGuardarCarrera.setOnClickListener {
            val nombreCarrera = editTextNombreCarrera.text.toString()
            val localidad = editTextLocalidad.text.toString()

            if (nombreCarrera.isNotEmpty() && localidad.isNotEmpty() && idDeporte != -1) {
                guardarCarrera(nombreCarrera, localidad, idDeporte)
            } else {
                Toast.makeText(requireContext(), "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                Log.e("DEBUG", "Error: Datos faltantes o ID del deporte incorrecto")
            }
        }

        return view
    }

    private fun guardarCarrera(nombreCarrera: String, localidad: String, idDeporte: Int) {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("nombre_carrera", nombreCarrera)
            put("localidad", localidad)
            put("id_deporte", idDeporte)
        }

        val resultado = db.insert("carreras", null, valores)
        if (resultado != -1L) {
            Toast.makeText(requireContext(), "Carrera con nombre $nombreCarrera y realizada en  $localidad guardada", Toast.LENGTH_SHORT).show()
            Log.i("SQL", "Carrera insertada en la base de datos con ID: $resultado, Localidad: $localidad")

            // Volver al Fragment anterior
            requireActivity().supportFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Error al guardar la carrera", Toast.LENGTH_SHORT).show()
            Log.e("SQL", "Error al insertar la carrera")
        }
    }
}