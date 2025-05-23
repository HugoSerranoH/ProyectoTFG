package com.example.proyectotfg

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegistrarUsuario : AppCompatActivity() {
    private lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar_usuario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val dbHelper = BaseDatosEjemplo(this, "ProyectoTFG", null, 1)
        db = dbHelper.writableDatabase


        val nombreUsuario = findViewById<EditText>(R.id.editTextRegistroNombre)
        val contraseña = findViewById<EditText>(R.id.editTextRegistroContrasena)
        val telefono = findViewById<EditText>(R.id.editTextRegistroTelefono)
        val email = findViewById<EditText>(R.id.editTextRegistroEmail)
        val sexoSpinner = findViewById<Spinner>(R.id.spinnerRegistroSexo)
        val registrarButton = findViewById<Button>(R.id.buttonRegistrar)


        val adapter = ArrayAdapter.createFromResource(this, R.array.sexo_opciones, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sexoSpinner.adapter = adapter


        registrarButton.setOnClickListener {
            val nombre = nombreUsuario.text.toString()
            val pass = contraseña.text.toString()
            val telefonoText = telefono.text.toString()
            val emailText = email.text.toString()
            val sexo = sexoSpinner.selectedItem.toString()

            if (nombre.isNotEmpty() && pass.isNotEmpty() && telefonoText.isNotEmpty() && emailText.isNotEmpty() && sexo.isNotEmpty()) {
                if (!emailValido(emailText)) {
                    Toast.makeText(this, "Email no válido", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!contrasenasegura(pass)) {
                    Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres, incluyendo mayúsculas, minúsculas y un número", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if (!telefonoText.all { it.isDigit() }) {
                    Toast.makeText(this, "El teléfono solo debe contener números", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


                val hashedPassword = hashcontrasena(pass)
                insertarUsuario(nombre, hashedPassword, telefonoText, emailText, sexo)
            } else {
                Toast.makeText(this, "Por favor, rellena todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun insertarUsuario(nombre: String, contrasena: String, telefono: String, email: String, sexo: String) {
        try {
            val sqlInsert = "INSERT INTO usuarios (nombre_usuario, contraseña, telefono, email, sexo) VALUES (?, ?, ?, ?, ?)"
            db.execSQL(sqlInsert, arrayOf(nombre, contrasena, telefono, email, sexo))
            Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
            //Log.i("SQL", "Usuario $nombre registrado con éxito")

            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al registrar el usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            //Log.e("SQL", "Error al registrar el usuario: ${e.message}")
        }
    }

    private fun emailValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun contrasenasegura(contrasena: String): Boolean {
        val contrasenaregex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}\$")
        return contrasena.matches(contrasenaregex)
    }

    private fun hashcontrasena(contrasena: String): String {
        val bytes = contrasena.toByteArray()
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

}
