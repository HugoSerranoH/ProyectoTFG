package com.example.proyectotfg

import android.app.AlertDialog
import android.content.ContentValues
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
    private lateinit var Carreranombre: String
    private var idDeporte2: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_lista_carreras, container, false)


        listViewCarreras = view.findViewById(R.id.listViewCarreras)
        editTextBuscaCarrera = view.findViewById(R.id.editTextTextBuscaCarrera)


        dbHelper = BaseDatosEjemplo(requireContext(), "ProyectoTFG", null, 1)
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

        val db = dbHelper.readableDatabase


        var idDeporte = userViewModel.deporteSeleccionado.value?.first ?: -1
//        Log.i("DEBUG", "Lista_Carreras: ID deporte recibido desde UserViewModel -> $idDeporte")


        if (idDeporte == -1) {
            idDeporte = arguments?.getInt("id_deporte", -1) ?: -1
            idDeporte2 = idDeporte
            Log.w("WARNING", "Lista_Carreras: ID deporte inicial inválido, intentando recuperar desde argumentos -> $idDeporte")
        }


        if (idDeporte == -1) {
//            Log.e("ERROR", "Lista_Carreras: No se pudo recuperar ID del deporte")
            return view
        }


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
//                Log.i("DEBUG", "Lista_Carreras: Carrera encontrada -> ID: $idCarrera, Nombre: $nombreCarrera, Localidad: $localidad")
            } while (cursorlistacarreras.moveToNext())
        } else {
//            Log.i("WARNING", "Lista_Carreras: No se encontraron carreras para el ID de deporte = $idDeporte")
            Toast.makeText(requireContext(), "No hay carreras para este deporte", Toast.LENGTH_SHORT).show()
        }

        cursorlistacarreras.close()

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listaCarreras.map { it.second })
        listViewCarreras.adapter = adapter


        editTextBuscaCarrera.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }
        })

        // Mostrar el diálogo al hacer clic en una carrera
        listViewCarreras.setOnItemClickListener { _, _, position, _ ->
            val carreraSeleccionada = adapter.getItem(position)
            Carreranombre = carreraSeleccionada?.split("---")?.first()?.trim() ?: ""
            val idCarrera = listaCarreras.find { it.second == carreraSeleccionada }?.first
            if (idCarrera != null) {
                val nombreCarrera = carreraSeleccionada ?: ""
                userViewModel.setCarreraSeleccionada(idCarrera, nombreCarrera)
                mostrarDialogoSeleccion(idCarrera, nombreCarrera)
            } else {
                Toast.makeText(requireContext(), "Error al seleccionar la carrera", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    /**
     * @param
     */
    private fun mostrarDialogoSeleccion(idCarrera: Int, nombreCarrera: String) {
        val localidadActual = listaCarreras.find { it.first == idCarrera }?.second?.split("---")?.last()?.trim() ?: ""
        val opciones = arrayOf("Añadir Participante", "Modificar Resultados", "Ver Carrera","Cambiar datos")

        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona una opción")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> navegarAConsultaCarrera(idCarrera, nombreCarrera, "AnadirParticipante") // Envía el tipo de fragmento
                    1 -> navegarAConsultaCarrera(idCarrera, nombreCarrera, "ModificarResultados")
                    2 -> navegarAConsultaCarrera(idCarrera, nombreCarrera, "VerCarrera")
                    3 -> mostrarDialogoModificar(idCarrera, Carreranombre, localidadActual)
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

//        Log.i("DEBUG", "Lista_Carreras: Enviando a ConsultaCarrera -> ID deporte=$idDeporte, ID carrera=$idCarrera, Fragmento=$fragmento")

        val intent = Intent(requireContext(), ConsultaCarrera::class.java).apply {
            putExtra("id_deporte", idDeporte)
            putExtra("id_carrera", idCarrera)
            putExtra("nombre_carrera", Carreranombre)
            putExtra("fragmento", fragmento)
        }
        startActivity(intent)
    }

    private fun mostrarDialogoModificar(idCarrera: Int, nombreCarreraActual: String, localidadActual: String) {
        val textViewNombre = TextView(requireContext()).apply {
            text = "Nuevo Nombre :"
            textSize = 16f
            setPadding(0, 6, 0, 6)
        }
        val editTextNombre = EditText(requireContext()).apply {
            hint = "Nuevo nombre"
            setText(nombreCarreraActual)
        }
        val textViewLocalidad = TextView(requireContext()).apply {
            text = "Nueva Localidad :"
            textSize = 16f
            setPadding(0, 6, 0, 6)
        }
        val editTextLocalidad = EditText(requireContext()).apply {
            hint = "Nueva localidad"
            setText(localidadActual)
        }
        val layoutdeldialog = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            addView(textViewNombre)
            addView(editTextNombre)
            addView(textViewLocalidad)
            addView(editTextLocalidad)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar datos de la carrera")
            .setView(layoutdeldialog)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = editTextNombre.text.toString().trim()
                val nuevaLocalidad = editTextLocalidad.text.toString().trim()
                if (nuevoNombre.isEmpty() || nuevaLocalidad.isEmpty()) {
                    Toast.makeText(requireContext(), "Los campos no pueden estar vacíos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                actualizarDatos(idCarrera, nuevoNombre, nuevaLocalidad)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarDatos(idCarrera: Int, nuevoNombre: String, nuevaLocalidad: String) {
        val db = dbHelper.writableDatabase

        val actualizardatos = """UPDATE carreras SET nombre_carrera = ?, localidad = ? WHERE id = ?"""
        db.execSQL(actualizardatos, arrayOf(nuevoNombre, nuevaLocalidad, idCarrera.toString()))

        val cursoractualizardatos = db.rawQuery("SELECT nombre_carrera, localidad FROM carreras WHERE id = ?", arrayOf(idCarrera.toString()))
        if (cursoractualizardatos.moveToFirst()) {
            val nombreActualizado = cursoractualizardatos.getString(0)
            val localidadActualizada = cursoractualizardatos.getString(1)

            if (nombreActualizado == nuevoNombre && localidadActualizada == nuevaLocalidad) {
                Toast.makeText(requireContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                recargarListaCarreras()
            } else {
                Toast.makeText(requireContext(), "Datos no actualizados", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Error: Carrera no encontrada", Toast.LENGTH_SHORT).show()
        }
        cursoractualizardatos.close()
    }

    private fun recargarListaCarreras() {
        listaCarreras.clear()

        val db = dbHelper.readableDatabase
        val cursorlistacarrerasactualizar = db.rawQuery(
            "SELECT id, nombre_carrera, localidad FROM carreras WHERE id_deporte = ?",
            arrayOf(idDeporte2.toString())
        )

        if (cursorlistacarrerasactualizar.moveToFirst()) {
            do {
                val idCarrera = cursorlistacarrerasactualizar.getInt(0)
                val nombreCarrera = cursorlistacarrerasactualizar.getString(1)
                val localidad = cursorlistacarrerasactualizar.getString(2)
                listaCarreras.add(Pair(idCarrera, "$nombreCarrera --- $localidad"))
            } while (cursorlistacarrerasactualizar.moveToNext())
        }

        cursorlistacarrerasactualizar.close()
        adapter.clear()
        adapter.addAll(listaCarreras.map { it.second })
        adapter.notifyDataSetChanged()

    }

}