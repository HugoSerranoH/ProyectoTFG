package com.example.proyectotfg

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class ModificarDeportista : Fragment() {

    private lateinit var dbHelper: BaseDatosEjemplo
    private var usuarioSeleccionadoId: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_modificar_deportista, container, false)
        dbHelper = BaseDatosEjemplo(requireContext(), "ProyectoTFG", null, 1)


        val botonModificarDeportista = view.findViewById<Button>(R.id.buttonModificarDeportista)
        botonModificarDeportista.setOnClickListener {
            val listaUsuariosFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragmentContainerViewModificarUsuarios) as? ListaUsuarios
            val usuarioSeleccionado = listaUsuariosFragment?.obtenerUsuarioSeleccionado()

            if (usuarioSeleccionado.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Selecciona un usuario primero", Toast.LENGTH_SHORT).show()
            } else {

                usuarioSeleccionadoId = obtenerIdUsuario(usuarioSeleccionado)
                if (usuarioSeleccionadoId == null) {
                    Toast.makeText(requireContext(), "Error: No se encontró el ID del usuario", Toast.LENGTH_SHORT).show()
                } else {
                    mostrarDialogoModificar(usuarioSeleccionado)
                }
            }
        }


        Handler(Looper.getMainLooper()).post {
            val fragmentManager = requireActivity().supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerViewModificarUsuarios, ListaUsuarios())
                .commitAllowingStateLoss()
        }

        return view
    }

    private fun obtenerIdUsuario(nombreUsuario: String): Int? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id FROM corredores WHERE nombre = ?", arrayOf(nombreUsuario))

        return if (cursor.moveToFirst()) {
            val id = cursor.getInt(0)
            cursor.close()
//            Log.i("DEBUG", "ID del usuario obtenido: $id")
            id
        } else {
            cursor.close()
            null
        }
    }

    private fun mostrarDialogoModificar(usuario: String) {
        val opciones = arrayOf("Editar Nombre", "Editar Equipo", "Editar Edad", "Ver (próximamente)")

        AlertDialog.Builder(requireContext())
            .setTitle("Modificar $usuario")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> editarCampo("nombre")
                    1 -> editarCampo("equipo")
                    2 -> editarCampo("edad")
                    3 -> Toast.makeText(requireContext(), "Esta funcionalidad llegará pronto", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun editarCampo(campo: String) {
        val input = EditText(requireContext())
        input.hint = "Introduce nuevo $campo"

        AlertDialog.Builder(requireContext())
            .setTitle("Editar $campo del deportista")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoValor = input.text.toString()
                if (nuevoValor.isEmpty()) {
                    Toast.makeText(requireContext(), "El $campo no puede estar vacío", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val db = dbHelper.writableDatabase
                val idUsuario = usuarioSeleccionadoId
                if (idUsuario != null) {
                    db.execSQL("UPDATE corredores SET $campo = ? WHERE id = ?", arrayOf(nuevoValor, idUsuario.toString()))
                    Toast.makeText(requireContext(), "$campo actualizado correctamente", Toast.LENGTH_SHORT).show()
                    actualizarListaUsuarios()
                } else {
                    Toast.makeText(requireContext(), "Error: ID del usuario no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarListaUsuarios() {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentUsuarios = fragmentManager.findFragmentById(R.id.fragmentContainerViewModificarUsuarios)
        if (fragmentUsuarios != null) {
            fragmentManager.beginTransaction().remove(fragmentUsuarios).commitNow()
        }
        fragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerViewModificarUsuarios, ListaUsuarios())
            .commitAllowingStateLoss()
    }
}