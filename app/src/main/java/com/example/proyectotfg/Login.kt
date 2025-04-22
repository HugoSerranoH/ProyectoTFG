package com.example.proyectotfg

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
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
        Log.i("SQL", "Base de datos inicializada correctamente.")


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
                Log.i("SQL", "Usuario y contraseña correctos")
                loginViewModel.setUsuario(usuario) // Pasar el usuario al ViewModel
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                Log.i("SQL", "Error en inicio de sesión")

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


}