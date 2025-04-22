package com.example.proyectotfg

import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class ListaCarreras : Fragment() {
    private lateinit var dbHelper: BaseDatosEjemplo
    private lateinit var userViewModel: UserViewModel
    private lateinit var listViewCarreras: ListView
    private lateinit var editTextBuscaCarrera: EditText
    private lateinit var adapter: ArrayAdapter<String>
    private var listaCarreras = mutableListOf<Pair<Int, String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_lista_carreras, container, false)

        // Inicializar elementos de la UI
        listViewCarreras = view.findViewById(R.id.listViewCarreras)
        editTextBuscaCarrera = view.findViewById(R.id.editTextTextBuscaCarrera)

        // Inicializar la base de datos y ViewModel
        dbHelper = BaseDatosEjemplo(requireContext(), "ProyectoTFG", null, 1)
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

        val db = dbHelper.readableDatabase

        // Obtener ID del deporte desde UserViewModel
        var idDeporte = userViewModel.deporteSeleccionado.value?.first ?: -1
        Log.i("DEBUG", "Lista_Carreras: ID deporte recibido desde UserViewModel -> $idDeporte")


        if (idDeporte == -1) {
            idDeporte = arguments?.getInt("id_deporte", -1) ?: -1
            Log.w("WARNING", "Lista_Carreras: ID deporte inicial inválido, intentando recuperar desde argumentos -> $idDeporte")
        }

        // Validación final
        if (idDeporte == -1) {
            Log.e("ERROR", "Lista_Carreras: No se pudo recuperar ID del deporte")
            return view
        }

        // Obtener carreras desde la base de datos
        val cursorlistacarreras: Cursor = db.rawQuery(
            "SELECT id, nombre_carrera,localidad FROM carreras WHERE id_deporte = ?",
            arrayOf(idDeporte.toString())
        )

        Log.i("DEBUG", "Lista_Carreras: Ejecutando consulta SQL con ID deporte = $idDeporte")

        if (cursorlistacarreras.moveToFirst()) {
            do {
                val idCarrera = cursorlistacarreras.getInt(0)
                val nombreCarrera = cursorlistacarreras.getString(1)
                val localidad = cursorlistacarreras.getString(2)
                listaCarreras.add(Pair(idCarrera, "$nombreCarrera --- $localidad"))
                Log.i("DEBUG", "Lista_Carreras: Carrera encontrada -> ID: $idCarrera, Nombre: $nombreCarrera, Localidad: $localidad")
            } while (cursorlistacarreras.moveToNext())
        } else {
            Log.w("WARNING", "Lista_Carreras: No se encontraron carreras para el ID de deporte = $idDeporte")
            Toast.makeText(requireContext(), "No hay carreras para este deporte", Toast.LENGTH_SHORT).show()
        }

        cursorlistacarreras.close()

        // Inicializar el adaptador
        adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            listaCarreras.map { it.second }
        )
        listViewCarreras.adapter = adapter

        // Filtro de búsqueda
        editTextBuscaCarrera.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }
        })

        // Mostrar el diálogo al hacer clic en una carrera
        listViewCarreras.setOnItemClickListener { _, _, position, _ ->
            val idCarrera = listaCarreras[position].first
            val nombreCarrera = listaCarreras[position].second

            // Guardar carrera seleccionada
            userViewModel.setCarreraSeleccionada(idCarrera, nombreCarrera)

            mostrarDialogoSeleccion(idCarrera, nombreCarrera)
        }

        return view
    }

    /**
     * @param
     */
    private fun mostrarDialogoSeleccion(idCarrera: Int, nombreCarrera: String) {
        val opciones = arrayOf("Añadir Participante", "Modificar Resultados", "Ver Carrera")

        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona una opción")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> navegarAConsultaCarrera(idCarrera, nombreCarrera, "AnadirParticipante") // Envía el tipo de fragmento
                    1 -> navegarAConsultaCarrera(idCarrera, nombreCarrera, "ModificarResultados")
                    2 -> navegarAConsultaCarrera(idCarrera, nombreCarrera, "VerCarrera")
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    /**
     *
     */
    private fun navegarAConsultaCarrera(idCarrera: Int, nombreCarrera: String, fragmento: String) {
        val idDeporte = userViewModel.deporteSeleccionado.value?.first ?: -1

        Log.i("DEBUG", "Lista_Carreras: Enviando a ConsultaCarrera -> ID deporte=$idDeporte, ID carrera=$idCarrera, Fragmento=$fragmento")

        val intent = Intent(requireContext(), ConsultaCarrera::class.java).apply {
            putExtra("id_deporte", idDeporte)
            putExtra("id_carrera", idCarrera)
            putExtra("nombre_carrera", nombreCarrera)
            putExtra("fragmento", fragmento)
        }
        startActivity(intent)
    }

}