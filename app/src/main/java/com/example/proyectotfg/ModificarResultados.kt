package com.example.proyectotfg

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class ModificarResultados : Fragment() {
    private lateinit var listViewResultados: ListView
    private lateinit var buttonActualizarResultados: Button
    private lateinit var dbHelper: BaseDatosEjemplo
    private var listaPosiciones = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private var idCarrera: Int = -1
    private var corredoresDisponibles = listOf<Pair<Int, String>>()
    private var corredoresTiempos = mutableMapOf<Int, String>()
    private val corredoresOrdenados = mutableListOf<Int?>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_modificar_resultados, container, false)

        listViewResultados = view.findViewById(R.id.listViewResultados)
        buttonActualizarResultados = view.findViewById(R.id.buttonActualizarResultados)
        dbHelper = BaseDatosEjemplo(requireContext(), "ProyectoTFG", null, 1)

        idCarrera = requireActivity().intent.getIntExtra("id_carrera", -1)
        if (idCarrera == -1) {
            Toast.makeText(requireContext(), "Error: No se recibió un ID de carrera válido", Toast.LENGTH_SHORT).show()
            return view
        }


        corredoresDisponibles = obtenerListaCorredores()
        inicializarListaPosiciones()
        cargarResultadosExistentes()


        adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, listaPosiciones) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val row = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)

                val container = LinearLayout(context)
                container.orientation = LinearLayout.HORIZONTAL

                val textViewPosicion = TextView(context)
                textViewPosicion.text = "${position + 1}."
                container.addView(textViewPosicion)

                val spinner = Spinner(context)
                val spinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, corredoresDisponibles.map { it.second })
                spinner.adapter = spinnerAdapter

                val idCorredorSeleccionado = corredoresOrdenados.getOrNull(position)
                if (idCorredorSeleccionado != null) {
                    val index = corredoresDisponibles.indexOfFirst { it.first == idCorredorSeleccionado }
                    if (index >= 0) spinner.setSelection(index)
                }

                // Al seleccionar un corredor en el Spinner, se añade su id en la lista de manera ordenada
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, positionSpinner: Int, id: Long) {
                        val idCorredorSeleccionado = corredoresDisponibles[positionSpinner].first

                        if (idCorredorSeleccionado == -1) {
                            return
                        }

                        if (idCorredorSeleccionado in corredoresOrdenados && corredoresOrdenados.indexOf(idCorredorSeleccionado) != position) {
                            Toast.makeText(context, "Este corredor ya está en el resultado", Toast.LENGTH_LONG).show()
                            val indiceAnterior = corredoresDisponibles.indexOfFirst { it.first == corredoresOrdenados.getOrNull(position) }
                            if (indiceAnterior >= 0) {
                                spinner.setSelection(indiceAnterior)
                            }
                            return
                        }
                        if (position >= corredoresOrdenados.size) {
                            corredoresOrdenados.add(idCorredorSeleccionado)
                        } else {
                            corredoresOrdenados[position] = idCorredorSeleccionado
                        }
                        Log.i("DEBUG", "Seleccionado corredor para posición $position: ID $idCorredorSeleccionado")
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

                container.addView(spinner)

                val editTextTiempo = EditText(context)
                editTextTiempo.hint = "Tiempo (hh:mm:ss)"
                editTextTiempo.setSingleLine(true)

                editTextTiempo.setText(corredoresTiempos[position] ?: "")

                editTextTiempo.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        val editTextInput = EditText(requireContext())
                        editTextInput.hint = "hh:mm:ss"
                        editTextInput.setSingleLine(true)

                        AlertDialog.Builder(requireContext())
                            .setTitle("Ingresar tiempo")
                            .setView(editTextInput)
                            .setPositiveButton("Guardar") { _, _ ->
                                val tiempoIngresado = editTextInput.text.toString().trim()

                                if (tiempoIngresado.matches(Regex("^(\\d{2}:\\d{2}:\\d{2}|DNF|DNS|DSQ|\\+\\d+ vuelta[s]?|\\+ \\d+ vuelta[s]?)\$", RegexOption.IGNORE_CASE))) {
                                    corredoresTiempos[position] = tiempoIngresado
                                    editTextTiempo.setText(tiempoIngresado)
                                    Log.i("DEBUG", "Tiempo ingresado para posición $position: $tiempoIngresado")

                                } else {
                                    Toast.makeText(
                                        context,
                                        "Formato incorrecto. Usa hh:mm:ss, '+ x vueltas' o (DNF, DNS, DSQ)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    editTextTiempo.setText(corredoresTiempos[position] ?: "")
                                }
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                }

                container.addView(editTextTiempo)

                return container
            }
        }

        listViewResultados.adapter = adapter

        buttonActualizarResultados.setOnClickListener {
            actualizarResultados()
        }

        return view
    }

    private fun inicializarListaPosiciones() {
        listaPosiciones.clear()
        val corredores = obtenerListaCorredores()

        for ((idCorredor, nombre) in corredores) {
            if (idCorredor != -1) {
                listaPosiciones.add("$idCorredor - $nombre")
            }
        }
    }

    private fun obtenerListaCorredores(): List<Pair<Int, String>> {
        val db = dbHelper.readableDatabase
        val listaCorredores = mutableListOf<Pair<Int, String>>()

        listaCorredores.add(Pair(-1, "Agregar corredor"))

        val cursorobtenerlista = db.rawQuery("""
        SELECT participante_carrera.id, corredores.nombre, participante_carrera.dorsal 
        FROM participante_carrera 
        JOIN corredores ON participante_carrera.id_participante = corredores.id
        WHERE participante_carrera.id_carrera = ?
        order by participante_carrera.dorsal asc
    """, arrayOf(idCarrera.toString()))

        if (cursorobtenerlista.moveToFirst()) {
            do {
                val id = cursorobtenerlista.getInt(0)
                val nombre = cursorobtenerlista.getString(1)
                val dorsal = cursorobtenerlista.getInt(2)
                listaCorredores.add(Pair(id, "Nº: $dorsal - $nombre"))
            } while (cursorobtenerlista.moveToNext())
        }
        cursorobtenerlista.close()

        return listaCorredores
    }

    private fun cargarResultadosExistentes() {
        val db = dbHelper.readableDatabase

        val cursorexistentes = db.rawQuery("""
        SELECT rc.posicion, pc.id, rc.tiempo 
        FROM resultados_carrera rc
        JOIN participante_carrera pc ON rc.id_participante_carrera = pc.id
        WHERE pc.id_carrera = ?
        ORDER BY rc.posicion ASC
        """, arrayOf(idCarrera.toString()))

        corredoresOrdenados.clear()
        corredoresTiempos.clear()

        if (cursorexistentes.moveToFirst()) {
            do {
                val posicion = cursorexistentes.getInt(0) - 1
                val idParticipante = cursorexistentes.getInt(1)
                val tiempo = cursorexistentes.getString(2)

                while (corredoresOrdenados.size <= posicion) corredoresOrdenados.add(null)
                corredoresOrdenados[posicion] = idParticipante
                corredoresTiempos[posicion] = tiempo

            } while (cursorexistentes.moveToNext())
        }

        cursorexistentes.close()
    }


    private fun actualizarResultados() {
        val db = dbHelper.writableDatabase

        Log.i("DEBUG", "Ejecutando DELETE de resultados para la carrera ID: $idCarrera")
        db.execSQL("DELETE FROM resultados_carrera WHERE id_participante_carrera IN (SELECT id FROM participante_carrera WHERE id_carrera = ?)", arrayOf(idCarrera.toString()))

        listaPosiciones.forEachIndexed { index, resultado ->
            val idCorredor = corredoresOrdenados.getOrNull(index)
            val tiempo = corredoresTiempos[index] ?: "00:00:00"

            Log.i("DEBUG", "Procesando posición $index -> Resultado: $resultado")

            if (idCorredor != null) {
                Log.i("DEBUG", "Preparando INSERT: ID Corredor $idCorredor, Posición ${index + 1}, Tiempo $tiempo")

                db.execSQL("INSERT INTO resultados_carrera (id_participante_carrera, posicion, tiempo) VALUES (?, ?, ?)",
                    arrayOf(idCorredor.toString(), (index + 1).toString(), tiempo))

                Log.i("DEBUG", "INSERT ejecutado correctamente para ID Corredor $idCorredor")
            } else {
                Log.e("ERROR", "No se pudo extraer un ID válido de la lista de posiciones en el índice $index")
            }
        }


    // SELECT PARA COMPROBAR ANTES DE CREAR VER CARRERA
//        val cursor = db.rawQuery(
//            "SELECT id_participante_carrera, posicion, tiempo FROM resultados_carrera WHERE id_participante_carrera IN (SELECT id FROM participante_carrera WHERE id_carrera = ?)",
//            arrayOf(idCarrera.toString())
//        )
//
//        if (cursor.moveToFirst()) {
//            Log.i("DEBUG", "=== RESULTADOS AÑADIDOS ===")
//            do {
//                val idParticipante = cursor.getInt(0)
//                val posicion = cursor.getInt(1)
//                val tiempo = cursor.getString(2)
//                Log.i("DEBUG", "ID Participante: $idParticipante, Posición: $posicion, Tiempo: $tiempo")
//            } while (cursor.moveToNext())
//        } else {
//            Log.i("DEBUG", "No se encontraron resultados guardados.")
//        }
//
//        cursor.close()

        Toast.makeText(requireContext(), "Resultados actualizados correctamente", Toast.LENGTH_SHORT).show()
    }
}