package com.example.proyectotfg

import android.app.DatePickerDialog
import android.content.ContentValues
import android.icu.util.Calendar
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_crear_carrera, container, false)

        // Inicializar la base de datos
        dbHelper = BaseDatosEjemplo(requireContext(), "ProyectoTFG", null, 1)


        val editTextNombreCarrera = view.findViewById<EditText>(R.id.editTextTextNombreCarrera)
        val editTextLocalidad = view.findViewById<EditText>(R.id.editTextTextLocalidad)
        val editTextFecha = view.findViewById<EditText>(R.id.editTextTextFecha)
        val buttonGuardarCarrera = view.findViewById<Button>(R.id.buttonGuardarCarrera)

        // Obtener el ID del deporte del argumento
        val idDeporte = arguments?.getInt("id_deporte", -1) ?: -1
        Log.i("DEBUG", "ID del deporte recibido en Crear_CarreraFragment: $idDeporte")


        editTextFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            val ano = calendar.get(Calendar.YEAR)
            val mes = calendar.get(Calendar.MONTH)
            val dia = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, anoseleccionado, messeleccionado, diaseleccionado ->
                val fechaFormateada = String.format("%02d/%02d/%04d", diaseleccionado, messeleccionado + 1, anoseleccionado)
                editTextFecha.setText(fechaFormateada)
            }, ano, mes, dia)

            datePickerDialog.show()
        }

        buttonGuardarCarrera.setOnClickListener {
            val nombreCarrera = editTextNombreCarrera.text.toString()
            val localidad = editTextLocalidad.text.toString()
            val fecha = editTextFecha.text.toString()

            if (nombreCarrera.isNotEmpty() && localidad.isNotEmpty() && idDeporte != -1) {
                guardarCarrera(nombreCarrera, localidad,fecha, idDeporte)
            } else {
                Toast.makeText(requireContext(), "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                Log.e("DEBUG", "Error: Datos faltantes o ID del deporte incorrecto")
            }
        }

        return view
    }

    private fun guardarCarrera(nombreCarrera: String, localidad: String,fecha: String, idDeporte: Int) {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("nombre_carrera", nombreCarrera)
            put("localidad", localidad)
            put("fecha", fecha)
            put("id_deporte", idDeporte)
        }

        val resultado = db.insert("carreras", null, valores)
        if (resultado != -1L) {
            Toast.makeText(requireContext(), "Carrera con nombre $nombreCarrera y realizada en  $localidad guardada", Toast.LENGTH_SHORT).show()
            Log.i("SQL", "Carrera insertada en la base de datos con ID: $resultado, Localidad: $localidad , Fecha: $fecha")

            // Volver al Fragment anterior
            requireActivity().supportFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Error al guardar la carrera", Toast.LENGTH_SHORT).show()
            Log.e("SQL", "Error al insertar la carrera")
        }
    }
}