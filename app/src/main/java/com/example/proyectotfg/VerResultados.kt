package com.example.proyectotfg

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment

class VerResultados : Fragment() {
    private lateinit var listViewVerResultados: ListView
    private lateinit var dbHelper: BaseDatosEjemplo
    private lateinit var buttonvolver: Button
    private var idCarrera: Int = -1
    private var listaResultados = mutableListOf<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_ver_resultados, container, false)

        listViewVerResultados = view.findViewById(R.id.ListViewVerResultados)
        buttonvolver = view.findViewById(R.id.buttonvolvercarreras)
        dbHelper = BaseDatosEjemplo(requireContext(), "ProyectoTFG", null, 1)


        idCarrera = requireActivity().intent.getIntExtra("id_carrera", -1)

        Log.i("DEBUG", "VerResultados: ID de carrera recibido -> $idCarrera")

        buttonvolver.setOnClickListener {
            val db = dbHelper.readableDatabase
            val cursordeportever: Cursor = db.rawQuery("SELECT id_deporte FROM carreras WHERE id = ?", arrayOf(idCarrera.toString()))

            var idDeporte: Int? = null
            if (cursordeportever.moveToFirst()) {
                idDeporte = cursordeportever.getInt(0)
//                Log.i("DEBUG", "idDeporte obtenido -> $idDeporte")
            } else {
//                Log.e("ERROR", "No se encontró idDeporte para la carrera con ID -> $idCarrera")
            }
            cursordeportever.close()


            if (idDeporte != null) {
                val intent = Intent(requireActivity(), SeleccionarCarrera::class.java)
                intent.putExtra("id_deporte", idDeporte)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Error: No se pudo obtener el deporte asociado", Toast.LENGTH_SHORT).show()
            }
        }


        if (idCarrera == -1) {
            Toast.makeText(requireContext(), "Error: No se recibió un ID de carrera válido", Toast.LENGTH_SHORT).show()
            return view
        }


        cargarResultados()

        return view
    }

    private fun cargarResultados() {
        listaResultados.clear()
        val db = dbHelper.readableDatabase

//        Log.i("DEBUG", "Ejecutando SELECT de resultados para la carrera ID: $idCarrera")

        val cursorverresultados: Cursor = db.rawQuery("""
                    SELECT r.posicion, c.nombre, c.equipo,p.dorsal, r.tiempo
                    FROM resultados_carrera r
                    JOIN participante_carrera p ON r.id_participante_carrera = p.id
                    JOIN corredores c ON p.id_participante = c.id
                    WHERE p.id_carrera = ?
                    ORDER BY r.posicion ASC
                    """, arrayOf(idCarrera.toString()))


        if (cursorverresultados.moveToFirst()) {
//            Log.i("DEBUG", "Resultados encontrados para la carrera ID: $idCarrera")

            do {
                val posicion = cursorverresultados.getInt(0)
                val nombre = cursorverresultados.getString(1)
                val equipo = cursorverresultados.getString(2) ?: "Sin equipo"
                val dorsal = cursorverresultados.getInt(3)
                val tiempo = cursorverresultados.getString(4)

                //listaResultados.add("$posicion.  $nombre      ($equipo)      $dorsal     $tiempo")
                listaResultados.add(String.format("%-4d %-20s %-20s %-5d %s", posicion, nombre, "($equipo)", dorsal, tiempo))
//                Log.i("DEBUG", "Posición: $posicion, Nombre: $nombre, Equipo: $equipo, Tiempo: $tiempo")
            } while (cursorverresultados.moveToNext())
        } else {
//            Log.i("DEBUG", "No hay resultados para la carrera ID: $idCarrera")
            Toast.makeText(requireContext(), "No hay resultados para esta carrera", Toast.LENGTH_SHORT).show()
        }
        cursorverresultados.close()

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listaResultados)
        listViewVerResultados.adapter = adapter
    }
}