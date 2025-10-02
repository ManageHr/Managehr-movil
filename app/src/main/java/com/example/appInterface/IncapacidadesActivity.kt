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
import com.example.appinterface.Adapter.HojaDeVidaAdapter
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

    private lateinit var tvMensaje: TextView
    private lateinit var tvArchivoSeleccionado: TextView
    private lateinit var etFechaInicio: EditText
    private lateinit var etFechaFinal: EditText
    private lateinit var etContratoId: EditText
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: IncapacidadesAdapter
    private lateinit var btnSeleccionarArchivo: Button


    private var archivoUri: Uri? = null


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


        tvMensaje = findViewById(R.id.tvMensaje)
        tvArchivoSeleccionado = findViewById(R.id.tvArchivoSeleccionado)
        etFechaInicio = findViewById(R.id.etFechaInicio)
        etFechaFinal = findViewById(R.id.etFechaFinal)
        etContratoId = findViewById(R.id.etContratoId)
        btnSeleccionarArchivo = findViewById(R.id.btnSeleccionarArchivo)


        btnSeleccionarArchivo.setOnClickListener {
            pickFileLauncher.launch("*/*") // acepta cualquier archivo
        }


        recycler = findViewById(R.id.RecyIncapacidades)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = IncapacidadesAdapter(emptyList()) { incapacidad ->
            mostrarDialogoEditarIncapacidad(incapacidad)
        }
        recycler.adapter = adapter

        findViewById<Button>(R.id.btnCrearIncapacidad).setOnClickListener { crearIncapacidad(it) }
        findViewById<Button>(R.id.btnMostrarIncapacidades).setOnClickListener { cargarIncapacidades() }

        cargarIncapacidades()
    }


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
    private fun mostrarDialogoEditarIncapacidad(incapacidad: IncapacidadesDto) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_editar_incapacidades)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        // Referencias a los campos
        val etArchivo = dialog.findViewById<EditText>(R.id.etArchivo)
        val etEstado = dialog.findViewById<EditText>(R.id.etEstado)
        val etFechaInicio = dialog.findViewById<EditText>(R.id.etFechaInicio)
        val etFechaFinal = dialog.findViewById<EditText>(R.id.etFechaFinal)
        val etContratoId = dialog.findViewById<EditText>(R.id.etContratoId)

        // Rellenar con datos existentes
        etArchivo.setText(incapacidad.archivo)
        etEstado.setText(incapacidad.estado.toString())
        etFechaInicio.setText(incapacidad.fechaInicio)
        etFechaFinal.setText(incapacidad.fechaFinal)
        etContratoId.setText(incapacidad.contratoId.toString())

        // Botón guardar cambios
        dialog.findViewById<Button>(R.id.btnGuardarCambios).setOnClickListener {
            val nuevoArchivo = etArchivo.text.toString()
            val nuevoEstado = etEstado.text.toString().toIntOrNull()
            val nuevaFechaInicio = etFechaInicio.text.toString()
            val nuevaFechaFinal = etFechaFinal.text.toString()
            val nuevoContratoId = etContratoId.text.toString().toLongOrNull()

            // Validaciones
            if (nuevoArchivo.isEmpty() || nuevaFechaInicio.isEmpty() || nuevaFechaFinal.isEmpty() ||
                nuevoEstado == null || nuevoContratoId == null) {
                Toast.makeText(this, "Complete todos los campos correctamente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidDateFormat(nuevaFechaInicio) || !isValidDateFormat(nuevaFechaFinal)) {
                Toast.makeText(this, "Formato de fecha debe ser YYYY-MM-DD", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nuevaFechaFinal < nuevaFechaInicio) {
                Toast.makeText(this, "La fecha final no puede ser menor a la inicial", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nuevoEstado !in listOf(0, 1)) {
                Toast.makeText(this, "Estado debe ser 0 (inactivo) o 1 (activo)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val incapacidadActualizada = incapacidad.copy(
                archivo = nuevoArchivo,
                estado = nuevoEstado,
                fechaInicio = nuevaFechaInicio,
                fechaFinal = nuevaFechaFinal,
                contratoId = nuevoContratoId
            )

            actualizarIncapacidadEnApi(incapacidadActualizada)
            dialog.dismiss()
        }

        // Botón eliminar
        dialog.findViewById<Button>(R.id.btnEliminarIncapacidad).setOnClickListener {
            dialog.dismiss()
            mostrarDialogoConfirmarEliminacion(incapacidad)
        }

        // Botón cancelar
        dialog.findViewById<Button>(R.id.btnCancelar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarDialogoConfirmarEliminacion(incapacidad: IncapacidadesDto) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_confirmar_eliminar_incapacidades)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val tvMensaje = dialog.findViewById<TextView>(R.id.tvMensajeConfirmacion)
        tvMensaje.text = "¿Estás seguro de que deseas eliminar la incapacidad del contrato: ${incapacidad.contratoId}?\n\nArchivo: ${incapacidad.archivo}\nEsta acción no se puede deshacer."

        dialog.findViewById<Button>(R.id.btnCancelarEliminar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnConfirmarEliminar).setOnClickListener {
            incapacidad.idIncapacidad?.let { id ->
                eliminarIncapacidadEnApi(id)
            } ?: run {
                Toast.makeText(this, "ID de incapacidad no válido", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun actualizarIncapacidadEnApi(incapacidad: IncapacidadesDto) {
        incapacidad.idIncapacidad?.let { id ->
            RetrofitInstance.api2kotlin.actualizarIncapacidad(id, incapacidad).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@IncapacidadesActivity, "Incapacidad actualizada", Toast.LENGTH_SHORT).show()
                        cargarIncapacidades()
                    } else {
                        Toast.makeText(this@IncapacidadesActivity, "Error en actualización: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@IncapacidadesActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun eliminarIncapacidadEnApi(id: Long) {
        RetrofitInstance.api2kotlin.eliminarIncapacidad(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@IncapacidadesActivity, "Incapacidad eliminada", Toast.LENGTH_SHORT).show()
                    cargarIncapacidades()
                } else {
                    Toast.makeText(this@IncapacidadesActivity, "Error al eliminar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@IncapacidadesActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Función auxiliar para validar formato de fecha
    private fun isValidDateFormat(date: String): Boolean =
        date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
}
