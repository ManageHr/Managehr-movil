package com.example.appinterface

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.VacacionesAdapter
import com.example.appinterface.Api.RetrofitInstance
import com.example.appinterface.Models.VacacionesDto
import okhttp3.ResponseBody
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


        tvMensaje     = findViewById(R.id.tvMensaje)
        etMotivo      = findViewById(R.id.etMotivo)
        etFechaInicio = findViewById(R.id.etFechaInicio)
        etFechaFinal  = findViewById(R.id.etFechaFinal)
        etContratoId  = findViewById(R.id.etContratoId)
        etDias        = findViewById(R.id.etDias)


        recycler = findViewById(R.id.RecyVacaciones)
        recycler.layoutManager = LinearLayoutManager(this)
        val vacaciones: List<VacacionesDto> = listOf() // o la que tengas
        adapter = VacacionesAdapter(vacaciones) { vacacion ->
            mostrarDialogoEditarVacaciones(vacacion)
        }

        recycler.adapter = adapter


        findViewById<Button>(R.id.btnCrearVacacion).setOnClickListener { crearVacacion(it) }
        findViewById<Button>(R.id.btnMostrarVacaciones).setOnClickListener { cargarVacaciones() }

        cargarVacaciones()
    }



    private fun isValidDate(date: String) =
        date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))


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

    private fun mostrarDialogoEditarVacaciones(vacacion: VacacionesDto) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_editar_vacaciones) // Crea este layout similar a tu de usuario
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        // Asigna los campos del layout a las variables
        val etMotivo = dialog.findViewById<EditText>(R.id.etMotivo)
        val etFechaInicio = dialog.findViewById<EditText>(R.id.etFechaInicio)
        val etFechaFinal = dialog.findViewById<EditText>(R.id.etFechaFinal)
        val etDias = dialog.findViewById<EditText>(R.id.etDias)
        val etEstado = dialog.findViewById<EditText>(R.id.etEstado)
        val etContratoId = dialog.findViewById<EditText>(R.id.etContratoId)

        // Rellena los campos con los datos existentes
        etMotivo.setText(vacacion.motivo)
        etFechaInicio.setText(vacacion.fechaInicio)
        etFechaFinal.setText(vacacion.fechaFinal)
        etDias.setText(vacacion.dias?.toString() ?: "")
        etEstado.setText(vacacion.estado)
        etContratoId.setText(vacacion.contratoId.toString())

        // Botón guardar cambios
        dialog.findViewById<Button>(R.id.btnGuardarCambios).setOnClickListener {
            // Aquí obtienes los nuevos valores y llamas a la API para actualizar
            val nuevoMotivo = etMotivo.text.toString()
            val nuevaFechaInicio = etFechaInicio.text.toString()
            val nuevaFechaFinal = etFechaFinal.text.toString()
            val nuevosDias = etDias.text.toString().toIntOrNull()
            val nuevoEstado = etEstado.text.toString()
            val nuevoContratoId = etContratoId.text.toString().toIntOrNull()

            // Validaciones básicas
            if (nuevaFechaInicio.isEmpty() || nuevaFechaFinal.isEmpty() || nuevoEstado.isEmpty() || nuevoContratoId == null) {
                Toast.makeText(this, "Por favor complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val vacacionActualizada = vacacion.copy(
                motivo = nuevoMotivo,
                fechaInicio = nuevaFechaInicio,
                fechaFinal = nuevaFechaFinal,
                dias = nuevosDias,
                estado = nuevoEstado,
                contratoId = nuevoContratoId
            )

            actualizarVacacionEnApi(vacacionActualizada)
            dialog.dismiss()
        }


        dialog.findViewById<Button>(R.id.btnEliminarVacacion).setOnClickListener {
            dialog.dismiss()
            mostrarDialogoConfirmarEliminacion(vacacion)
        }


        dialog.findViewById<Button>(R.id.btnCancelar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarDialogoConfirmarEliminacion(vacacion: VacacionesDto) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_confirmar_eliminar_vacaciones)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val tvMensaje = dialog.findViewById<TextView>(R.id.tvMensajeConfirmacion)
        tvMensaje.text = "¿Estás seguro de que deseas eliminar las vacaciones del contrato: ${vacacion.contratoId}?\n\nEsta acción no se puede deshacer."

        dialog.findViewById<Button>(R.id.btnCancelarEliminar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnConfirmarEliminar).setOnClickListener {

            vacacion.idVacaciones?.let { id ->
                eliminarVacacionEnApi(id)
            } ?: run {
                Toast.makeText(this, "ID de vacación no válido", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun actualizarVacacionEnApi(vacacion: VacacionesDto) {
        RetrofitInstance.api2kotlin.actualizarVacacion(vacacion.idVacaciones ?: 0, vacacion).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@VacacionesActivity, "Vacación actualizada", Toast.LENGTH_SHORT).show()
                    cargarVacaciones()
                } else {
                    Toast.makeText(this@VacacionesActivity, "Error en actualización", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@VacacionesActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun eliminarVacacionEnApi(id: Long) {
        RetrofitInstance.api2kotlin.eliminarVacacion(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@VacacionesActivity, "Vacaciones eliminadas", Toast.LENGTH_SHORT).show()
                    cargarVacaciones()
                } else {
                    Toast.makeText(this@VacacionesActivity, "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@VacacionesActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
