package com.example.appinterface

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import com.example.appinterface.Models.ExperienciaDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EstudiosActivity : AppCompatActivity() {

    private lateinit var adapter: EstudiosAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvMensaje: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContentView(R.layout.estudios_main) // ← Si este es el nombre correcto



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        tvMensaje = findViewById(R.id.tvMensaje)


        recyclerView = findViewById(R.id.RecyEstudios)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EstudiosAdapter()
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
            val intent = Intent(this, ProductosActivity::class.java)
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

        // USAR LA MISMA VALIDACIÓN QUE EN EXPERIENCIAS
        if (!isValidDateFormat(anioInicio) || !isValidDateFormat(anioFin)) {
            mostrarMensaje("Error: Formato debe ser YYYY-MM-DD", true)
            return
        }

        // Validar que la fecha de finalización no sea menor a la de inicio
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

    // USA LA MISMA FUNCIÓN DE VALIDACIÓN QUE EXPERIENCIAS
    private fun isValidDateFormat(date: String): Boolean {
        return date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
    }

    private fun crearEstudioEnApi(estudio: EstudiosDto) {
        println("EstudiosActivity: Creando estudio...")

        RetrofitInstance.api2kotlin.crearEstudios(estudio).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                println("EstudiosActivity: Respuesta creación - Código: ${response.code()}")

                if (response.isSuccessful) {
                    mostrarMensaje("✓ Estudio creado exitosamente", false)
                    limpiarCampos()
                    mostrarEstudios() // Actualizar la lista
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