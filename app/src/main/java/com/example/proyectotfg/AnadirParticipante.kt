package com.example.proyectotfg

import android.app.AlertDialog
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class AnadirParticipante : Fragment() {
    private lateinit var buttonAnadir: Button
    private lateinit var buttonBorrarparticipante: Button
    private lateinit var dbHelper: BaseDatosEjemplo
    private var idCarrera: Int = -1
    private lateinit var textanadir : TextView
    private lateinit var textparticipantes : TextView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_anadir_participante, container, false)

        buttonAnadir = view.findViewById(R.id.buttonanadirparticipante)
        buttonBorrarparticipante = view.findViewById(R.id.buttonborrarparticipante)
        textanadir = view.findViewById(R.id.textViewArribaParticipantes)
        textparticipantes = view.findViewById(R.id.textViewParticipantesanadidos)


        dbHelper = BaseDatosEjemplo(requireContext(), "ProyectoTFG", null, 1)

        // Recuperar ID de la carrera desde `ConsultaCarrera`
        idCarrera = requireActivity().intent.getIntExtra("id_carrera", -1)

        if (idCarrera == -1) {
            Toast.makeText(requireContext(), "Error al obtener la carrera", Toast.LENGTH_SHORT).show()
            return view
        }


        val fragmentUsuarios = ListaUsuarios()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerViewTodosUsuarios, fragmentUsuarios)
            .commit()

        val fragmentParticipantes = ListaParticipantes().apply {
            arguments = Bundle().apply { putInt("id_carrera", idCarrera) }
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerViewParticipantesanadidos, fragmentParticipantes)
            .commit()


        buttonAnadir.setOnClickListener {
            fragmentUsuarios.mostrarDialogoDorsal(::agregarParticipante)
        }
        buttonBorrarparticipante.setOnClickListener {
            val fragmentParticipantes = requireActivity().supportFragmentManager.findFragmentById(R.id.fragmentContainerViewParticipantesanadidos) as? ListaParticipantes

            if (fragmentParticipantes != null) {
                val nombreSeleccionado = fragmentParticipantes.obtenerParticipanteSeleccionado()

                if (nombreSeleccionado != null) {
                    mostrarDialogoConfirmacion(nombreSeleccionado)
                } else {
                    Toast.makeText(requireContext(), "Selecciona un participante para borrar", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Error al acceder a la lista de participantes", Toast.LENGTH_SHORT).show()
            }
        }


        val db = dbHelper.readableDatabase
        val cursornombreDeporte = db.rawQuery(
            "SELECT d.nombre_deporte FROM deportes d INNER JOIN carreras c ON d.id = c.id_deporte WHERE c.id = ?",
            arrayOf(idCarrera.toString())
        )

        if (cursornombreDeporte.moveToFirst()) {
            val nombreDeporte = cursornombreDeporte.getString(0)
            cursornombreDeporte.close()

            when {
                nombreDeporte.equals("Ciclismo", ignoreCase = true) -> {
                    textanadir.text = "Añadir ciclista a carrera"
                    textparticipantes.text = "Ciclistas añadidos"
                }
                nombreDeporte.equals("Atletismo", ignoreCase = true) -> {
                    textanadir.text = "Añadir atleta a carrera"
                    textparticipantes.text = "Atletas añadidos"
                }
                nombreDeporte.equals("Karts", ignoreCase = true) -> {
                    textanadir.text = "Añadir piloto a carrera"
                    textparticipantes.text = "Pilotos añadidos"
                }
                else -> {
                    textanadir.text = "Añadir participante a carrera"
                    textparticipantes.text = "Participantes añadidos"
                }
            }
        }

        return view
    }

    private fun agregarParticipante(nombre: String, dorsal: Int) {
        val db = dbHelper.writableDatabase


        val cursor: Cursor = db.rawQuery("SELECT id FROM corredores WHERE nombre = ?", arrayOf(nombre))
        if (!cursor.moveToFirst()) {
            cursor.close()
            Toast.makeText(requireContext(), "Error al obtener participante", Toast.LENGTH_SHORT).show()
            return
        }
        val idParticipante = cursor.getInt(0)
        cursor.close()


        val checkNombre: Cursor = db.rawQuery(
            "SELECT id FROM participante_carrera WHERE id_participante = ? AND id_carrera = ?",
            arrayOf(idParticipante.toString(), idCarrera.toString())
        )

        if (checkNombre.count > 0) {
            checkNombre.close()
            Toast.makeText(requireContext(), "$nombre ya está registrado en esta carrera", Toast.LENGTH_SHORT).show()
            return
        }
        checkNombre.close()

        val checkDorsal: Cursor = db.rawQuery(
            "SELECT id FROM participante_carrera WHERE dorsal = ? AND id_carrera = ?",
            arrayOf(dorsal.toString(), idCarrera.toString())
        )

        if (checkDorsal.count > 0) {
            checkDorsal.close()
            Toast.makeText(requireContext(), "El dorsal $dorsal ya está registrado en esta carrera", Toast.LENGTH_SHORT).show()
            return
        }
        checkDorsal.close()


        db.execSQL(
            "INSERT INTO participante_carrera (id_participante, id_carrera, dorsal) VALUES (?, ?, ?)",
            arrayOf(idParticipante.toString(), idCarrera.toString(), dorsal.toString())
        )

        Toast.makeText(requireContext(), "$nombre añadido con dorsal $dorsal", Toast.LENGTH_SHORT).show()


        actualizarListaParticipantes()
    }

    private fun actualizarListaParticipantes() {
        val fragmentParticipantes = ListaParticipantes().apply {
            arguments = Bundle().apply { putInt("id_carrera", idCarrera) }
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerViewParticipantesanadidos, fragmentParticipantes)
            .commit()
    }

    private fun mostrarDialogoConfirmacion(nombreParticipante: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar participante")
            .setMessage("¿Seguro que quieres eliminar a $nombreParticipante?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarParticipante(nombreParticipante)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun eliminarParticipante(nombreConDorsal: String) {
        val db = dbHelper.writableDatabase
        // esto es para que separe lo de antes de la coma
        val nombre = nombreConDorsal.split(",")[0].trim()
        val cursor = db.rawQuery("SELECT id FROM corredores WHERE nombre = ?", arrayOf(nombre))
        if (cursor.moveToFirst()) {
            val idParticipante = cursor.getInt(0)
            cursor.close()

            db.execSQL("DELETE FROM participante_carrera WHERE id_participante = ? AND id_carrera = ?", arrayOf(idParticipante.toString(), idCarrera.toString()))

            Toast.makeText(requireContext(), "$nombre eliminado", Toast.LENGTH_SHORT).show()

            actualizarListaParticipantes()
        } else {
            cursor.close()
            Toast.makeText(requireContext(), "Error: Participante no encontrado", Toast.LENGTH_SHORT).show()
        }
    }

}