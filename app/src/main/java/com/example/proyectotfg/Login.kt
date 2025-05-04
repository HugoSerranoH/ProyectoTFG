package com.example.proyectotfg

import android.content.Context
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
        informacionTextView.setOnClickListener {
            startActivity(Intent(this, Creditos::class.java))
        }
        descargardatos.setOnClickListener {
            anadirciclistas()
//            borrarCorredores(this)
        }
    }

    private fun verificarUsuario(
        dbw: SQLiteDatabase,
        usuario: String,
        contraseña: String
    ): Boolean {
        val query = "SELECT COUNT(*) FROM usuarios WHERE nombre_usuario = ? AND contraseña = ?"
        val cursorLogin = dbw.rawQuery(query, arrayOf(usuario, contraseña))


        var existeUsuario = false
        if (cursorLogin.moveToFirst()) {
            existeUsuario = cursorLogin.getInt(0) > 0
        }
        cursorLogin.close()
        return existeUsuario
    }

    data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
    private fun anadirciclistas() {
        val dbw = db.writableDatabase

        val existeCursor = dbw.rawQuery(
            "SELECT COUNT(*) FROM carreras WHERE nombre_carrera = ?",
            arrayOf("París-Roubaix 2025")
        )
        var existe = false
        if (existeCursor.moveToFirst()) {
            existe = existeCursor.getInt(0) > 0
        }
        existeCursor.close()

        if (existe) {
            Toast.makeText(
                this,
                "La carrera París-Roubaix 2025 ya está creada. No se realiza ninguna acción.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        dbw.execSQL(
            """INSERT INTO carreras (nombre_carrera, id_deporte, localidad, fecha)
           VALUES ('París-Roubaix 2025', 1, 'Francia', '13/04/2025')"""
        )

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
            Quad(99000, "Mathieu van der Poel",      "Alpecin - Deceuninck",               30),
            Quad(99001, "Tadej Pogačar",             "UAE Team Emirates - XRG",            26),
            Quad(99002, "Mads Pedersen",             "Lidl - Trek",                        29),
            Quad(99003, "Wout van Aert",             "Team Visma | Lease a Bike",          30),
            Quad(99004, "Florian Vermeersch",        "UAE Team Emirates - XRG",            26),
            Quad(99005, "Jonas Rutsch",              "Intermarché - Wanty",                27),
            Quad(99006, "Stefan Bissegger",          "Decathlon AG2R La Mondiale",    26),
            Quad(99007, "Markus Hoelgaard",          "Uno-X Mobility",                     30),
            Quad(99008, "Fred Wright",               "Bahrain - Victorious",               25),
            Quad(99009, "Laurenz Rex",               "Intermarché - Wanty",                25),

            Quad(99010, "Jasper Philipsen",          "Alpecin - Deceuninck",               27),
            Quad(99011, "Marco Haller",              "Tudor Pro Cycling Team",             34),
            Quad(99012, "Filippo Ganna",             "INEOS Grenadiers",                   28),
            Quad(99013, "Madis Mihkels",             "EF Education-EasyPost",              21),
            Quad(99014, "Biniam Girmay",             "Intermarché - Wanty",                25),
            Quad(99015, "Mike Teunissen",            "XDS Astana Team",                    32),
            Quad(99016, "Daan Hoole",                "Lidl - Trek",                        26),
            Quad(99017, "Mick van Dijke",            "Red Bull - BORA-hansgrohe",          25),
            Quad(99018, "Marius Mayrhofer",          "Tudor Pro Cycling Team",             24),
            Quad(99019, "Taco van der Hoorn",        "Intermarché - Wanty",                31),

            Quad(99020, "Johan Jacobs",              "Groupama - FDJ",                     28),
            Quad(99021, "Dries De Bondt",            "Decathlon AG2R La Mondiale",    33),
            Quad(99022, "Phil Bauhaus",              "Bahrain - Victorious",               30),
            Quad(99023, "António Morgado",           "UAE Team Emirates - XRG",            21),
            Quad(99024, "Florian Dauphin",           "Team TotalEnergies",                 26),
            Quad(99025, "Erik Nordsæter Resell",     "Uno-X Mobility",                     28),
            Quad(99026, "Axel Huens",                "Unibet Tietema Rockets",             23),
            Quad(99027, "Yves Lampaert",             "Soudal Quick-Step",                  34),
            Quad(99028, "Cees Bol",                  "XDS Astana Team",                    29),
            Quad(99029, "Tim van Dijke",             "Red Bull - BORA-hansgrohe",          25),

            Quad(99030, "Rasmus S. Pedersen",        "Decathlon AG2R La Mondiale",    22),
            Quad(99031, "Frederik Frison",           "Q36.5 Pro Cycling Team",             32),
            Quad(99032, "Tomáš Kopecký",             "Unibet Tietema Rockets",             25),
            Quad(99033, "Edward Planckaert",         "Alpecin - Deceuninck",               30),
            Quad(99034, "Dylan van Baarle",          "Team Visma | Lease a Bike",          32),
            Quad(99035, "Tim Merlier",               "Soudal Quick-Step",                  32),
            Quad(99036, "Giacomo Nizzolo",           "Q36.5 Pro Cycling Team",             36),
            Quad(99037, "Jenthe Biermans",           "Arkéa - B&B Hotels",                 29),
            Quad(99038, "Anthony Turgis",            "Team TotalEnergies",                 30),
            Quad(99039, "Sébastien Grignard",        "Lotto",                              25),

            Quad(99040, "Damien Touzé",              "Cofidis",                            28),
            Quad(99041, "Søren Wærenskjold",         "Uno-X Mobility",                     25),
            Quad(99042, "Stefan Küng",               "Groupama - FDJ",                     31),
            Quad(99043, "Matthew Brennan",           "Team Visma | Lease a Bike",          19),
            Quad(99044, "Stanisław Aniołkowski",     "Cofidis",                            28),
            Quad(99045, "Lukáš Kubiš",               "Unibet Tietema Rockets",             25),
            Quad(99046, "Hugo Hofstetter",           "Israel - Premier Tech",              31),
            Quad(99047, "Guillaume Boivin",          "Israel - Premier Tech",              35),
            Quad(99048, "Vlad Van Mechelen",         "Bahrain - Victorious",               20),
            Quad(99049, "Mikkel Bjerg",              "UAE Team Emirates - XRG",            26),

            Quad(99050, "Cedric Beullens",           "Lotto",                              28),
            Quad(99051, "Joshua Tarling",            "INEOS Grenadiers",                   21),
            Quad(99052, "Jordi Meeus",               "Red Bull - BORA-hansgrohe",          26),
            Quad(99053, "Joshua Giddings",           "Lotto",                              21),
            Quad(99054, "Andreas Stokbro",           "Unibet Tietema Rockets",             28),
            Quad(99055, "Pavel Bittner",             "Team Picnic PostNL",                 22),
            Quad(99056, "Tom Van Asbroeck",          "Israel - Premier Tech",              34),
            Quad(99057, "Petr Kelemen",              "Tudor Pro Cycling Team",             24),
            Quad(99058, "Max Walscheid",             "Team Jayco AlUla",                   31),
            Quad(99059, "Fabian Lienhard",           "Tudor Pro Cycling Team",             31)
        )


        corredores.forEach { (id, nombre, equipo, edad) ->
            dbw.execSQL(
                "INSERT OR IGNORE INTO corredores (id, nombre, equipo, edad, genero, id_deporte) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf(id, nombre, equipo, edad, "Masculino", 1)
            )
        }

        val participantes = listOf(
            "Mathieu van der Poel"    to 1,
            "Tadej Pogačar"           to 21,
            "Mads Pedersen"           to 11,
            "Wout van Aert"           to 36,
            "Florian Vermeersch"      to 26,
            "Jonas Rutsch"            to 144,
            "Stefan Bissegger"        to 71,
            "Markus Hoelgaard"        to 83,
            "Fred Wright"             to 137,
            "Laurenz Rex"             to 143,

            "Jasper Philipsen"        to 3,
            "Marco Haller"            to 111,
            "Filippo Ganna"           to 41,
            "Madis Mihkels"           to 164,
            "Biniam Girmay"           to 141,
            "Mike Teunissen"          to 106,
            "Daan Hoole"              to 13,
            "Mick van Dijke"          to 66,
            "Marius Mayrhofer"        to 116,
            "Taco van der Hoorn"      to 145,

            "Johan Jacobs"            to 55,
            "Dries De Bondt"          to 73,
            "Phil Bauhaus"            to 132,
            "António Morgado"         to 25,
            "Florian Dauphin"         to 174,
            "Erik Nordsæter Resell"   to 86,
            "Axel Huens"              to 244,
            "Yves Lampaert"           to 93,
            "Cees Bol"                to 102,
            "Tim van Dijke"           to 65,

            "Rasmus S. Pedersen"      to 77,
            "Frederik Frison"         to 233,
            "Tomáš Kopecký"           to 245,
            "Edward Planckaert"       to 4,
            "Dylan van Baarle"        to 31,
            "Tim Merlier"             to 91,
            "Giacomo Nizzolo"         to 231,
            "Jenthe Biermans"         to 152,
            "Anthony Turgis"          to 171,
            "Sébastien Grignard"      to 205,

            "Damien Touzé"            to 127,
            "Søren Wærenskjold"       to 88,
            "Stefan Küng"             to 51,
            "Matthew Brennan"         to 34,
            "Stanisław Aniołkowski"   to 122,
            "Lukáš Kubiš"             to 241,
            "Hugo Hofstetter"         to 181,
            "Guillaume Boivin"        to 182,
            "Vlad Van Mechelen"       to 136,
            "Mikkel Bjerg"            to 22,

            "Cedric Beullens"         to 202,
            "Joshua Tarling"          to 151,
            "Jordi Meeus"             to 61,
            "Joshua Giddings"         to 204,
            "Andreas Stokbro"         to 246,
            "Pavel Bittner"           to 221,
            "Tom Van Asbroeck"        to 187,
            "Petr Kelemen"            to 113,
            "Max Walscheid"           to 191,
            "Fabian Lienhard"         to 115
        )

        participantes.forEach { (nombre, dorsal) ->
            val cur = dbw.rawQuery(
                "SELECT id FROM corredores WHERE nombre = ?",
                arrayOf(nombre)
            )
            var idCorredor = -1
            if (cur.moveToFirst()) {
                idCorredor = cur.getInt(0)
            }
            cur.close()
            dbw.execSQL(
                "INSERT INTO participante_carrera (id_participante, id_carrera, dorsal) VALUES (?, ?, ?)",
                arrayOf(idCorredor, idCarrera, dorsal)
            )
        }

        val tiempos = listOf(
            "05:31:27","05:32:45","05:33:38","05:33:38","05:35:13","05:35:13","05:35:13","05:35:13","05:36:02","05:36:03",
            "05:36:35","05:37:12","05:37:45","05:38:10","05:38:55","05:39:20","05:39:48","05:40:15","05:40:42","05:41:05",
            "05:41:30","05:42:10","05:42:45","05:43:20","05:43:55","05:44:30","05:45:05","05:45:40","05:46:15","05:46:50",
            "05:47:25","05:48:00","05:48:35","05:49:10","05:49:45","05:50:20","05:50:55","05:51:30","05:52:05","05:52:40",
            "05:53:15","05:53:50","05:54:25","05:55:00","05:55:35","05:56:10","05:56:45","05:57:20","05:57:55","05:58:30",
            "05:59:05","05:59:40","06:00:15","06:00:50","06:01:25","06:02:00","06:02:35","06:03:10","06:03:45","06:04:20"
        )

        participantes.forEachIndexed { index, (nombre, _) ->
            val cur1 = dbw.rawQuery("SELECT id FROM corredores WHERE nombre = ?", arrayOf(nombre))
            var idCorredor = -1
            if (cur1.moveToFirst()) idCorredor = cur1.getInt(0)
            cur1.close()

            val cur2 = dbw.rawQuery(
                "SELECT id FROM participante_carrera WHERE id_participante = ? AND id_carrera = ?",
                arrayOf(idCorredor.toString(), idCarrera.toString())
            )
            var idParticipanteCarrera = -1
            if (cur2.moveToFirst()) idParticipanteCarrera = cur2.getInt(0)
            cur2.close()

            dbw.execSQL(
                "INSERT INTO resultados_carrera (id_participante_carrera, tiempo, posicion) VALUES (?, ?, ?)",
                arrayOf(idParticipanteCarrera, tiempos[index], index + 1)
            )
        }
        val existeCursor2 = dbw.rawQuery(
            "SELECT COUNT(*) FROM carreras WHERE nombre_carrera = ?",
            arrayOf("Milan-San Remo")
        )
        var existe2 = false
        if (existeCursor2.moveToFirst()) {
            existe2 = existeCursor2.getInt(0) > 0
        }
        existeCursor2.close()

        if (existe2) {
            Toast.makeText(
                this,
                "La carrera París-Roubaix 2025 ya está creada. No se realiza ninguna acción.",
                Toast.LENGTH_LONG
            ).show()
            return
        }else {
            val clasicas = listOf(
                Quad("Milan-San Remo 2025", 1, "Italia", "22/03/2025"),
                Quad("Strade Bianche 2025", 1, "Italia", "01/03/2025"),
                Quad("Tour of Flanders 2025", 1, "Bélgica", "06/04/2025"),
                Quad("Liège–Bastogne–Liège 2025", 1, "Bélgica", "27/04/2025"),
                Quad("Il Lombardia 2025", 1, "Italia", "11/10/2025"),
                Quad("Amstel Gold Race 2025", 1, "Países Bajos", "13/04/2025"),
                Quad("La Flèche Wallonne 2025", 1, "Bélgica", "23/04/2025"),
                Quad("Clásica de San Sebastián 2025", 1, "España", "02/08/2025"),
                Quad("E3 Saxo Bank Classic 2025", 1, "Bélgica", "28/03/2025"),
                Quad("Gent–Wevelgem 2025", 1, "Bélgica", "30/03/2025"),
                Quad("Dwars door Vlaanderen 2025", 1, "Bélgica", "02/04/2025"),
                Quad("Omloop Het Nieuwsblad 2025", 1, "Bélgica", "22/02/2025"),
                Quad("Kuurne–Brussels–Kuurne 2025", 1, "Bélgica", "23/02/2025"),
                Quad("Brabantse Pijl 2025", 1, "Bélgica", "16/04/2025"),
                Quad("Trofeo Laigueglia 2025", 1, "Italia", "26/02/2025"),
                Quad("GP Miguel Induráin 2025", 1, "España", "05/04/2025"),
                Quad("GP de Québec 2025", 1, "Canadá", "12/09/2025"),
                Quad("GP de Montréal 2025", 1, "Canadá", "14/09/2025"),
                Quad("Clásica de Hamburgo 2025", 1, "Alemania", "23/08/2025"),
                Quad("RideLondon–Surrey Classic 2025", 1, "Reino Unido", "24/05/2025")

            )

            clasicas.forEach { (nombre, idDeporte, localidad, fecha) ->
                dbw.execSQL(
                    "INSERT OR IGNORE INTO carreras (nombre_carrera, id_deporte, localidad, fecha) VALUES (?, ?, ?, ?)",
                    arrayOf(nombre, idDeporte, localidad, fecha)
                )
            }
        }

        Toast.makeText(this, "Se han insertado los 60 primeros corredores de París-Roubaix 2025 y 20 clásicas del calendario 2025", Toast.LENGTH_LONG).show()
    }

    private fun borrarCorredores(context: Context, inicio: Int = 99000, fin: Int = 99059): Int {
        val dbw = db.writableDatabase
        val filasBorradas = dbw.delete("corredores", "id BETWEEN ? AND ?", arrayOf(inicio.toString(), fin.toString()))


        if (filasBorradas > 0) {
            Toast.makeText(context, "Se han borrado $filasBorradas corredores de París-Roubaix 2025", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "No se ha borrado ningún corredor en el rango especificado", Toast.LENGTH_SHORT).show()
        }

        return filasBorradas
    }


}