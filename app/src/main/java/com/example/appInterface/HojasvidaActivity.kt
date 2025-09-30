package com.example.appInterface

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.HojaDeVidaAdapter
import com.example.appinterface.Api.RetrofitInstance
import com.example.appinterface.BaseActivity
import com.example.appinterface.Models.HojaDeVidaDto
import com.example.appinterface.R

class HojasvidaActivity: BaseActivity() {
    override val selfMenuItemId: Int = R.id.nav_hojasvida
    private lateinit var adapter: HojaDeVidaAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvMensaje: TextView
    private var hojaDeVidaSeleccionada: HojaDeVidaDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setModuleContent(R.layout.activity_hojadevida)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvId)) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvMensaje = findViewById(R.id.tvMensaje)

        recyclerView = findViewById(R.id.RecyHojadevida)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HojaDeVidaAdapter(emptyList()) { hojaDeVida ->
            mostrarDialogoEditarHojaDeVida(hojaDeVida)
        }
        recyclerView.adapter = adapter


        val btnCrear = findViewById<Button>(R.id.btnGuardar)
        val buttonGoToSecondActivity: Button = findViewById(R.id.btnRegresar)

        btnCrear.setOnClickListener {
            crearHojaDeVida(it)
        }

        buttonGoToSecondActivity.setOnClickListener {
            val intent = Intent(this, ExperienciaActivity::class.java)
            startActivity(intent)
        }

        // Cargar la lista al inicio
        mostrarHojasDeVida()
    }
    fun regresar(view: View) {
        val intent = Intent(this, ExperienciaActivity::class.java)
        startActivity(intent)
    }
    private fun mostrarHojasDeVida() {
        RetrofitInstance.api2kotlin.obtenerHojasDeVida().enqueue(object : retrofit2.Callback<List<HojaDeVidaDto>> {
            override fun onResponse(
                call: retrofit2.Call<List<HojaDeVidaDto>>,
                response: retrofit2.Response<List<HojaDeVidaDto>>
            ) {
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    adapter.submitList(data)
                    if (data.isEmpty()) {
                        mostrarMensaje("No hay hojas de vida registradas", false)
                    } else {
                        mostrarMensaje("${data.size} hojas de vida cargadas", false)
                    }
                } else {
                    mostrarMensaje("Error al cargar: ${response.code()}", true)
                }
            }

            override fun onFailure(call: retrofit2.Call<List<HojaDeVidaDto>>, t: Throwable) {
                mostrarMensaje("Error de conexión", true)
            }
        })
    }

    private fun crearHojaDeVida(view: View) {
        val etClaseLibreta = findViewById<EditText>(R.id.etClaseLibretaMilitar)
        val etNumeroLibreta = findViewById<EditText>(R.id.etNumeroLibreta)
        val etNumDocumento = findViewById<EditText>(R.id.etUsuarioNumDocumento)

        if (etClaseLibreta.text.isNullOrEmpty() || etNumeroLibreta.text.isNullOrEmpty() || etNumDocumento.text.isNullOrEmpty()) {
            mostrarMensaje("Todos los campos son obligatorios", true)
            return
        }

        val hoja = HojaDeVidaDto(
            idHojaDeVida = null,
            claseLibretaMilitar = etClaseLibreta.text.toString(),
            numeroLibretaMilitar = etNumeroLibreta.text.toString(),
            usuarioNumDocumento = etNumDocumento.text.toString().toLong()
        )

        RetrofitInstance.api2kotlin.crearHojasDeVida(hoja).enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    mostrarMensaje("Hoja de vida creada", false)
                    limpiarCampos()
                    mostrarHojasDeVida()
                } else {
                    mostrarMensaje("Error al crear: ${response.code()}", true)
                }
            }

            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                mostrarMensaje("Error de conexión", true)
            }
        })
    }
    private fun mostrarDialogoEditarHojaDeVida(hojaDeVida: HojaDeVidaDto) {
        hojaDeVidaSeleccionada = hojaDeVida

        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_editar_hojavida)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val etClaseLibreta = dialog.findViewById<EditText>(R.id.etEditClaseLibreta)
        val etNumeroLibreta = dialog.findViewById<EditText>(R.id.etEditNumeroLibreta)
        val etNumDocumento = dialog.findViewById<EditText>(R.id.etEditNumDocumento)

        // Llenar campos con datos actuales
        etClaseLibreta.setText(hojaDeVida.claseLibretaMilitar)
        etNumeroLibreta.setText(hojaDeVida.numeroLibretaMilitar)
        etNumDocumento.setText(hojaDeVida.usuarioNumDocumento.toString())

        // Botón Guardar Cambios
        dialog.findViewById<Button>(R.id.btnGuardarCambios).setOnClickListener {
            val claseLibreta = etClaseLibreta.text.toString()
            val numeroLibreta = etNumeroLibreta.text.toString()
            val numDocumento = etNumDocumento.text.toString()

            if (claseLibreta.isEmpty() || numeroLibreta.isEmpty() || numDocumento.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hojaActualizada = hojaDeVida.copy(
                claseLibretaMilitar = claseLibreta,
                numeroLibretaMilitar = numeroLibreta,
                usuarioNumDocumento = numDocumento.toLongOrNull() ?: 0L
            )

            actualizarHojaDeVida(hojaActualizada)
            dialog.dismiss()
        }

        // Botón Eliminar
        dialog.findViewById<Button>(R.id.btnEliminarHoja).setOnClickListener {
            dialog.dismiss()
            mostrarDialogoConfirmarEliminacion(hojaDeVida)
        }

        dialog.show()
    }
    private fun mostrarDialogoConfirmarEliminacion(hojaDeVida: HojaDeVidaDto) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_confirmar_eliminar)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val tvMensajeConfirmacion = dialog.findViewById<TextView>(R.id.tvMensajeConfirmacion)
        tvMensajeConfirmacion.text = "¿Estás seguro de que deseas eliminar la hoja de vida con ID: ${hojaDeVida.idHojaDeVida}?\n\nEsta acción no se puede deshacer."

        // Botón Cancelar
        dialog.findViewById<Button>(R.id.btnCancelarEliminar).setOnClickListener {
            dialog.dismiss()
        }

        // Botón Confirmar Eliminar
        dialog.findViewById<Button>(R.id.btnConfirmarEliminar).setOnClickListener {
            eliminarHojaDeVida(hojaDeVida.idHojaDeVida!!)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun actualizarHojaDeVida(hojaDeVida: HojaDeVidaDto) {
        val id = hojaDeVida.idHojaDeVida ?: return

        RetrofitInstance.api2kotlin.actualizarHojaDeVida(id, hojaDeVida).enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    mostrarMensaje("Hoja de vida actualizada correctamente", false)
                    mostrarHojasDeVida() // Recargar lista
                    Toast.makeText(this@HojasvidaActivity, "Hoja de vida actualizada", Toast.LENGTH_SHORT).show()
                } else {
                    when (response.code()) {
                        404 -> mostrarMensaje("Error: Hoja de vida no encontrada", true)
                        400 -> mostrarMensaje("Error: Datos inválidos", true)
                        else -> mostrarMensaje("Error al actualizar: ${response.code()}", true)
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                mostrarMensaje("Error de conexión al actualizar", true)
            }
        })
    }

    private fun eliminarHojaDeVida(id: Long) {
        RetrofitInstance.api2kotlin.eliminarHojaDeVida(id).enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    mostrarMensaje("Hoja de vida eliminada correctamente", false)
                    mostrarHojasDeVida() // Recargar lista
                    Toast.makeText(this@HojasvidaActivity, "Hoja de vida eliminada", Toast.LENGTH_SHORT).show()
                } else {
                    when (response.code()) {
                        404 -> mostrarMensaje("Error: Hoja de vida no encontrada", true)
                        400 -> mostrarMensaje("Error: No se pudo eliminar la hoja de vida", true)
                        else -> mostrarMensaje("Error al eliminar: ${response.code()}", true)
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                mostrarMensaje("Error de conexión al eliminar", true)
            }
        })
    }

    private fun mostrarMensaje(mensaje: String, esError: Boolean) {
        tvMensaje.text = mensaje
        tvMensaje.setTextColor(if (esError) 0xFFFF0000.toInt() else 0xFF00FF00.toInt())
    }

    private fun limpiarCampos() {
        findViewById<EditText>(R.id.etClaseLibretaMilitar).text.clear()
        findViewById<EditText>(R.id.etNumeroLibreta).text.clear()
        findViewById<EditText>(R.id.etUsuarioNumDocumento).text.clear()
    }
}