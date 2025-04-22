package com.example.proyectotfg

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment

class ListaParticipantes : Fragment() {
    private lateinit var listViewParticipantes: ListView
    private lateinit var dbHelper: BaseDatosEjemplo
    private var listaParticipantes = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_lista_participantes, container, false)

        listViewParticipantes = view.findViewById(R.id.listViewParticipantes)
        dbHelper = BaseDatosEjemplo(requireContext(), "ProyectoTFG", null, 1)

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_single_choice, listaParticipantes)
        listViewParticipantes.adapter = adapter


        listViewParticipantes.choiceMode = ListView.CHOICE_MODE_SINGLE


        val idCarrera = arguments?.getInt("id_carrera", -1) ?: -1
        cargarParticipantes(idCarrera)


        listViewParticipantes.setOnItemClickListener { _, _, position, _ ->
            val seleccionado = listaParticipantes[position]
            Log.i("DEBUG", "Participante seleccionado -> $seleccionado")
        }

        return view
    }

    private fun cargarParticipantes(idCarrera: Int) {
        listaParticipantes.clear()
        val db = dbHelper.readableDatabase

        Log.i("DEBUG", "ListaParticipantes: Cargando participantes de la carrera ID=$idCarrera")

        val cursor: Cursor = db.rawQuery("""
            SELECT corredores.nombre
            FROM participante_carrera
            JOIN corredores ON participante_carrera.id_participante = corredores.id
            WHERE participante_carrera.id_carrera = ?
        """, arrayOf(idCarrera.toString()))

        if (cursor.moveToFirst()) {
            do {
                listaParticipantes.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()

        adapter.notifyDataSetChanged()
    }

    fun obtenerParticipanteSeleccionado(): String? {
        val posicionSeleccionada = listViewParticipantes.checkedItemPosition
        return if (posicionSeleccionada != ListView.INVALID_POSITION) {
            listaParticipantes[posicionSeleccionada]
        } else {
            null
        }
    }
}