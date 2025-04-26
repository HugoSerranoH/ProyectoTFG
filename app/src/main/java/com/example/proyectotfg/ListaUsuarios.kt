package com.example.proyectotfg

import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class ListaUsuarios : Fragment() {
    private lateinit var listViewUsuarios: ListView
    private lateinit var editTextBuscarUsuario: EditText
    private lateinit var dbHelper: BaseDatosEjemplo
    private var listaUsuarios = mutableListOf<String>()
    private var listaUsuariosFiltrados = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private var usuarioSeleccionado: String? = null
    private var idDeporte: Int = -1
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_lista_usuarios, container, false)

        listViewUsuarios = view.findViewById(R.id.listViewUsuarios)
        editTextBuscarUsuario = view.findViewById(R.id.editTextTextBuscaUsuarios)

        dbHelper = BaseDatosEjemplo(requireContext(), "ProyectoTFG", null, 1)
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]


        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_single_choice, listaUsuariosFiltrados)
        listViewUsuarios.adapter = adapter
        listViewUsuarios.choiceMode = ListView.CHOICE_MODE_SINGLE

        userViewModel.deporteSeleccionado.observe(viewLifecycleOwner) { deporte ->
            deporte?.let { (id, nombre) ->
                idDeporte = id
                Log.i("DEBUG", "ListaUsuarios: Obtenido ID de deporte -> $id, Nombre -> $nombre")
                cargarUsuarios()
            }
        }

        listViewUsuarios.setOnItemClickListener { _, _, position, _ ->
            usuarioSeleccionado = listaUsuariosFiltrados[position]
        }


        editTextBuscarUsuario.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarUsuarios(s.toString())
            }
        })

        return view
    }

    private fun cargarUsuarios() {
        listaUsuarios.clear()
        listaUsuariosFiltrados.clear()

        if (idDeporte == -1) {
            Log.e("ERROR", "ListaUsuarios: ID de deporte inválido, no se puede cargar usuarios")
            return
        }

        val db = dbHelper.readableDatabase

        val cursordeportistadeporte: Cursor = db.rawQuery(
            "SELECT nombre FROM corredores WHERE id_deporte = ? order by nombre asc",
            arrayOf(idDeporte.toString())
        )

        if (cursordeportistadeporte.moveToFirst()) {
            do {
                listaUsuarios.add(cursordeportistadeporte.getString(0))
            } while (cursordeportistadeporte.moveToNext())
        }
        cursordeportistadeporte.close()

        listaUsuariosFiltrados.addAll(listaUsuarios)
        Log.i("DEBUG", "ListaUsuarios: Usuarios cargados -> $listaUsuariosFiltrados")
        adapter.notifyDataSetChanged()
    }

    private fun filtrarUsuarios(texto: String) {
        listaUsuariosFiltrados.clear()
        if (texto.isEmpty()) {
            listaUsuariosFiltrados.addAll(listaUsuarios)
        } else {
            listaUsuariosFiltrados.addAll(listaUsuarios.filter {
                it.contains(texto, ignoreCase = true)
            })
        }
        adapter.notifyDataSetChanged()
    }


    fun mostrarDialogoDorsal(agregarParticipante: (String, Int) -> Unit) {
        if (usuarioSeleccionado.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Selecciona un usuario primero", Toast.LENGTH_SHORT).show()
            return
        }

        val editTextDorsal = EditText(requireContext())
        editTextDorsal.hint = "Introduce número de dorsal"

        AlertDialog.Builder(requireContext())
            .setTitle("Asignar Dorsal a $usuarioSeleccionado")
            .setView(editTextDorsal)
            .setPositiveButton("Añadir") { _, _ ->
                val dorsal = editTextDorsal.text.toString().toIntOrNull()
                if (dorsal == null) {
                    Toast.makeText(requireContext(), "Introduce un número válido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                agregarParticipante(usuarioSeleccionado!!, dorsal)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    fun obtenerUsuarioSeleccionado(): String? {
        Log.i("DEBUG", "Intentando obtener usuario seleccionado -> $usuarioSeleccionado")
        return usuarioSeleccionado
    }
}