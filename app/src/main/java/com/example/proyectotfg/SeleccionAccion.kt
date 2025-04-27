package com.example.proyectotfg

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

import androidx.lifecycle.ViewModelProvider

class SeleccionAccionFragment : Fragment() {

    private lateinit var userViewModel: UserViewModel
    private var nombreDeporte: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_seleccion_accion, container, false)


        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

        val textViewElegido = view.findViewById<TextView>(R.id.textViewElegidoDeporte)
        val botonCrearDeportista = view.findViewById<Button>(R.id.buttonCrearDeportista)
        val botonCrearCarrera = view.findViewById<Button>(R.id.buttonCrearCarrera)
        val botonBorrarCarrera = view.findViewById<Button>(R.id.buttonBorrarCarrera)
        val botonmodificarver = view.findViewById<Button>(R.id.buttonModificar)
        val botonBorrarDeportista = view.findViewById<Button>(R.id.buttonBorrarDeportista)
        val botonModificarDeportista = view.findViewById<Button>(R.id.buttonModificarDeportista)


        textViewElegido.text = "Has seleccionado como deporte: $nombreDeporte"

        botonCrearDeportista.setOnClickListener {
            val fragment = CrearDeportista().apply {
                arguments = Bundle().apply {
                    putInt("id_deporte", userViewModel.deporteSeleccionado.value?.first ?: -1)
                }
            }
            cambiarFragment(fragment)
        }

        botonBorrarDeportista.setOnClickListener {
            val fragment = BorrarDeportista().apply {
                arguments = Bundle().apply {
                    putInt("id_deporte", userViewModel.deporteSeleccionado.value?.first ?: -1)
                }
            }
            cambiarFragment(fragment)
        }

        botonModificarDeportista.setOnClickListener {
            val fragment = ModificarDeportista().apply {
                arguments = Bundle().apply {
                    putInt("id_deporte", userViewModel.deporteSeleccionado.value?.first ?: -1)
                }
            }
            cambiarFragment(fragment)
        }

        botonCrearCarrera.setOnClickListener {
            val fragment = CrearCarrera().apply {
                arguments = Bundle().apply {
                    putInt("id_deporte", userViewModel.deporteSeleccionado.value?.first ?: -1)
                }
            }
            cambiarFragment(fragment)
        }

        botonBorrarCarrera.setOnClickListener {
            val fragment = BorrarCarrera().apply {
                arguments = Bundle().apply {
                    putInt("id_deporte", userViewModel.deporteSeleccionado.value?.first ?: -1)
                }
            }
            cambiarFragment(fragment)
        }
        botonmodificarver.setOnClickListener {
            val intent = Intent(requireContext(), SeleccionarCarrera::class.java).apply {
                putExtra("id_deporte", userViewModel.deporteSeleccionado.value?.first ?: -1)
            }
            startActivity(intent)
        }




        return view
    }

    fun actualizarDeporte(nombreDeporte: String) {
        this.nombreDeporte = nombreDeporte
        view?.findViewById<TextView>(R.id.textViewElegidoDeporte)?.text =
            "Has seleccionado como deporte: $nombreDeporte"

        val Imagenfondo = view?.findViewById<ImageView>(R.id.imagenfondoaccion)

        when (nombreDeporte.lowercase()) {
            "ciclismo" -> {
                val ciclismoFondos = arrayOf(
                R.drawable.fondociclismo,
                R.drawable.fondociclismo2,
                R.drawable.fondociclismo3,
                R.drawable.fondociclismo4, )
                val fondociclismoaleatorio = ciclismoFondos.random()
                Imagenfondo?.setImageResource(fondociclismoaleatorio)
            }
            "atletismo" -> {
                val atletismoFondos = arrayOf(
                    R.drawable.fondoatletismo,
                    R.drawable.fondoatletismo2,
                    R.drawable.fondoatletismo3,)
                val fondoatletismoaleatorio = atletismoFondos.random()
                Imagenfondo?.setImageResource(fondoatletismoaleatorio)
            }
            "karts" -> {
                val kartsFondos = arrayOf(
                    R.drawable.fondokarts,
                    R.drawable.fondokarts2,
                    R.drawable.fondokarts3,)
                val fondokartsaleatorio = kartsFondos.random()
                Imagenfondo?.setImageResource(fondokartsaleatorio)
            }
            else -> Imagenfondo?.setImageDrawable(null)
        }
    }


    private fun cambiarFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentAccion, fragment)
            .addToBackStack(null)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }
}