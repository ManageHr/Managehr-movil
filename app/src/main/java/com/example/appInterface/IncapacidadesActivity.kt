package com.example.appinterface

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.IncapacidadesAdapter
import com.example.appinterface.Api.RetrofitInstance
import com.example.appinterface.Models.IncapacidadesDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class IncapacidadesActivity : BaseActivity() {
    override val selfMenuItemId: Int = R.id.nav_incapacidades
    // UI
    private lateinit var tvMensaje: TextView
    private lateinit var tvArchivoSeleccionado: TextView
    private lateinit var etFechaInicio: EditText
    private lateinit var etFechaFinal: EditText
    private lateinit var etContratoId: EditText
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: IncapacidadesAdapter
    private lateinit var btnSeleccionarArchivo: Button

    // Archivo seleccionado
    private var archivoUri: Uri? = null

    // Launcher para seleccionar archivo (mimeType: cualquiera)
    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        archivoUri = uri
        tvArchivoSeleccionado.text =
            uri?.toString() ?: "Ningún archivo seleccionado"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setModuleContent(R.layout.incapacidades_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, b.bottom); insets
        }
        supportActionBar?.title = "Manage Hr"

        // refs
        tvMensaje = findViewById(R.id.tvMensaje)
        tvArchivoSeleccionado = findViewById(R.id.tvArchivoSeleccionado)
        etFechaInicio = findViewById(R.id.etFechaInicio)
        etFechaFinal = findViewById(R.id.etFechaFinal)
        etContratoId = findViewById(R.id.etContratoId)
        btnSeleccionarArchivo = findViewById(R.id.btnSeleccionarArchivo)

        // abrir selector de archivos
        btnSeleccionarArchivo.setOnClickListener {
            pickFileLauncher.launch("*/*") // acepta cualquier archivo
        }

        // recycler
        recycler = findViewById(R.id.RecyIncapacidades)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = IncapacidadesAdapter()
        recycler.adapter = adapter

        findViewById<Button>(R.id.btnCrearIncapacidad).setOnClickListener { crearIncapacidad(it) }
        findViewById<Button>(R.id.btnMostrarIncapacidades).setOnClickListener { cargarIncapacidades() }

        cargarIncapacidades()
    }

    // ===== helpers =====
    private fun isValidDate(date: String) =
        date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))

    private fun parseable(date: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        return try { sdf.parse(date); true } catch (_: ParseException) { false }
    }

    private fun showMsg(msg: String, error: Boolean) {
        tvMensaje.text = msg
        tvMensaje.setTextColor(if (error) 0xFFFF0000.toInt() else 0xFF00FF00.toInt())
    }

    private fun limpiar() {
        archivoUri = null
        tvArchivoSeleccionado.text = "Ningún archivo seleccionado"
        etFechaInicio.text?.clear()
        etFechaFinal.text?.clear()
        etContratoId.text?.clear()
    }

    // ===== acciones =====
    private fun crearIncapacidad(view: View? = null) {
        val fIni = etFechaInicio.text.toString().trim()
        val fFin = etFechaFinal.text.toString().trim()
        val contratoId = etContratoId.text.toString().toLongOrNull()

        if (archivoUri == null || fIni.isEmpty() || fFin.isEmpty() || contratoId == null) {
            showMsg("Selecciona archivo y completa los campos", true); return
        }
        if (!isValidDate(fIni) || !isValidDate(fFin) || !parseable(fIni) || !parseable(fFin)) {
            showMsg("Fecha en formato YYYY-MM-DD", true); return
        }
        if (fFin < fIni) {
            showMsg("La fecha final no puede ser menor a la inicial", true); return
        }

        val dto = IncapacidadesDto(
            idIncapacidad = null,
            archivo = archivoUri.toString(), // guardamos la URI
            estado = 0,                       // siempre Pendiente
            fechaInicio = fIni,
            fechaFinal = fFin,
            contratoId = contratoId
        )

        RetrofitInstance.api2kotlin.crearIncapacidad(dto).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showMsg("✓ Incapacidad creada", false)
                    limpiar()
                    cargarIncapacidades()
                } else {
                    showMsg("Error al crear: ${response.code()}", true)
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                showMsg("✗ Error de conexión", true)
                Log.e("IncapacidadesActivity", "crear onFailure", t)
            }
        })
    }

    private fun cargarIncapacidades() {
        RetrofitInstance.api2kotlin.obtenerIncapacidades()
            .enqueue(object : Callback<List<IncapacidadesDto>> {
                override fun onResponse(
                    call: Call<List<IncapacidadesDto>>,
                    response: Response<List<IncapacidadesDto>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body().orEmpty()
                        adapter.submitList(data)
                        showMsg(if (data.isEmpty()) "Sin registros" else "${data.size} registros cargados", false)
                    } else {
                        showMsg("Error al cargar: ${response.code()}", true)
                    }
                }
                override fun onFailure(call: Call<List<IncapacidadesDto>>, t: Throwable) {
                    showMsg("✗ Error de conexión al cargar", true)
                }
            })
    }
}
