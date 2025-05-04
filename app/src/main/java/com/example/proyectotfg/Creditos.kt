package com.example.proyectotfg

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Creditos : AppCompatActivity() {
    private lateinit var botongithub : ImageView
    private lateinit var botonlinkedin : ImageView
    private lateinit var botoncifp : ImageView
    private lateinit var botondescargardatos : ImageButton
    private lateinit var db: BaseDatosEjemplo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_creditos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        botongithub = findViewById(R.id.imageViewgithubicon)
        botonlinkedin = findViewById(R.id.imageViewlinkedinicon)
        botoncifp = findViewById(R.id.imageViewlogocifp)
        botondescargardatos = findViewById(R.id.imageButtonDescargarDatos)
        db = BaseDatosEjemplo(this, "ProyectoTFG", null, 1)


        botongithub.setOnClickListener {
            val githubUrl = "https://github.com/HugoSerranoH/ProyectoTFG"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
            startActivity(intent)
        }

        botonlinkedin.setOnClickListener {
            val linkedinUrl = "https://www.linkedin.com/in/hugo-serrano-hernández-baaa512a6"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkedinUrl))
            startActivity(intent)
        }

        botoncifp.setOnClickListener {
            val linkedinUrl = "http://cifpjuandeherrera.centros.educa.jcyl.es/sitio/"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkedinUrl))
            startActivity(intent)
        }

        botondescargardatos.setOnClickListener {
            anadirciclistas()
//            borrarCorredores(this)
        }

    }
    data class Corredor(val id: Int, val nombre: String, val equipo: String, val edad: Int)
    data class Carrera(val nombre: String, val idDeporte: Int, val localidad: String, val fecha: String)

    private fun anadirciclistas() {
        val dbw = db.writableDatabase

        val existeCarreraCursor = dbw.rawQuery("SELECT COUNT(*) FROM carreras WHERE nombre_carrera = ?", arrayOf("París-Roubaix 2025")
        )
        var existecarrera = false
        if (existeCarreraCursor.moveToFirst()) {
            existecarrera = existeCarreraCursor.getInt(0) > 0
        }
        existeCarreraCursor.close()

        val existeCorredorCursor = dbw.rawQuery("SELECT COUNT(*) FROM corredores WHERE nombre = ?", arrayOf("António Morgado")
        )
        var existecorredor = false
        if (existeCorredorCursor.moveToFirst()) {
            existecorredor = existeCorredorCursor.getInt(0) > 0
        }
        existeCorredorCursor.close()


        if (existecarrera || existecorredor) {
            Toast.makeText(this, "Los datos ya fueron insertados antes", Toast.LENGTH_LONG).show()
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
            Corredor(99000, "Mathieu van der Poel",      "Alpecin - Deceuninck",               30),
            Corredor(99001, "Tadej Pogačar",             "UAE Team Emirates - XRG",            26),
            Corredor(99002, "Mads Pedersen",             "Lidl - Trek",                        29),
            Corredor(99003, "Wout van Aert",             "Team Visma | Lease a Bike",          30),
            Corredor(99004, "Florian Vermeersch",        "UAE Team Emirates - XRG",            26),
            Corredor(99005, "Jonas Rutsch",              "Intermarché - Wanty",                27),
            Corredor(99006, "Stefan Bissegger",          "Decathlon AG2R La Mondiale",    26),
            Corredor(99007, "Markus Hoelgaard",          "Uno-X Mobility",                     30),
            Corredor(99008, "Fred Wright",               "Bahrain - Victorious",               25),
            Corredor(99009, "Laurenz Rex",               "Intermarché - Wanty",                25),

            Corredor(99010, "Jasper Philipsen",          "Alpecin - Deceuninck",               27),
            Corredor(99011, "Marco Haller",              "Tudor Pro Cycling Team",             34),
            Corredor(99012, "Filippo Ganna",             "INEOS Grenadiers",                   28),
            Corredor(99013, "Madis Mihkels",             "EF Education-EasyPost",              21),
            Corredor(99014, "Biniam Girmay",             "Intermarché - Wanty",                25),
            Corredor(99015, "Mike Teunissen",            "XDS Astana Team",                    32),
            Corredor(99016, "Daan Hoole",                "Lidl - Trek",                        26),
            Corredor(99017, "Mick van Dijke",            "Red Bull - BORA-hansgrohe",          25),
            Corredor(99018, "Marius Mayrhofer",          "Tudor Pro Cycling Team",             24),
            Corredor(99019, "Taco van der Hoorn",        "Intermarché - Wanty",                31),

            Corredor(99020, "Johan Jacobs",              "Groupama - FDJ",                     28),
            Corredor(99021, "Dries De Bondt",            "Decathlon AG2R La Mondiale",    33),
            Corredor(99022, "Phil Bauhaus",              "Bahrain - Victorious",               30),
            Corredor(99023, "António Morgado",           "UAE Team Emirates - XRG",            21),
            Corredor(99024, "Florian Dauphin",           "Team TotalEnergies",                 26),
            Corredor(99025, "Erik Nordsæter Resell",     "Uno-X Mobility",                     28),
            Corredor(99026, "Axel Huens",                "Unibet Tietema Rockets",             23),
            Corredor(99027, "Yves Lampaert",             "Soudal Quick-Step",                  34),
            Corredor(99028, "Cees Bol",                  "XDS Astana Team",                    29),
            Corredor(99029, "Tim van Dijke",             "Red Bull - BORA-hansgrohe",          25),

            Corredor(99030, "Rasmus S. Pedersen",        "Decathlon AG2R La Mondiale",    22),
            Corredor(99031, "Frederik Frison",           "Q36.5 Pro Cycling Team",             32),
            Corredor(99032, "Tomáš Kopecký",             "Unibet Tietema Rockets",             25),
            Corredor(99033, "Edward Planckaert",         "Alpecin - Deceuninck",               30),
            Corredor(99034, "Dylan van Baarle",          "Team Visma | Lease a Bike",          32),
            Corredor(99035, "Tim Merlier",               "Soudal Quick-Step",                  32),
            Corredor(99036, "Giacomo Nizzolo",           "Q36.5 Pro Cycling Team",             36),
            Corredor(99037, "Jenthe Biermans",           "Arkéa - B&B Hotels",                 29),
            Corredor(99038, "Anthony Turgis",            "Team TotalEnergies",                 30),
            Corredor(99039, "Sébastien Grignard",        "Lotto",                              25),

            Corredor(99040, "Damien Touzé",              "Cofidis",                            28),
            Corredor(99041, "Søren Wærenskjold",         "Uno-X Mobility",                     25),
            Corredor(99042, "Stefan Küng",               "Groupama - FDJ",                     31),
            Corredor(99043, "Matthew Brennan",           "Team Visma | Lease a Bike",          19),
            Corredor(99044, "Stanisław Aniołkowski",     "Cofidis",                            28),
            Corredor(99045, "Lukáš Kubiš",               "Unibet Tietema Rockets",             25),
            Corredor(99046, "Hugo Hofstetter",           "Israel - Premier Tech",              31),
            Corredor(99047, "Guillaume Boivin",          "Israel - Premier Tech",              35),
            Corredor(99048, "Vlad Van Mechelen",         "Bahrain - Victorious",               20),
            Corredor(99049, "Mikkel Bjerg",              "UAE Team Emirates - XRG",            26),

            Corredor(99050, "Cedric Beullens",           "Lotto",                              28),
            Corredor(99051, "Joshua Tarling",            "INEOS Grenadiers",                   21),
            Corredor(99052, "Jordi Meeus",               "Red Bull - BORA-hansgrohe",          26),
            Corredor(99053, "Joshua Giddings",           "Lotto",                              21),
            Corredor(99054, "Andreas Stokbro",           "Unibet Tietema Rockets",             28),
            Corredor(99055, "Pavel Bittner",             "Team Picnic PostNL",                 22),
            Corredor(99056, "Tom Van Asbroeck",          "Israel - Premier Tech",              34),
            Corredor(99057, "Petr Kelemen",              "Tudor Pro Cycling Team",             24),
            Corredor(99058, "Max Walscheid",             "Team Jayco AlUla",                   31),
            Corredor(99059, "Fabian Lienhard",           "Tudor Pro Cycling Team",             31)
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
            arrayOf("Milan-San Remo 2025")
        )
        var existe2 = false
        if (existeCursor2.moveToFirst()) {
            existe2 = existeCursor2.getInt(0) > 0
        }
        existeCursor2.close()

        if (existe2) {
            Toast.makeText(this, "Solo se añade Roubaix y corredores.No las 20 clásicas", Toast.LENGTH_LONG).show()
            return
        }else {
            val clasicas = listOf(
                Carrera("Milan-San Remo 2025", 1, "Italia", "22/03/2025"),
                Carrera("Strade Bianche 2025", 1, "Italia", "01/03/2025"),
                Carrera("Ronde Van Vlaanderen 2025", 1, "Bélgica", "06/04/2025"),
                Carrera("Liège–Bastogne–Liège 2025", 1, "Bélgica", "27/04/2025"),
                Carrera("Il Lombardia 2025", 1, "Italia", "11/10/2025"),
                Carrera("Amstel Gold Race 2025", 1, "Países Bajos", "13/04/2025"),
                Carrera("La Flèche Wallonne 2025", 1, "Bélgica", "23/04/2025"),
                Carrera("Clásica de San Sebastián 2025", 1, "España", "02/08/2025"),
                Carrera("E3 Saxo Bank Classic 2025", 1, "Bélgica", "28/03/2025"),
                Carrera("Gent–Wevelgem 2025", 1, "Bélgica", "30/03/2025"),
                Carrera("Dwars door Vlaanderen 2025", 1, "Bélgica", "02/04/2025"),
                Carrera("Omloop Het Nieuwsblad 2025", 1, "Bélgica", "22/02/2025"),
                Carrera("Kuurne–Brussels–Kuurne 2025", 1, "Bélgica", "23/02/2025"),
                Carrera("GP Miguel Induráin 2025", 1, "España", "05/04/2025"),
                Carrera("GP de Québec 2025", 1, "Canadá", "12/09/2025"),
                Carrera("GP de Montréal 2025", 1, "Canadá", "14/09/2025"),

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