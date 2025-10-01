package com.example.appinterface

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.EstudiosAdapter
import com.example.appinterface.Models.EstudiosDto
import com.example.appinterface.Api.RetrofitInstance
import com.example.appinterface.BaseActivity
import com.example.appinterface.HojasvidaActivity
import com.example.appinterface.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EstudiosActivity : BaseActivity() {
    override val selfMenuItemId: Int = R.id.nav_estudios
    private lateinit var adapter: EstudiosAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvMensaje: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setModuleContent(R.layout.estudios_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvMensaje = findViewById(R.id.tvMensaje)

        recyclerView = findViewById(R.id.RecyEstudios)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Adapter con función de clic
        adapter = EstudiosAdapter(emptyList()) { estudio ->
            mostrarDialogoEditarEstudio(estudio)
        }
        recyclerView.adapter = adapter

        val btnCrear = findViewById<Button>(R.id.btnCrearEstudio)
        val btnMostrar = findViewById<Button>(R.id.btnMostrarEstudios)
        val buttonSiguiente = findViewById<Button>(R.id.buttonSiguienteActividad)

        btnCrear.setOnClickListener {
            Log.d("EstudiosActivity", "Botón Crear clickeado")
            crearEstudio(it)
        }

        btnMostrar.setOnClickListener {
            Log.d("EstudiosActivity", "Botón Mostrar clickeado")
            mostrarEstudios(it)
        }

        buttonSiguiente.setOnClickListener {
            val intent = Intent(this, HojasvidaActivity::class.java)
            startActivity(intent)
        }

        mostrarEstudios()
    }

    fun crearEstudio(view: View) {
        val etNomEstudio = findViewById<EditText>(R.id.etNomEstudio)
        val etInstitucion = findViewById<EditText>(R.id.etInstitucion)
        val etTituloObtenido = findViewById<EditText>(R.id.etTituloObtenido)
        val etAnioInicio = findViewById<EditText>(R.id.etAnioInicio)
        val etAnioFin = findViewById<EditText>(R.id.etAnioFin)

        if (etNomEstudio.text.isNullOrEmpty() || etInstitucion.text.isNullOrEmpty() ||
            etTituloObtenido.text.isNullOrEmpty() || etAnioInicio.text.isNullOrEmpty() ||
            etAnioFin.text.isNullOrEmpty()) {
            mostrarMensaje("Error: Todos los campos son obligatorios", true)
            return
        }

        val anioInicio = etAnioInicio.text.toString()
        val anioFin = etAnioFin.text.toString()

        if (!isValidDateFormat(anioInicio) || !isValidDateFormat(anioFin)) {
            mostrarMensaje("Error: Formato debe ser YYYY-MM-DD", true)
            return
        }

        if (anioFin < anioInicio) {
            mostrarMensaje("Error: La fecha de finalización no puede ser menor a la de inicio", true)
            return
        }

        val estudiosDto = EstudiosDto(
            idEstudios = null,
            nomEstudio = etNomEstudio.text.toString(),
            nomInstitucion = etInstitucion.text.toString(),
            tituloObtenido = etTituloObtenido.text.toString(),
            anioInicio = anioInicio,
            anioFinalizacion = anioFin
        )

        crearEstudioEnApi(estudiosDto)
    }

    // NUEVOS MÉTODOS PARA EDITAR Y ELIMINAR:

    private fun mostrarDialogoEditarEstudio(estudio: EstudiosDto) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_editar_estudio)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val etNomEstudio = dialog.findViewById<EditText>(R.id.etEditNomEstudio)
        val etInstitucion = dialog.findViewById<EditText>(R.id.etEditInstitucion)
        val etTitulo = dialog.findViewById<EditText>(R.id.etEditTituloObtenido)
        val etAnioInicio = dialog.findViewById<EditText>(R.id.etEditAnioInicio)
        val etAnioFin = dialog.findViewById<EditText>(R.id.etEditAnioFin)

        // Llenar campos con datos actuales
        etNomEstudio.setText(estudio.nomEstudio)
        etInstitucion.setText(estudio.nomInstitucion)
        etTitulo.setText(estudio.tituloObtenido)
        etAnioInicio.setText(estudio.anioInicio)
        etAnioFin.setText(estudio.anioFinalizacion)

        // Botón Guardar Cambios
        dialog.findViewById<Button>(R.id.btnGuardarEstudio).setOnClickListener {
            val nomEstudio = etNomEstudio.text.toString()
            val institucion = etInstitucion.text.toString()
            val titulo = etTitulo.text.toString()
            val anioInicio = etAnioInicio.text.toString()
            val anioFin = etAnioFin.text.toString()

            if (nomEstudio.isEmpty() || institucion.isEmpty() || titulo.isEmpty() ||
                anioInicio.isEmpty() || anioFin.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidDateFormat(anioInicio) || !isValidDateFormat(anioFin)) {
                Toast.makeText(this, "Error: Formato debe ser YYYY-MM-DD", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (anioFin < anioInicio) {
                Toast.makeText(this, "Error: La fecha de finalización no puede ser menor a la de inicio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val estudioActualizado = estudio.copy(
                nomEstudio = nomEstudio,
                nomInstitucion = institucion,
                tituloObtenido = titulo,
                anioInicio = anioInicio,
                anioFinalizacion = anioFin
            )

            actualizarEstudioEnApi(estudioActualizado)
            dialog.dismiss()
        }

        // Botón Eliminar
        dialog.findViewById<Button>(R.id.btnEliminarEstudio).setOnClickListener {
            dialog.dismiss()
            mostrarDialogoConfirmarEliminacionEstudio(estudio)
        }

        dialog.show()
    }

    private fun mostrarDialogoConfirmarEliminacionEstudio(estudio: EstudiosDto) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_confirmar_eliminar_estudio)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val tvMensajeConfirmacion = dialog.findViewById<TextView>(R.id.tvMensajeConfirmacion)
        tvMensajeConfirmacion.text = "¿Estás seguro de que deseas eliminar el estudio: ${estudio.nomEstudio}?\n\nEsta acción no se puede deshacer."

        // Botón Cancelar
        dialog.findViewById<Button>(R.id.btnCancelarEliminar).setOnClickListener {
            dialog.dismiss()
        }

        // Botón Confirmar Eliminar
        dialog.findViewById<Button>(R.id.btnConfirmarEliminar).setOnClickListener {
            eliminarEstudioEnApi(estudio.idEstudios!!)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun actualizarEstudioEnApi(estudio: EstudiosDto) {
        val id = estudio.idEstudios ?: return

        RetrofitInstance.api2kotlin.actualizarEstudio(id, estudio).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    mostrarMensaje("✓ Estudio actualizado correctamente", false)
                    mostrarEstudios() // Recargar lista
                    Toast.makeText(this@EstudiosActivity, "Estudio actualizado", Toast.LENGTH_SHORT).show()
                } else {
                    when (response.code()) {
                        404 -> mostrarMensaje("Error: Estudio no encontrado", true)
                        400 -> mostrarMensaje("Error: Datos inválidos", true)
                        else -> mostrarMensaje("Error al actualizar: ${response.code()}", true)
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                mostrarMensaje("Error de conexión al actualizar", true)
            }
        })
    }

    private fun eliminarEstudioEnApi(id: Long) {
        RetrofitInstance.api2kotlin.eliminarEstudio(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    mostrarMensaje("✓ Estudio eliminado correctamente", false)
                    mostrarEstudios() // Recargar lista
                    Toast.makeText(this@EstudiosActivity, "Estudio eliminado", Toast.LENGTH_SHORT).show()
                } else {
                    when (response.code()) {
                        404 -> mostrarMensaje("Error: Estudio no encontrado", true)
                        400 -> mostrarMensaje("Error: No se pudo eliminar el estudio", true)
                        else -> mostrarMensaje("Error al eliminar: ${response.code()}", true)
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                mostrarMensaje("Error de conexión al eliminar", true)
            }
        })
    }

    // MÉTODOS EXISTENTES (mantener igual):

    private fun isValidDateFormat(date: String): Boolean {
        return date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
    }

    fun irHojas(view: View) {
        val intent = Intent(this, HojasvidaActivity::class.java)
        startActivity(intent)
    }

    private fun crearEstudioEnApi(estudio: EstudiosDto) {
        println("EstudiosActivity: Creando estudio...")

        RetrofitInstance.api2kotlin.crearEstudios(estudio).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                println("EstudiosActivity: Respuesta creación - Código: ${response.code()}")

                if (response.isSuccessful) {
                    mostrarMensaje("✓ Estudio creado exitosamente", false)
                    limpiarCampos()
                    mostrarEstudios()
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Error: Datos inválidos"
                        409 -> "Error: El estudio ya existe"
                        500 -> "Error del servidor"
                        else -> "Error al crear: ${response.code()}"
                    }
                    mostrarMensaje(errorMessage, true)
                    println("EstudiosActivity: Error response - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                mostrarMensaje("✗ Error de conexión al crear estudio", true)
                println("EstudiosActivity: Error de conexión - ${t.message}")
                t.printStackTrace()
            }
        })
    }

    fun mostrarEstudios(view: View? = null) {
        println("EstudiosActivity: Solicitando estudios...")

        RetrofitInstance.api2kotlin.obtenerEstudios().enqueue(object : Callback<List<EstudiosDto>> {
            override fun onResponse(call: Call<List<EstudiosDto>>, response: Response<List<EstudiosDto>>) {
                println("EstudiosActivity: Respuesta recibida - Código: ${response.code()}")

                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    println("EstudiosActivity: ${data.size} estudios recibidos")

                    data.forEachIndexed { index, estudio ->
                        println("Estudio $index: ${estudio.nomEstudio} - ${estudio.nomInstitucion}")
                    }

                    adapter.submitList(data)

                    if (data.isEmpty()) {
                        mostrarMensaje("No hay estudios registrados", false)
                    } else {
                        mostrarMensaje("${data.size} estudios cargados", false)
                    }
                } else {
                    mostrarMensaje("Error al cargar: ${response.code()}", true)
                    println("EstudiosActivity: Error response - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<EstudiosDto>>, t: Throwable) {
                mostrarMensaje("Error de conexión al cargar", true)
                println("EstudiosActivity: Error de conexión - ${t.message}")
                t.printStackTrace()
            }
        })
    }

    private fun mostrarMensaje(mensaje: String, esError: Boolean) {
        tvMensaje.text = mensaje
        tvMensaje.setTextColor(if (esError) 0xFFFF0000.toInt() else 0xFF00FF00.toInt())
    }

    private fun limpiarCampos() {
        findViewById<EditText>(R.id.etNomEstudio).text.clear()
        findViewById<EditText>(R.id.etInstitucion).text.clear()
        findViewById<EditText>(R.id.etTituloObtenido).text.clear()
        findViewById<EditText>(R.id.etAnioInicio).text.clear()
        findViewById<EditText>(R.id.etAnioFin).text.clear()
    }
}