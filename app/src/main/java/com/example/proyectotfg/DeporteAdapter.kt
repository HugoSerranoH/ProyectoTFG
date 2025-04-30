package com.example.proyectotfg
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class DeporteAdapter(context: Context, private val deportes: List<String>, private val icons: List<Int>) :
    ArrayAdapter<String>(context, 0, deportes) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinnerdeportes, parent, false)

        val icon = view.findViewById<ImageView>(R.id.deporte_icon)
        val name = view.findViewById<TextView>(R.id.deporte_name)

        icon.setImageResource(icons[position])
        name.text = deportes[position]

        return view
    }
}