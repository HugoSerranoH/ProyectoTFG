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
                spinner.setTag(position)


                // Al seleccionar un corredor en el Spinner, se añade su id en la lista de manera ordenada
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, positionSpinner: Int, id: Long) {
                        // Obtienes el indice en el que se encuentra
                        val index = spinner.getTag() as Int
                        val idCorredorSeleccionado = corredoresDisponibles[positionSpinner].first


                        if (index >= corredoresOrdenados.size) {
                            corredoresOrdenados.add(idCorredorSeleccionado)
                        } else {
                            corredoresOrdenados[index] = idCorredorSeleccionado
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {

                    }
                }


                val editTextTiempo = EditText(context)
                editTextTiempo.hint = "Tiempo (hh:mm:ss)"
                editTextTiempo.setSingleLine(true)

                // Esto hace que se muestre el cuadro donde añadir el tiempo
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

                                if (tiempoIngresado.matches(Regex("^\\d{2}:\\d{2}:\\d{2}\$"))) {
                                    corredoresTiempos[position] = tiempoIngresado
                                    editTextTiempo.setText(tiempoIngresado)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Formato incorrecto. Usa hh:mm:ss",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }

                }
                    container.addView(spinner)
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

    private fun obtenerNumeroCorredores(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM participante_carrera WHERE id_carrera = ?", arrayOf(idCarrera.toString()))

        var cantidad = 0
        if (cursor.moveToFirst()) {
            cantidad = cursor.getInt(0)
        }
        cursor.close()
        return cantidad
    }

    private fun obtenerListaCorredores(): List<Pair<Int, String>> {
        val db = dbHelper.readableDatabase
        val listaCorredores = mutableListOf<Pair<Int, String>>()

        listaCorredores.add(Pair(-1, "Agregar corredor"))

        val cursor = db.rawQuery("""
        SELECT participante_carrera.id, corredores.nombre, participante_carrera.dorsal 
        FROM participante_carrera 
        JOIN corredores ON participante_carrera.id_participante = corredores.id
        WHERE participante_carrera.id_carrera = ?
    """, arrayOf(idCarrera.toString()))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val nombre = cursor.getString(1)
                val dorsal = cursor.getInt(2)
                listaCorredores.add(Pair(id, "Dorsal: $dorsal - $nombre"))
            } while (cursor.moveToNext())
        }
        cursor.close()

        return listaCorredores
    }

    private fun actualizarResultados() {
        val db = dbHelper.writableDatabase

        Log.i("DEBUG", "Ejecutando DELETE de resultados para la carrera ID: $idCarrera")
        db.execSQL("DELETE FROM resultados_carrera WHERE id_participante_carrera IN (SELECT id FROM participante_carrera WHERE id_carrera = ?)", arrayOf(idCarrera.toString()))

        listaPosiciones.forEachIndexed { index, resultado ->
            val idCorredor = corredoresOrdenados.getOrNull(index) // Obtenemos el ID del corredor según el orden en la lista
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