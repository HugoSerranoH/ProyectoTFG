package com.example.proyectotfg

import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment

class ListaParticipantes : Fragment() {
    private lateinit var listViewParticipantes: ListView
    private lateinit var dbHelper: BaseDatosEjemplo
    private var listaParticipantes = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var editTextBuscarParticipante: EditText
    private var listaParticipantesFiltrados = mutableListOf<String>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_lista_participantes, container, false)

        listViewParticipantes = view.findViewById(R.id.listViewParticipantes)
        editTextBuscarParticipante = view.findViewById(R.id.editTextTextBuscaParticipante)
        dbHelper = BaseDatosEjemplo(requireContext(), "ProyectoTFG", null, 1)

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_single_choice, listaParticipantesFiltrados)
        listViewParticipantes.adapter = adapter


        listViewParticipantes.choiceMode = ListView.CHOICE_MODE_SINGLE


        val idCarrera = arguments?.getInt("id_carrera", -1) ?: -1
        cargarParticipantes(idCarrera)

        editTextBuscarParticipante.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarParticipantes(s.toString())
            }
        })

        listViewParticipantes.setOnItemClickListener { _, _, position, _ ->
            val seleccionado = listaParticipantesFiltrados.getOrNull(position)
            Log.i("DEBUG", "Participante seleccionado -> $seleccionado")
        }

        return view
    }

    private fun cargarParticipantes(idCarrera: Int) {
        listaParticipantes.clear()
        val db = dbHelper.readableDatabase

        Log.i("DEBUG", "ListaParticipantes: Cargando participantes de la carrera ID=$idCarrera")

        val cursorparticipantes: Cursor = db.rawQuery("""
            SELECT corredores.nombre,participante_carrera.dorsal
            FROM participante_carrera
            JOIN corredores ON participante_carrera.id_participante = corredores.id
            WHERE participante_carrera.id_carrera = ?
            order by participante_carrera.dorsal asc
        """, arrayOf(idCarrera.toString()))

        if (cursorparticipantes.moveToFirst()) {
            do {
                listaParticipantes.add("${cursorparticipantes.getString(0)},  NºD: ${cursorparticipantes.getInt(1)}")
            } while (cursorparticipantes.moveToNext())
        }
        cursorparticipantes.close()
        listaParticipantesFiltrados.clear()
        listaParticipantesFiltrados.addAll(listaParticipantes)
        adapter.notifyDataSetChanged()
    }

    private fun filtrarParticipantes(texto: String) {
        listaParticipantesFiltrados.clear()
        if (texto.isEmpty()) {
            listaParticipantesFiltrados.addAll(listaParticipantes)
        } else {
            listaParticipantesFiltrados.addAll(listaParticipantes.filter {
                it.contains(texto, ignoreCase = true)
            })
        }
        adapter.notifyDataSetChanged()

    }
    fun obtenerParticipanteSeleccionado(): String? {
        val posicionSeleccionada = listViewParticipantes.checkedItemPosition
        return if (posicionSeleccionada != ListView.INVALID_POSITION) {
            listaParticipantesFiltrados.getOrNull(posicionSeleccionada)
        } else {
            null
        }
    }
}