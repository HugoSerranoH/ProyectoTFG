package com.example.proyectotfg

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest

class VerResultados : Fragment() {
    private lateinit var listViewVerResultados: ListView
    private lateinit var dbHelper: BaseDatosEjemplo
    private lateinit var buttonvolver: Button
    private lateinit var buttoncsv: Button
    private lateinit var buttonpdf: Button
    private var idCarrera: Int = -1
    private var listaResultados = mutableListOf<String>()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_ver_resultados, container, false)

        listViewVerResultados = view.findViewById(R.id.ListViewVerResultados)
        buttonvolver = view.findViewById(R.id.buttonvolvercarreras)
        buttoncsv = view.findViewById(R.id.buttonexportaracsv)
        buttonpdf = view.findViewById(R.id.buttonexportarapdf)
        dbHelper = BaseDatosEjemplo(requireContext(), "ProyectoTFG", null, 1)


        idCarrera = requireActivity().intent.getIntExtra("id_carrera", -1)

        Log.i("DEBUG", "VerResultados: ID de carrera recibido -> $idCarrera")

        buttoncsv.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val permiso = Manifest.permission.WRITE_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(requireContext(), permiso) == PackageManager.PERMISSION_GRANTED) {
                    guardarCSV()
                } else {
                    requestPermissions(arrayOf(permiso), 123)
                }
            } else {
                guardarCSV()
            }
        }

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

    private fun generarCSVResultados(): String {
        val db = dbHelper.readableDatabase
        val builder = StringBuilder()

       //Columnas del CSV
        builder.append("Posición,Nombre,Equipo,Dorsal,Tiempo\n")

        val cursor = db.rawQuery("""
        SELECT r.posicion, c.nombre, c.equipo, p.dorsal, r.tiempo
        FROM resultados_carrera r
        JOIN participante_carrera p ON r.id_participante_carrera = p.id
        JOIN corredores c ON p.id_participante = c.id
        WHERE p.id_carrera = ?
        ORDER BY r.posicion ASC
    """, arrayOf(idCarrera.toString()))

        if (cursor.moveToFirst()) {
            do {
                val posicion = cursor.getInt(0)
                val nombre = cursor.getString(1)
                val equipo = cursor.getString(2) ?: "Sin equipo"
                val dorsal = cursor.getInt(3)
                val tiempo = cursor.getString(4)

                // Asegura que los campos con comas o espacios se manejen bien con comillas
                builder.append("\"$posicion\",\"$nombre\",\"$equipo\",\"$dorsal\",\"$tiempo\"\n")
            } while (cursor.moveToNext())
        }
        cursor.close()
        return builder.toString()
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun guardarCSV() {

        val db = dbHelper.readableDatabase
        val cursornombrecarrera = db.rawQuery("SELECT nombre_carrera FROM carreras WHERE id = ?", arrayOf(idCarrera.toString()))

        var nombreCarrera = "Carrera"

        if (cursornombrecarrera.moveToFirst()) {
            nombreCarrera = cursornombrecarrera.getString(0).replace(" ", "_")
        }

        cursornombrecarrera.close()
        val nombrearchivo = "${nombreCarrera}.csv"
        val csvContent = generarCSVResultados()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Para Android 10+ (Que no sé si Android 10 entra y mi movil lo es
            val resolver = requireContext().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, nombrearchivo)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                resolver.openOutputStream(it)?.use { output ->
                    output.write(csvContent.toByteArray())
                    Toast.makeText(requireContext(), "CSV guardado en Descargas", Toast.LENGTH_LONG).show()
                }
            } ?: run {
                Toast.makeText(requireContext(), "No se pudo guardar el archivo CSV", Toast.LENGTH_SHORT).show()
            }

        } else {
            // Para Android 9 o menos
            val estado = Environment.getExternalStorageState()
            if (estado == Environment.MEDIA_MOUNTED) {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, nombrearchivo)

                try {
                    FileOutputStream(file).use { output ->
                        output.write(csvContent.toByteArray())
                        Toast.makeText(requireContext(), "CSV guardado en Descargas", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error al guardar CSV: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "No hay acceso al almacenamiento", Toast.LENGTH_SHORT).show()
            }
        }
    }
}