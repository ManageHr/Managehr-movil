package com.example.appinterface

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.ExperienciaAdapter
import com.example.appinterface.Api.RetrofitInstance
import com.example.appinterface.Models.ExperienciaDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExperienciaActivity: AppCompatActivity() {
    private lateinit var adapter: ExperienciaAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvMensaje: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_experiencia)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        tvMensaje = findViewById(R.id.tvMensaje)


        recyclerView = findViewById(R.id.RecyExperiencias)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ExperienciaAdapter()
        recyclerView.adapter = adapter

        val buttonGoToSecondActivity: Button = findViewById(R.id.buttonSegundaActividad)
        buttonGoToSecondActivity.setOnClickListener {
            val intent = Intent(this, ProductosActivity::class.java)
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

        buttonGoToSecondActivity.setOnClickListener {
            val intent = Intent(this, ProductosActivity::class.java)
            startActivity(intent)
        }

        Log.d("MainActivity", "Configuración completada")


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