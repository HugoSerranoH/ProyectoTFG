package com.example.proyectotfg

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider

class Login : AppCompatActivity() {
    private lateinit var db: BaseDatosEjemplo
    private lateinit var loginViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de la base de datos
        db = BaseDatosEjemplo(this, "ProyectoTFG", null, 1)
        val dbw = db.writableDatabase
        //Log.i("SQL", "Base de datos inicializada correctamente.")


        val cursordeportes = dbw.rawQuery("SELECT COUNT(*) FROM deportes", null)
        var hayDeportes = false

        if (cursordeportes.moveToFirst()) {
        hayDeportes = cursordeportes.getInt(0) > 0
            }
        cursordeportes.close()

        if (!hayDeportes) {
        val sql = "INSERT INTO deportes (id,nombre_deporte) VALUES(1, 'Ciclismo')"
        val sql2 = "INSERT INTO deportes (id,nombre_deporte) VALUES(2, 'Atletismo')"
        val sql3 = "INSERT INTO deportes (id,nombre_deporte) VALUES(3, 'Karts')"
        val sql4 = "INSERT INTO deportes (id,nombre_deporte) VALUES(4, 'Futbol')"

        dbw.execSQL(sql)
        dbw.execSQL(sql2)
        dbw.execSQL(sql3)
        dbw.execSQL(sql4)

        } else {
        Log.i("DEBUG", "Ya existen deportes en la tabla, no se insertan nuevos valores.")
        }


        // Inicializar ViewModel
        loginViewModel = ViewModelProvider(this)[UserViewModel::class.java]


        val usuarioEditText = findViewById<EditText>(R.id.UsuarioeditTextText)
        val contraseñaEditText = findViewById<EditText>(R.id.ContraseñaeditTextText2)
        val loginButton = findViewById<Button>(R.id.button)
        val registrateTextView = findViewById<TextView>(R.id.textViewRegistrate)
        val informacionTextView = findViewById<TextView>(R.id.textViewCreditos)
        val descargardatos = findViewById<ImageButton>(R.id.imageButtonDescargarDatos)


        registrateTextView.setOnClickListener {
            startActivity(Intent(this, RegistrarUsuario::class.java))
        }

        // Observador para la navegación
        loginViewModel.usuario.observe(this) { usuario ->
            usuario?.let {
                val intent = Intent(this, EligeModo::class.java).apply {
                    putExtra("nombre_usuario", it)
                }
                startActivity(intent)
                loginViewModel.resetUsuario()
            }
        }

