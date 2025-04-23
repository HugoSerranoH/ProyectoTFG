package com.example.proyectotfg

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class BorrarDeportista : Fragment() {
    private lateinit var buttonEliminar: Button
    private lateinit var dbHelper: BaseDatosEjemplo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_borrar_deportista, container, false)

        buttonEliminar = view.findViewById(R.id.buttonEliminarDeportista)
        dbHelper = BaseDatosEjemplo(requireContext(), "ProyectoTFG", null, 1)

        buttonEliminar.setOnClickListener {
            borrarDeportistaSeleccionado()
        }

        Handler(Looper.getMainLooper()).post {
            val fragmentManager = requireActivity().supportFragmentManager
            if (fragmentManager.findFragmentById(R.id.fragmentContainerViewBorrarUsuarios) == null) {
                fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerViewBorrarUsuarios, ListaUsuarios())
                    .commitAllowingStateLoss()

            }
        }

        return view
    }

    private fun borrarDeportistaSeleccionado() {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentUsuarios = fragmentManager.findFragmentById(R.id.fragmentContainerViewBorrarUsuarios) as? ListaUsuarios

        if (fragmentUsuarios == null) {
            actualizarListaUsuarios()
            return
        }

        val nombreSeleccionado = fragmentUsuarios.obtenerUsuarioSeleccionado()
        if (nombreSeleccionado != null) {
            mostrarDialogoConfirmacion(nombreSeleccionado)
        } else {
            Toast.makeText(requireContext(), "Selecciona un deportista para borrar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoConfirmacion(nombreUsuario: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar deportista")
            .setMessage("¿Seguro que quieres eliminar a $nombreUsuario?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarDeportista(nombreUsuario)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarDeportista(nombre: String) {
        val db = dbHelper.writableDatabase
        val filasAfectadas = db.delete("corredores", "nombre = ?", arrayOf(nombre))

        if (filasAfectadas > 0) {
            Toast.makeText(requireContext(), "$nombre eliminado", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Error: Usuario no encontrado o no eliminado", Toast.LENGTH_SHORT).show()
        }
        actualizarListaUsuarios()
    }

    private fun actualizarListaUsuarios() {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentUsuarios = fragmentManager.findFragmentById(R.id.fragmentContainerViewBorrarUsuarios)
        if (fragmentUsuarios != null) {
            fragmentManager.beginTransaction().remove(fragmentUsuarios).commitNow()
        }
        fragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerViewBorrarUsuarios, ListaUsuarios())
            .commitAllowingStateLoss()
    }
}