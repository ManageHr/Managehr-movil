package com.example.appinterface

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.VacacionesAdapter
import com.example.appinterface.Api.RetrofitInstance
import com.example.appinterface.Models.VacacionesDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VacacionesActivity : BaseActivity() {
    override val selfMenuItemId: Int = R.id.nav_vacaciones
    private lateinit var tvMensaje: TextView
    private lateinit var etMotivo: EditText
    private lateinit var etFechaInicio: EditText
    private lateinit var etFechaFinal: EditText
    private lateinit var etContratoId: EditText
    private lateinit var etDias: EditText

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: VacacionesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setModuleContent(R.layout.vacaciones_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, b.bottom); insets
        }
        supportActionBar?.title = "Manage Hr"

        // UI refs
        tvMensaje     = findViewById(R.id.tvMensaje)
        etMotivo      = findViewById(R.id.etMotivo)
        etFechaInicio = findViewById(R.id.etFechaInicio)
        etFechaFinal  = findViewById(R.id.etFechaFinal)
        etContratoId  = findViewById(R.id.etContratoId)
        etDias        = findViewById(R.id.etDias)

        // Recycler
        recycler = findViewById(R.id.RecyVacaciones)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = VacacionesAdapter()
        recycler.adapter = adapter

        // Botones
        findViewById<Button>(R.id.btnCrearVacacion).setOnClickListener { crearVacacion(it) }
        findViewById<Button>(R.id.btnMostrarVacaciones).setOnClickListener { cargarVacaciones() }

        cargarVacaciones()
    }

    // ===== Helpers =====

    private fun isValidDate(date: String) =
        date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))

    /** Cálculo de días inclusivo (compatible con minSdk bajos). */
    private fun diffDiasCompat(inicio: String, fin: String): Int? {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply { isLenient = false }
        return try {
            val d1 = sdf.parse(inicio) ?: return null
            val d2 = sdf.parse(fin) ?: return null
            val diffMs = d2.time - d1.time
            if (diffMs < 0L) null
            else (java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffMs).toInt() + 1)
        } catch (_: java.text.ParseException) { null }
    }

    private fun showMsg(msg: String, error: Boolean) {
        tvMensaje.text = msg
        tvMensaje.setTextColor(if (error) 0xFFFF0000.toInt() else 0xFF00FF00.toInt())
    }

    private fun limpiar() {
        etMotivo.text?.clear()
        etFechaInicio.text?.clear()
        etFechaFinal.text?.clear()
        etContratoId.text?.clear()
        etDias.text?.clear()
    }

    // ===== Acciones =====

    private fun crearVacacion(view: View? = null) {
        val motivo   = etMotivo.text.toString().trim()
        val fIni     = etFechaInicio.text.toString().trim()
        val fFin     = etFechaFinal.text.toString().trim()
        val contrato = etContratoId.text.toString().toIntOrNull()
        var dias     = etDias.text.toString().toIntOrNull()

        if (motivo.isEmpty() || fIni.isEmpty() || fFin.isEmpty() || contrato == null) {
            showMsg("Todos los campos son obligatorios", true); return
        }
        if (!isValidDate(fIni) || !isValidDate(fFin)) {
            showMsg("Fecha en formato YYYY-MM-DD", true); return
        }
        if (fFin < fIni) {
            showMsg("La fecha final no puede ser menor a la inicial", true); return
        }

        // Si no escriben días, calcúlalos
        if (dias == null) {
            dias = diffDiasCompat(fIni, fFin) ?: run {
                showMsg("No pude calcular días. Revisa las fechas.", true); return
            }
        }
        if (dias <= 0) { showMsg("Días debe ser mayor a 0", true); return }

        // IMPORTANTE: estos nombres deben coincidir con tu VacacionesDto
        val dto = VacacionesDto(
            idVacaciones = null,
            motivo       = motivo,
            fechaInicio  = fIni,
            fechaFinal   = fFin,
            contratoId   = contrato,
            dias         = dias,
            estado       = "Pendiente"
        )


        RetrofitInstance.api2kotlin.crearVacaciones(dto).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("VacacionesActivity", "crear -> ${response.code()}")
                if (response.isSuccessful) {
                    showMsg("✓ Vacación creada", false)
                    limpiar()
                    cargarVacaciones()
                } else {
                    showMsg("Error al crear: ${response.code()}", true)
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                showMsg("✗ Error de conexión", true)
            }
        })
    }

    private fun cargarVacaciones() {
        RetrofitInstance.api2kotlin.obtenerVacaciones()
            .enqueue(object : Callback<List<VacacionesDto>> {
                override fun onResponse(
                    call: Call<List<VacacionesDto>>,
                    response: Response<List<VacacionesDto>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body().orEmpty()
                        adapter.submitList(data)
                        showMsg(
                            if (data.isEmpty()) "Sin registros"
                            else "${data.size} registros cargados",
                            false
                        )
                    } else {
                        showMsg("Error al cargar: ${response.code()}", true)
                    }
                }
                override fun onFailure(call: Call<List<VacacionesDto>>, t: Throwable) {
                    showMsg("✗ Error de conexión al cargar", true)
                }
            })
    }
}
