package com.example.proyectotfg

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class BaseDatosEjemplo(
    contexto: Context?,
    nombre: String?,
    cursorFactory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(
    contexto,
    nombre,
    cursorFactory,
    version
) {
    override fun onCreate(db: SQLiteDatabase) {
        // Tabla usuarios
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre_usuario TEXT NOT NULL,
                contraseña TEXT NOT NULL,
                telefono TEXT NOT NULL,
                email TEXT NOT NULL,
                sexo TEXT CHECK( sexo IN ('Masculino', 'Femenino', 'Otro') ) NOT NULL,
                ultima_sesion DATETIME
            )"""
        )
        // Tabla deportes
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS deportes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre_deporte TEXT NOT NULL
            )"""
        )
        // Tabla corredores
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS corredores (
                id INTEGER PRIMARY KEY AUTOINCREMENT ,
                nombre VARCHAR(100) NOT NULL,
                equipo VARCHAR(100),
                edad INT NOT NULL,
                genero VARCHAR(10),
                id_deporte INT NOT NULL, 
                FOREIGN KEY (id_deporte) REFERENCES deportes(id) ON DELETE CASCADE
            )"""
        )
        // Tabla carreras
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS carreras (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre_carrera TEXT NOT NULL,
                id_deporte INTEGER NOT NULL,
                localidad VARCHAR(30),
                fecha VARCHAR(30),
                FOREIGN KEY (id_deporte) REFERENCES deportes(id) ON DELETE CASCADE
            )"""
        )
        // Tabla participante_carrera
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS participante_carrera (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                id_participante INTEGER NOT NULL,
                id_carrera INTEGER NOT NULL,
                dorsal INTEGER NOT NULL,
                FOREIGN KEY (id_participante) REFERENCES corredores(id) ON DELETE CASCADE,
                FOREIGN KEY (id_carrera) REFERENCES carreras(id) ON DELETE CASCADE
            )"""
        )
        // Tabla resultados_carrera
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS resultados_carrera (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                id_participante_carrera INTEGER NOT NULL,
                tiempo VARCHAR(30) NOT NULL,
                posicion INTEGER NOT NULL,
                FOREIGN KEY (id_participante_carrera) REFERENCES participante_carrera(id) ON DELETE CASCADE
            )"""
        )
        Log.i("SQL", "Se han creado todas las tablas si no existían previamente")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {



    }
}
