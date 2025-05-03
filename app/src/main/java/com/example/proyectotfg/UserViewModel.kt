package com.example.proyectotfg
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class UserViewModel : ViewModel() {
    private val _usuario = MutableLiveData<String?>()
    val usuario: LiveData<String?> get() = _usuario

    fun setUsuario(usuario: String) {
        _usuario.postValue(usuario)
    }

    fun resetUsuario() {
        _usuario.postValue(null)
    }

    private val _deporteSeleccionado = MutableLiveData<Pair<Int, String>?>()
    val deporteSeleccionado: LiveData<Pair<Int, String>?> get() = _deporteSeleccionado

    fun setDeporteSeleccionado(idDeporte: Int, nombreDeporte: String) {
        //Log.i("DEBUG", "UserViewModel: Guardando ID Deporte -> $idDeporte, Nombre -> $nombreDeporte")
        _deporteSeleccionado.postValue(Pair(idDeporte, nombreDeporte))
    }


    fun resetDeporteSeleccionado() {
        _deporteSeleccionado.postValue(null)
    }
    private val _carreraSeleccionada = MutableLiveData<Pair<Int, String>>()
    val carreraSeleccionada: LiveData<Pair<Int, String>> = _carreraSeleccionada

    fun setCarreraSeleccionada(idCarrera: Int, nombreCarrera: String) {
        //Log.i("DEBUG", "UserViewModel: Guardando ID carrera -> $idCarrera, Nombre -> $nombreCarrera")
        _carreraSeleccionada.postValue(Pair(idCarrera, nombreCarrera))
    }


}


