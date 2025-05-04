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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.TextView

class VerResultados : Fragment() {
    private lateinit var listViewVerResultados: ListView
    private lateinit var dbHelper: BaseDatosEjemplo
    private lateinit var buttonvolver: Button
    private lateinit var buttoncsv: Button
    private lateinit var buttonpdf: Button
    private var idCarrera: Int = -1
    private var listaResultados = mutableListOf<String>()
    private lateinit var textviewequipo : TextView
    private lateinit var textviewdorsal : TextView

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_ver_resultados, container, false)

        listViewVerResultados = view.findViewById(R.id.ListViewVerResultados)
        buttonvolver = view.findViewById(R.id.buttonvolvercarreras)
        buttoncsv = view.findViewById(R.id.buttonexportaracsv)
        buttonpdf = view.findViewById(R.id.buttonexportarapdf)
        textviewequipo = view.findViewById(R.id.textViewVerEquipo)
        textviewdorsal = view.findViewById(R.id.textViewVerDorsal)
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

        buttonpdf.setOnClickListener {
            guardarPDF()
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

        val db = dbHelper.readableDatabase
        val cursornombreDeporte2 = db.rawQuery(
            "SELECT d.nombre_deporte FROM deportes d INNER JOIN carreras c ON d.id = c.id_deporte WHERE c.id = ?",
            arrayOf(idCarrera.toString())
        )

        if (cursornombreDeporte2.moveToFirst()) {
            val nombreDeporte = cursornombreDeporte2.getString(0)
            cursornombreDeporte2.close()

            when {
                nombreDeporte.equals("Karts", ignoreCase = true) -> {
                    textviewequipo.text = "Escudería"
                    textviewdorsal.text = "Número"
                }
                else -> {
                    textviewequipo.text = "Equipo"
                    textviewdorsal.text = "Dorsal"
                }
            }
        }

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

    /**
     *
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun guardarPDF() {
        val db = dbHelper.readableDatabase
        val cursorNombreCarrera = db.rawQuery("SELECT nombre_carrera FROM carreras WHERE id = ?", arrayOf(idCarrera.toString()))
        var nombreCarrera = "Carrera"
        if (cursorNombreCarrera.moveToFirst()) {
            nombreCarrera = cursorNombreCarrera.getString(0)
        }
        cursorNombreCarrera.close()
        val iconosDeporte = mapOf(
            "Ciclismo" to R.drawable.bicycle_icon,
            "Atletismo" to R.drawable.sprint_icon,
            "Karts" to R.drawable.kart_icon
        )
        val coloresFondo = mapOf(
            1 to R.color.light_grey,
            2 to R.color.light_brown,
            3 to R.color.lime
        )

        var deporteCiclismo = false
        var deporteAtletismo = false
        var deporteKarts = false
        var nombreDeporte = ""
        val cursorDeporte = db.rawQuery(
            "SELECT d.nombre_deporte FROM deportes d INNER JOIN carreras c ON d.id = c.id_deporte WHERE c.id = ?",
            arrayOf(idCarrera.toString())
        )
        if (cursorDeporte.moveToFirst()) {
            nombreDeporte = cursorDeporte.getString(0)
            deporteCiclismo = nombreDeporte.equals("Ciclismo", ignoreCase = true)
            deporteAtletismo = nombreDeporte.equals("Atletismo", ignoreCase = true)
            deporteKarts = nombreDeporte.equals("Karts", ignoreCase = true)

        }
        cursorDeporte.close()

        val encabezadoEquipo = if (deporteKarts) "Escudería" else "Equipo"
        val encabezadoDorsal = if (deporteKarts) "Número" else "Dorsal"

        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 20f
        titlePaint.textAlign = Paint.Align.CENTER

        val colorTitulo = when {
            deporteCiclismo -> coloresFondo[1]
            deporteAtletismo -> coloresFondo[2]
            deporteKarts -> coloresFondo[3]
            else -> R.color.black
        }
        titlePaint.color = ContextCompat.getColor(requireContext(), colorTitulo ?: R.color.black)
        canvas.drawText(nombreCarrera, (pageInfo.pageWidth / 2).toFloat(), 50f, titlePaint)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val iconoResId = iconosDeporte[nombreDeporte]
        if (iconoResId != null) {
            val bitmap = BitmapFactory.decodeResource(resources, iconoResId)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 40, 40, false)
            canvas.drawBitmap(scaledBitmap, (pageInfo.pageWidth * 0.950f - 40), 30f, null)
            canvas.drawBitmap(scaledBitmap, (pageInfo.pageWidth * 0.05f), 30f, null)
        }
        val startX = 40f
        var currentY = 90f
        val rowHeight = 20f

        canvas.drawText("Pos", startX, currentY, paint)
        canvas.drawText("Nombre", startX + 40, currentY, paint)
        canvas.drawText(encabezadoEquipo, startX + 180, currentY, paint)
        canvas.drawText(encabezadoDorsal, startX + 370, currentY, paint)
        canvas.drawText("Tiempo", startX + 440, currentY, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)


        val cursorverpdf = db.rawQuery("""
        SELECT r.posicion, c.nombre, c.equipo, p.dorsal, r.tiempo
        FROM resultados_carrera r
        JOIN participante_carrera p ON r.id_participante_carrera = p.id
        JOIN corredores c ON p.id_participante = c.id
        WHERE p.id_carrera = ?
        ORDER BY r.posicion ASC
    """, arrayOf(idCarrera.toString()))

        if (cursorverpdf.moveToFirst()) {
            do {
                currentY += rowHeight
                if (currentY > pageInfo.pageHeight - 50) {
                    pdfDocument.finishPage(page)
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                    val newPage = pdfDocument.startPage(newPageInfo)
                    page = newPage
                    canvas = newPage.canvas
                    currentY = 90f
                }

                val posicion = (cursorverpdf.getInt(0).toString() + "." )
                val nombre = cursorverpdf.getString(1)
                val equipo = cursorverpdf.getString(2) ?: "Sin equipo"
                val dorsal = cursorverpdf.getInt(3).toString()
                val tiempo = cursorverpdf.getString(4)

                canvas.drawText(posicion, startX, currentY, paint)
                canvas.drawText(nombre, startX + 40, currentY, paint)
                canvas.drawText(equipo, startX + 180, currentY, paint)
                canvas.drawText(dorsal, startX + 370, currentY, paint)
                canvas.drawText(tiempo, startX + 440, currentY, paint)

            } while (cursorverpdf.moveToNext())
        }

        cursorverpdf.close()
        pdfDocument.finishPage(page)
        val fileName = nombreCarrera.replace(" ", "_") + ".pdf"

        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            TODO("VERSION.SDK_INT < Q")
        }

        uri?.let {
            resolver.openOutputStream(it)?.use { output ->
                pdfDocument.writeTo(output)
                Toast.makeText(requireContext(), "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            Toast.makeText(requireContext(), "No se pudo guardar el archivo PDF", Toast.LENGTH_SHORT).show()
        }

        pdfDocument.close()
    }

}