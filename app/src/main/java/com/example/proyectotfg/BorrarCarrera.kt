package com.example.proyectotfg

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class BorrarCarrera : Fragment() {
    private lateinit var dbHelper: BaseDatosEjemplo
    private lateinit var spinnerCarreras: Spinner
    private lateinit var buttonEliminarCarrera: Button
    private var listaCarreras = mutableListOf<Pair<Int, String>>() // Lista de carreras (ID, Nombre)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_borrar_carrera, container, false)


        dbHelper = BaseDatosEjemplo(requireContext(), "ProyectoTFG", null, 1)


        spinnerCarreras = view.findViewById(R.id.spinnerSeleccionCarrera)
        buttonEliminarCarrera = view.findViewById(R.id.buttonEliminarCarrera)

        // Obtener el ID del deporte
        val idDeporte = arguments?.getInt("id_deporte", -1) ?: -1
        Log.i("DEBUG", "ID del deporte recibido en Borrar_CarreraFragment: $idDeporte")

        if (idDeporte != -1) {
            cargarCarreras(idDeporte)
        } else {
            Toast.makeText(requireContext(), "Error: ID del deporte no válido", Toast.LENGTH_SHORT).show()
        }

        buttonEliminarCarrera.setOnClickListener {
            val posicionSeleccionada = spinnerCarreras.selectedItemPosition
            if (posicionSeleccionada >= 0) {
                val idCarreraSeleccionada = listaCarreras[posicionSeleccionada].first
                eliminarCarrera(idCarreraSeleccionada)
            } else {
                Toast.makeText(requireContext(), "Selecciona una carrera para eliminar", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun cargarCarreras(idDeporte: Int) {
        val db = dbHelper.readableDatabase
        listaCarreras.clear()

        val cursor: Cursor = db.rawQuery(
            "SELECT id, nombre_carrera FROM carreras WHERE id_deporte = ?",
            arrayOf(idDeporte.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val idCarrera = cursor.getInt(0)
                val nombreCarrera = cursor.getString(1)
                listaCarreras.add(Pair(idCarrera, nombreCarrera))
            } while (cursor.moveToNext())
        }
        cursor.close()

        if (listaCarreras.isNotEmpty()) {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listaCarreras.map { it.second }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCarreras.adapter = adapter
        } else {
            Toast.makeText(requireContext(), "No hay carreras disponibles en este deporte", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarCarrera(idCarrera: Int) {
        val db = dbHelper.writableDatabase
        val resultado = db.delete("carreras", "id = ?", arrayOf(idCarrera.toString()))

        if (resultado > 0) {
            Toast.makeText(requireContext(), "Carrera eliminada correctamente", Toast.LENGTH_SHORT).show()
            Log.i("SQL", "Carrera eliminada con ID: $idCarrera")
            cargarCarreras(arguments?.getInt("id_deporte", -1) ?: -1) // Recargar lista después de eliminar
        } else {
            Toast.makeText(requireContext(), "Error al eliminar la carrera", Toast.LENGTH_SHORT).show()
            Log.e("SQL", "Error al eliminar la carrera con ID: $idCarrera")
        }
    }
}