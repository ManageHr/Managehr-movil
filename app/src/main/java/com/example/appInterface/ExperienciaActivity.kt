package com.example.appinterface

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.ExperienciaAdapter
import com.example.appinterface.Api.RetrofitInstance
import com.example.appinterface.BaseActivity
import com.example.appinterface.Models.ExperienciaDto
import com.example.appinterface.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExperienciaActivity: BaseActivity() {
    override val selfMenuItemId: Int = R.id.nav_experiencia

    private lateinit var adapter: ExperienciaAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvMensaje: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setModuleContent(R.layout.activity_experiencia)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        tvMensaje = findViewById(R.id.tvMensaje)

        recyclerView = findViewById(R.id.RecyExperiencias)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // MODIFICA ESTA LÍNEA para agregar el click:
        adapter = ExperienciaAdapter { experiencia ->
            mostrarDialogoEditarExperiencia(experiencia)
        }
        recyclerView.adapter = adapter

        val buttonGoToSecondActivity: Button = findViewById(R.id.buttonSegundaActividad)
        buttonGoToSecondActivity.setOnClickListener {
            val intent = Intent(this, EstudiosActivity::class.java)
            startActivity(intent)
        }

        val btnCrear = findViewById<Button>(R.id.btnCrearExperiencia)
        val btnMostrar = findViewById<Button>(R.id.btnMostrarExperiencias)

        btnCrear.setOnClickListener {
            Log.d("MainActivity", "Botón Crear clickeado")
            crearExperiencia(it)
        }

        btnMostrar.setOnClickListener {
            Log.d("MainActivity", "Botón Mostrar clickeado")
            mostrarExperiencias(it)
        }

        mostrarExperiencias()
    }

    fun crearExperiencia(v: View) {
        val etEmpresa = findViewById<EditText>(R.id.etEmpresa)
        val etCargo = findViewById<EditText>(R.id.etCargo)
        val etJefe = findViewById<EditText>(R.id.etJefe)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etActividades = findViewById<EditText>(R.id.etActividades)
        val etFechaInicio = findViewById<EditText>(R.id.etFechaInicio)
        val etFechaFin = findViewById<EditText>(R.id.etFechaFin)


        if (etEmpresa.text.isNullOrEmpty() || etCargo.text.isNullOrEmpty() ||
            etJefe.text.isNullOrEmpty() || etTelefono.text.isNullOrEmpty() ||
            etActividades.text.isNullOrEmpty() || etFechaInicio.text.isNullOrEmpty() ||
            etFechaFin.text.isNullOrEmpty()) {
            mostrarMensaje("Error: Todos los campos son obligatorios", true)
            return
        }


        val fechaInicio = etFechaInicio.text.toString()
        val fechaFin = etFechaFin.text.toString()

        if (!isValidDateFormat(fechaInicio) || !isValidDateFormat(fechaFin)) {
            mostrarMensaje("Error: Formato debe ser YYYY-MM-DD", true)
            return
        }

        val experienciaDto = ExperienciaDto(
            idExperiencia = null,
            nomEmpresa = etEmpresa.text.toString(),
            nomJefe = etJefe.text.toString(),
            telefono = etTelefono.text.toString(),
            cargo = etCargo.text.toString(),
            actividades = etActividades.text.toString(),
            fechaInicio = fechaInicio,
            fechaFinalizacion = fechaFin
        )

        crearExperienciaEnApi(experienciaDto)
    }

    private fun isValidDateFormat(date: String): Boolean {
        return date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
    }

    private fun crearExperienciaEnApi(experiencia: ExperienciaDto) {
        RetrofitInstance.api2kotlin.crearExperiencia(experiencia).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    mostrarMensaje("Experiencia creada exitosamente", false)
                    limpiarCampos()
                    mostrarExperiencias()
                } else {
                    mostrarMensaje("Error al crear: ${response.code()}", true)
                    Log.e("API", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                mostrarMensaje("Error de conexión: ${t.message}", true)
                Log.e("API", "Falla de red", t)
            }
        })
    }
    fun irEstudios(view: View) {
        val intent = Intent(this, EstudiosActivity::class.java)
        startActivity(intent)
    }
    fun mostrarExperiencias(v: View? = null) {
        println("MainActivity: Solicitando experiencias...")

        RetrofitInstance.api2kotlin.obtenerExperiencias().enqueue(object : Callback<List<ExperienciaDto>> {
            override fun onResponse(call: Call<List<ExperienciaDto>>, response: Response<List<ExperienciaDto>>) {
                println("MainActivity: Respuesta recibida - Código: ${response.code()}")

                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    println("MainActivity: ${data.size} experiencias recibidas")


                    data.forEachIndexed { index, experiencia ->
                        println("Experiencia $index: ${experiencia.nomEmpresa} - ${experiencia.cargo}")
                    }

                    adapter.submitList(data)

                    if (data.isEmpty()) {
                        mostrarMensaje("No hay experiencias registradas", false)
                    } else {
                        mostrarMensaje(" ${data.size} experiencias cargadas", false)
                    }
                } else {
                    mostrarMensaje("Error al cargar: ${response.code()}", true)
                    println("MainActivity: Error response - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<ExperienciaDto>>, t: Throwable) {
                mostrarMensaje("Error de conexión al cargar", true)
                println("MainActivity: Error de conexión - ${t.message}")
                t.printStackTrace()
            }
        })

    }

    private fun mostrarMensaje(mensaje: String, esError: Boolean) {
        tvMensaje.text = mensaje
        tvMensaje.setTextColor(if (esError) 0xFFFF0000.toInt() else 0xFF00FF00.toInt())
    }
    // MÉTODOS PARA EDITAR Y ELIMINAR:

    private fun mostrarDialogoEditarExperiencia(experiencia: ExperienciaDto) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_editar_experiencia)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val etEmpresa = dialog.findViewById<EditText>(R.id.etEditEmpresa)
        val etCargo = dialog.findViewById<EditText>(R.id.etEditCargo)
        val etJefe = dialog.findViewById<EditText>(R.id.etEditJefe)
        val etTelefono = dialog.findViewById<EditText>(R.id.etEditTelefono)
        val etActividades = dialog.findViewById<EditText>(R.id.etEditActividades)
        val etFechaInicio = dialog.findViewById<EditText>(R.id.etEditFechaInicio)
        val etFechaFin = dialog.findViewById<EditText>(R.id.etEditFechaFin)

        // Llenar campos con datos actuales
        etEmpresa.setText(experiencia.nomEmpresa)
        etCargo.setText(experiencia.cargo)
        etJefe.setText(experiencia.nomJefe)
        etTelefono.setText(experiencia.telefono)
        etActividades.setText(experiencia.actividades)
        etFechaInicio.setText(experiencia.fechaInicio)
        etFechaFin.setText(experiencia.fechaFinalizacion)

        // Botón Guardar Cambios
        dialog.findViewById<Button>(R.id.btnGuardarExperiencia).setOnClickListener {
            val empresa = etEmpresa.text.toString()
            val cargo = etCargo.text.toString()
            val jefe = etJefe.text.toString()
            val telefono = etTelefono.text.toString()
            val actividades = etActividades.text.toString()
            val fechaInicio = etFechaInicio.text.toString()
            val fechaFin = etFechaFin.text.toString()

            if (empresa.isEmpty() || cargo.isEmpty() || jefe.isEmpty() ||
                telefono.isEmpty() || actividades.isEmpty() || fechaInicio.isEmpty() ||
                fechaFin.isEmpty()) {
                android.widget.Toast.makeText(this, "Todos los campos son obligatorios", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidDateFormat(fechaInicio) || !isValidDateFormat(fechaFin)) {
                android.widget.Toast.makeText(this, "Error: Formato debe ser YYYY-MM-DD", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fechaFin < fechaInicio) {
                android.widget.Toast.makeText(this, "Error: La fecha de finalización no puede ser menor a la de inicio", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val experienciaActualizada = experiencia.copy(
                nomEmpresa = empresa,
                cargo = cargo,
                nomJefe = jefe,
                telefono = telefono,
                actividades = actividades,
                fechaInicio = fechaInicio,
                fechaFinalizacion = fechaFin
            )

            actualizarExperienciaEnApi(experienciaActualizada)
            dialog.dismiss()
        }

        // Botón Eliminar
        dialog.findViewById<Button>(R.id.btnEliminarExperiencia).setOnClickListener {
            dialog.dismiss()
            mostrarDialogoConfirmarEliminacionExperiencia(experiencia)
        }

        dialog.show()
    }

    private fun mostrarDialogoConfirmarEliminacionExperiencia(experiencia: ExperienciaDto) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_confirmar_eliminar_experiencia)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val tvMensajeConfirmacion = dialog.findViewById<TextView>(R.id.tvMensajeConfirmacion)
        tvMensajeConfirmacion.text = "¿Estás seguro de que deseas eliminar la experiencia: ${experiencia.cargo} en ${experiencia.nomEmpresa}?\n\nEsta acción no se puede deshacer."

        // Botón Cancelar
        dialog.findViewById<Button>(R.id.btnCancelarEliminar).setOnClickListener {
            dialog.dismiss()
        }

        // Botón Confirmar Eliminar
        dialog.findViewById<Button>(R.id.btnConfirmarEliminar).setOnClickListener {
            eliminarExperienciaEnApi(experiencia.idExperiencia!!)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun actualizarExperienciaEnApi(experiencia: ExperienciaDto) {
        val id = experiencia.idExperiencia ?: return

        // CAMBIA Callback<Void> por Callback<ExperienciaDto>
        RetrofitInstance.api2kotlin.actualizarExperiencia(id, experiencia).enqueue(object : Callback<ExperienciaDto> {
            override fun onResponse(call: Call<ExperienciaDto>, response: Response<ExperienciaDto>) {
                if (response.isSuccessful) {
                    mostrarMensaje("✓ Experiencia actualizada correctamente", false)
                    mostrarExperiencias()
                    Toast.makeText(this@ExperienciaActivity, "Experiencia actualizada", Toast.LENGTH_SHORT).show()
                } else {
                    when (response.code()) {
                        404 -> mostrarMensaje("Error: Experiencia no encontrada", true)
                        400 -> mostrarMensaje("Error: Datos inválidos", true)
                        else -> mostrarMensaje("Error al actualizar: ${response.code()}", true)
                    }
                }
            }

            override fun onFailure(call: Call<ExperienciaDto>, t: Throwable) {
                mostrarMensaje("Error de conexión al actualizar", true)
            }
        })
    }

    private fun eliminarExperienciaEnApi(id: Long) {
        // PARA ELIMINAR sí usa Callback<Void>
        RetrofitInstance.api2kotlin.eliminarExperiencia(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    mostrarMensaje("✓ Experiencia eliminada correctamente", false)
                    mostrarExperiencias()
                    Toast.makeText(this@ExperienciaActivity, "Experiencia eliminada", Toast.LENGTH_SHORT).show()
                } else {
                    when (response.code()) {
                        404 -> mostrarMensaje("Error: Experiencia no encontrada", true)
                        400 -> mostrarMensaje("Error: No se pudo eliminar la experiencia", true)
                        else -> mostrarMensaje("Error al eliminar: ${response.code()}", true)
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                mostrarMensaje("Error de conexión al eliminar", true)
            }
        })
    }
    private fun limpiarCampos() {
        findViewById<EditText>(R.id.etEmpresa).text.clear()
        findViewById<EditText>(R.id.etCargo).text.clear()
        findViewById<EditText>(R.id.etJefe).text.clear()
        findViewById<EditText>(R.id.etTelefono).text.clear()
        findViewById<EditText>(R.id.etActividades).text.clear()
        findViewById<EditText>(R.id.etFechaInicio).text.clear()
        findViewById<EditText>(R.id.etFechaFin).text.clear()
    }
}