        // Configuración del botón de inicio de sesión
        loginButton.setOnClickListener {
            val usuario = usuarioEditText.text.toString().trim()
            val contraseña = contraseñaEditText.text.toString().trim()

            if (verificarUsuario(dbw, usuario, contraseña)) {
                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                //Log.i("SQL", "Usuario y contraseña correctos")
                loginViewModel.setUsuario(usuario) // Pasar el usuario al ViewModel
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
               // Log.i("SQL", "Error en inicio de sesión")

                // Código de prueba comentado (por si lo necesito más adelante)
                /* val sqlprueba = "INSERT INTO usuarios (nombre_usuario, contraseña, telefono, email, sexo, ultima_sesion) " +
                        "VALUES ('prueba', 'prueba', '123456789', 'prueba@email.com', 'Masculino', '');" */
                // val deleteprueba = "DELETE FROM usuarios where id = 2"
                // dbw.execSQL(sqlprueba)
                // dbw.execSQL(deleteprueba)
            }
        }
        informacionTextView.setOnClickListener{
            startActivity(Intent(this, Creditos::class.java))
        }
    }

    private fun verificarUsuario(dbw: SQLiteDatabase, usuario: String, contraseña: String): Boolean {
        val query = "SELECT COUNT(*) FROM usuarios WHERE nombre_usuario = ? AND contraseña = ?"
        val cursorLogin = dbw.rawQuery(query, arrayOf(usuario, contraseña))


        var existeUsuario = false
        if (cursorLogin.moveToFirst()) {
            existeUsuario = cursorLogin.getInt(0) > 0
        }
        cursorLogin.close()
        return existeUsuario
    }

    private fun anadirciclistas() {
        val dbw = db.writableDatabase

        // Insertar la carrera París-Roubaix 2025
        dbw.execSQL(
            """INSERT INTO carreras (nombre_carrera, id_deporte, localidad, fecha)
           VALUES ('París-Roubaix 2025', 1, 'Roubaix', '2025-04-13')"""
        )

        // Obtener el ID de la carrera recién insertada
        val carreraCursor = dbw.rawQuery(
            "SELECT id FROM carreras WHERE nombre_carrera = ?",
            arrayOf("París-Roubaix 2025")
        )
        var idCarrera = -1
        if (carreraCursor.moveToFirst()) {
            idCarrera = carreraCursor.getInt(0)
        }
        carreraCursor.close()


        val corredores = listOf(
            Triple("Mathieu van der Poel", "Alpecin - Deceuninck", 30),
            Triple("Tadej Pogačar", "UAE Team Emirates", 26),
            Triple("Mads Pedersen", "Lidl - Trek", 29),
            Triple("Wout van Aert", "Team Visma | Lease a Bike", 30),
            Triple("Florian Vermeersch", "UAE Team Emirates", 26),
            Triple("Jonas Rutsch", "Intermarché - Wanty", 26),
            Triple("Stefan Bissegger", "Decathlon AG2R La Mondiale Team", 26),
            Triple("Markus Hoelgaard", "Uno-X Mobility", 30),
            Triple("Fred Wright", "Bahrain - Victorious", 25),
            Triple("Laurenz Rex", "Intermarché - Wanty", 24)
            // Agrega los corredores restantes hasta completar los 70
        )


        val dorsales = listOf(1, 21, 11, 36, 26, 45, 52, 61, 72, 81)


        // Lista de tiempos en formato HH:MM:SS
        val tiempos = listOf(
            "05:31:27",
            "05:32:45",
            "05:33:38",
            "05:33:38",
            "05:35:13",
            "05:35:13",
            "05:35:13",
            "05:35:13",
            "05:36:02",
            "05:36:03"

        )

        for (i in corredores.indices) {
            val (nombre, equipo, edad) = corredores[i]
            val dorsal = dorsales[i]
            val tiempo = tiempos[i]

            // Insertar corredor
            dbw.execSQL(
                "INSERT INTO corredores (nombre, equipo, edad, genero, id_deporte) VALUES (?, ?, ?, ?, ?)",
                arrayOf(nombre, equipo, edad, "Masculino", 1)
            )

            // Obtener el ID del corredor recién insertado
            val corredorCursor = dbw.rawQuery(
                "SELECT id FROM corredores WHERE nombre = ? AND equipo = ?",
                arrayOf(nombre, equipo)
            )
            var idCorredor = -1
            if (corredorCursor.moveToFirst()) {
                idCorredor = corredorCursor.getInt(0)
            }
            corredorCursor.close()

            // Insertar participante en la carrera
            dbw.execSQL(
                "INSERT INTO participante_carrera (id_participante, id_carrera, dorsal) VALUES (?, ?, ?)",
                arrayOf(idCorredor, idCarrera, dorsal)
            )

            // Obtener el ID del participante_carrera recién insertado
            val participanteCursor = dbw.rawQuery(
                "SELECT id FROM participante_carrera WHERE id_participante = ? AND id_carrera = ?",
                arrayOf(idCorredor.toString(), idCarrera.toString())
            )
            var idParticipanteCarrera = -1
            if (participanteCursor.moveToFirst()) {
                idParticipanteCarrera = participanteCursor.getInt(0)
            }
            participanteCursor.close()

            // Insertar resultado de la carrera
            dbw.execSQL(
                "INSERT INTO resultados_carrera (id_participante_carrera, tiempo, posicion) VALUES (?, ?, ?)",
                arrayOf(idParticipanteCarrera, tiempo, i + 1)
            )
        }

        Toast.makeText(this, "Datos de París-Roubaix 2025 cargados correctamente", Toast.LENGTH_LONG).show()
    }

}