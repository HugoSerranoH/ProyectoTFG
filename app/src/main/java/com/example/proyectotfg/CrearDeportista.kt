package com.example.proyectotfg

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class CrearDeportista : Fragment() {
    private lateinit var dbHelper: BaseDatosEjemplo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_crear_deportista, container, false)

        // Inicializar base de datos
        dbHelper = BaseDatosEjemplo(requireContext(), "ProyectoTFG", null, 1)

        // Obtener el ID del deporte desde los argumentos y verificar su recepción
        val idDeporte = arguments?.getInt("id_deporte", -1) ?: -1
        Log.i("DEBUG", "ID del deporte recibido en Crear_DeportistaFragment (antes de usarlo): $idDeporte")

        // Capturar referencias de los elementos del formulario
        val editTextNombre = view.findViewById<EditText>(R.id.editTextTextNombreDeportista)
        val editTextEquipo = view.findViewById<EditText>(R.id.editTextTextNombreEquipo)
        val editTextEdad = view.findViewById<EditText>(R.id.editTextTextEdadDeportista)
        val spinnerGenero = view.findViewById<Spinner>(R.id.spinnerGeneroDeportista)
        val buttonGuardar = view.findViewById<Button>(R.id.buttonGuardarDeportista)

        // Configurar el Spinner con opciones de género desde strings.xml
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sexo_opciones, // Accede al string-array de valores en res/values/strings.xml
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGenero.adapter = adapter

        buttonGuardar.setOnClickListener {
            val nombre = editTextNombre.text.toString()
            val equipo = editTextEquipo.text.toString()
            val edad = editTextEdad.text.toString().toIntOrNull() ?: 0 // Evita errores si el campo está vacío
            val genero = spinnerGenero.selectedItem.toString()

            // **PRUEBAS DE DEPURACIÓN**
            Log.i("DEBUG", "Nombre ingresado: $nombre")
            Log.i("DEBUG", "Equipo ingresado: $equipo")
            Log.i("DEBUG", "Edad ingresada: $edad")
            Log.i("DEBUG", "Género seleccionado: $genero")
            Log.i("DEBUG", "ID del deporte recibido en Crear_DeportistaFragment (final): $idDeporte")

            if (nombre.isNotEmpty() && edad > 0 && idDeporte != -1) {
                guardarDeportista(nombre, equipo, edad, genero, idDeporte)
            } else {
                Toast.makeText(requireContext(), "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                Log.e("DEBUG", "Error: Datos faltantes o ID del deporte incorrecto")
            }
        }

        return view
    }

    /**
     *
     */
    private fun guardarDeportista(nombre: String, equipo: String, edad: Int, genero: String, idDeporte: Int) {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("nombre", nombre)
            put("equipo", equipo)
            put("edad", edad)
            put("genero", genero)
            put("id_deporte", idDeporte)
        }

        val resultado = db.insert("corredores", null, valores)
        if (resultado != -1L) {
            Toast.makeText(requireContext(), "Deportista guardado correctamente", Toast.LENGTH_SHORT).show()
            Log.i("SQL", "Deportista insertado en la base de datos con ID: $resultado")

            // Volver al Fragment anterior
            requireActivity().supportFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Error al guardar el deportista", Toast.LENGTH_SHORT).show()
            Log.e("SQL", "Error al insertar el deportista")
        }
    }
}