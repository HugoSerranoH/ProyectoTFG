package com.example.proyectotfg

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider

class EligeModo : AppCompatActivity() {
    private lateinit var dbHelper: BaseDatosEjemplo
    private lateinit var userViewModel: UserViewModel
    private lateinit var spinnerDeportes: Spinner
    private lateinit var deportesList: MutableList<String>
    private lateinit var deportesIdList: MutableList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_elige_modo)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]


        dbHelper = BaseDatosEjemplo(this, "ProyectoTFG", null, 1)
        val db = dbHelper.readableDatabase

        // Inicializar listas delSpinner
        deportesList = mutableListOf("Selecciona un deporte")
        //Para que sea un espacio en blanco
        deportesIdList = mutableListOf(-1)

        Log.i("DEBUG", "Cargando deportes desde la base de datos...")

        // Obtener deportes desde la base de datos
        val cursordeporte: Cursor = db.rawQuery("SELECT id, nombre_deporte FROM deportes", null)
        if (cursordeporte.moveToFirst()) {
            do {
                deportesIdList.add(cursordeporte.getInt(0))
                deportesList.add(cursordeporte.getString(1))
            } while (cursordeporte.moveToNext())
        }
        cursordeporte.close()

        Log.i("DEBUG", "Deportes cargados: $deportesList")

        val deportesIconList = mutableListOf(
            R.drawable.construction_icon,
            R.drawable.bicycle_icon, // Icono para "Ciclismo"
            R.drawable.sprint_icon, // Icono para "Atletismo"
            R.drawable.forklift_icon, // Icono para "karts"
            R.drawable.construction_icon, // Icono para "Fútbol"

        )


        // Configuración del Spinner
        spinnerDeportes = findViewById(R.id.spinner2)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, deportesList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


        spinnerDeportes.adapter = adapter

        // Selección de deporte en Spinner
        spinnerDeportes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                // Evitar navegación si la opción seleccionada es la inicial ("Selecciona un deporte")
                if (deportesIdList[position] != -1) {
                    Log.i("DEBUG", "Spinner seleccionado: ${deportesList[position]} (ID: ${deportesIdList[position]})")
                    userViewModel.setDeporteSeleccionado(deportesIdList[position], deportesList[position])
                } else {
                    Log.i("DEBUG", "Usuario no ha seleccionado ningún deporte todavía.")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.i("DEBUG", "Nada seleccionado en el Spinner")
            }
        }




//        // Configuración de los botones
//        val imageButtonCiclismo: ImageButton = findViewById(R.id.imageButtonCiclismo)
//        val imageButtonAtletismo: ImageButton = findViewById(R.id.imageButtonAtletismo)
//        val imageButtonKarts: ImageButton = findViewById(R.id.imageButtonKarts)
//
//        imageButtonCiclismo.setOnClickListener {
//            Log.i("DEBUG", "Botón Ciclismo pulsado")
//            userViewModel.setDeporteSeleccionado(1, "Ciclismo")
//        }
//
//        imageButtonAtletismo.setOnClickListener {
//            Log.i("DEBUG", "Botón Atletismo pulsado")
//            userViewModel.setDeporteSeleccionado(2, "Atletismo")
//        }
//
//        imageButtonKarts.setOnClickListener {
//            Log.i("DEBUG", "Botón Karts pulsado")
//            userViewModel.setDeporteSeleccionado(3, "Karts")
//        }

        // Cargar el Fragment inicial
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentAccion, SeleccionAccionFragment())
            .commit()

        // Observer
        userViewModel.deporteSeleccionado.observe(this) { deporte ->
            deporte?.let { (_, nombreDeporte) ->
                Log.i("DEBUG", "Cambiando Fragment a SeleccionAccion con deporte: $nombreDeporte")
                val fragmentContainer = findViewById<View>(R.id.fragmentAccion)
                fragmentContainer.visibility = View.VISIBLE
                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentAccion) as? SeleccionAccionFragment
                fragment?.actualizarDeporte(nombreDeporte)
//                fragment.visibility = View.VISIBLE
            }
        }

        /*
        // Antiguo Observer
        userViewModel.deporteSeleccionado.observe(this) { deporte ->
            deporte?.let { (idDeporte, nombreDeporte) ->
                Log.i("DEBUG", "Navegando a Seleccion_Accion con ID: $idDeporte, Deporte: $nombreDeporte")
                val intent = Intent(this, Seleccion_Accion::class.java).apply {
                    putExtra("id_deporte", idDeporte)
                    putExtra("nombreDeporte", nombreDeporte)
                }
                startActivity(intent)
                userViewModel.resetDeporteSeleccionado()
            }
        }
        */

        // Mostrar el nombre del usuario en el TextView
        val nombreUsuario = intent.getStringExtra("nombre_usuario")
        val textViewArriba = findViewById<TextView>(R.id.textViewDeporte_usuario)
        textViewArriba.text = "Elige el deporte, $nombreUsuario"
    }
}