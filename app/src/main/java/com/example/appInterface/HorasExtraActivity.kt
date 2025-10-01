package com.example.appinterface

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Adapter.HorasExtraAdapter
import com.example.appinterface.Api.RetrofitInstance
import com.example.appinterface.Models.HorasExtraDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.app.AlertDialog
import com.example.appinterface.BaseActivity
import com.example.appinterface.R


class HorasExtraActivity : BaseActivity() {
    override val selfMenuItemId: Int = R.id.nav_horas_extra

    // --------- Campos de UI ---------
    private lateinit var tvMensaje: TextView
    private lateinit var spinnerTipoHora: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HorasExtraAdapter

    // --------- Ciclo de vida ---------
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Usa el contenedor del Drawer definido en BaseActivity
        setModuleContent(R.layout.horas_extra_main)

        // Manejo de insets si el root del layout es @id/main
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ====== Referencias UI ======
        tvMensaje = findViewById(R.id.tvMensaje)
        spinnerTipoHora = findViewById(R.id.spinnerTipoHora)
        setupSpinnerTipos()

        recyclerView = findViewById(R.id.RecyHorasExtra)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HorasExtraAdapter { horasExtra ->
            mostrarDialogoEditarHorasExtra(horasExtra)
        }
        recyclerView.adapter = adapter

        val btnCrear = findViewById<Button>(R.id.btnCrearHoras)
        val btnMostrar = findViewById<Button>(R.id.btnMostrarHoras)

        btnCrear.setOnClickListener {
            Log.d("HorasExtraActivity", "Botón Crear clickeado")
            crearHorasExtra(it)
        }
        btnMostrar.setOnClickListener {
            Log.d("HorasExtraActivity", "Botón Mostrar clickeado")
            mostrarHorasExtra(it)
        }

        mostrarHorasExtra()
    }

    // ========= Lógica =========

    /** Llena el spinner con labels “<id> - <nombre>” */
    private fun setupSpinnerTipos() {
        val opcionesTipoHora = listOf(
            "2 - Diurna",
            "3 - Nocturna",
            "4 - Dominical/Festivo"
        )
        val adapterSpinner = ArrayAdapter(this, R.layout.item_spinner, opcionesTipoHora)
        adapterSpinner.setDropDownViewResource(R.layout.item_spinner_dropdown)
        spinnerTipoHora.adapter = adapterSpinner
    }

    /** Extrae el entero del label seleccionado: "2 - Nocturna" -> 2 */
    private fun selectedTipoHorasId(): Int? {
        val label = spinnerTipoHora.selectedItem as? String ?: return null
        val match = Regex("""^\s*(\d+)""").find(label) ?: return null
        return match.groupValues[1].toIntOrNull()
    }

    private fun isValidDateFormat(date: String): Boolean =
        date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))

    fun crearHorasExtra(view: View? = null) {
        val etDescripcion = findViewById<EditText>(R.id.etDescripcion)
        val etFecha = findViewById<EditText>(R.id.etFecha)
        val etCantidad = findViewById<EditText>(R.id.etCantidadHoras)
        val etEstado = findViewById<EditText>(R.id.etEstado)
        val etContratoId = findViewById<EditText>(R.id.etContratoId)

        if (etDescripcion.text.isNullOrBlank() ||
            etFecha.text.isNullOrBlank() ||
            etCantidad.text.isNullOrBlank() ||
            etEstado.text.isNullOrBlank() ||
            etContratoId.text.isNullOrBlank()
        ) {
            mostrarMensaje("Error: Todos los campos son obligatorios", true)
            return
        }

        val fecha = etFecha.text.toString()
        if (!isValidDateFormat(fecha)) {
            mostrarMensaje("Error: Formato debe ser YYYY-MM-DD", true)
            return
        }

        val tipoHorasId = selectedTipoHorasId()
        if (tipoHorasId == null) {
            mostrarMensaje("Selecciona un tipo de hora válido", true)
            return
        }

        val nHoras = etCantidad.text.toString().toIntOrNull()
        val estado = etEstado.text.toString().toIntOrNull()
        val contratoId = etContratoId.text.toString().toIntOrNull()

        if (nHoras == null || estado == null || contratoId == null) {
            mostrarMensaje("Error: Cantidad/Estado/Contrato deben ser numéricos", true)
            return
        }
        if (nHoras !in 1..24) {
            mostrarMensaje("Error: La cantidad de horas debe estar entre 1 y 24", true)
            return
        }
        if (estado !in listOf(0, 1)) {
            mostrarMensaje("Error: Estado debe ser 0 (inactivo) o 1 (activo)", true)
            return
        }

        val dto = HorasExtraDto(
            idHorasExtra = null,
            descripcion = etDescripcion.text.toString().trim(),
            fecha = fecha,
            tipoHorasId = tipoHorasId,   // entero 1/2/3
            nHorasExtra = nHoras,
            estado = estado,
            contratoId = contratoId
        )

        RetrofitInstance.api2kotlin.crearHorasExtra(dto).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("HorasExtraActivity", "crearHorasExtra -> code=${response.code()}")
                if (response.isSuccessful) {
                    AlertDialog.Builder(this@HorasExtraActivity)
                        .setTitle("¡Listo!")
                        .setMessage("El registro fue creado correctamente.")
                        .setPositiveButton("OK", null)
                        .show()

                    mostrarMensaje("✓ Registro creado exitosamente", false)
                    limpiarCampos()
                    mostrarHorasExtra()
                }else {
                    val msg = when (response.code()) {
                        400 -> "Error: Datos inválidos"
                        409 -> "Error: El registro ya existe"
                        500 -> "Error del servidor"
                        else -> "Error al crear: ${response.code()}"
                    }
                    mostrarMensaje(msg, true)
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                mostrarMensaje("✗ Error de conexión al crear", true)
                Log.e("HorasExtraActivity", "crearHorasExtra onFailure", t)
            }
        })
    }

    fun mostrarHorasExtra(view: View? = null) {
        RetrofitInstance.api2kotlin.obtenerHorasExtra().enqueue(object : Callback<List<HorasExtraDto>> {
            override fun onResponse(
                call: Call<List<HorasExtraDto>>,
                response: Response<List<HorasExtraDto>>
            ) {
                Log.d("HorasExtraActivity", "obtenerHorasExtra -> code=${response.code()}")
                if (response.isSuccessful) {
                    val data = response.body().orEmpty()
                    adapter.submitList(data)
                    mostrarMensaje(
                        if (data.isEmpty()) "No hay horas extra registradas"
                        else "${data.size} registros cargados",
                        false
                    )
                } else {
                    mostrarMensaje("Error al cargar: ${response.code()}", true)
                }
            }
            override fun onFailure(call: Call<List<HorasExtraDto>>, t: Throwable) {
                mostrarMensaje("Error de conexión al cargar", true)
                Log.e("HorasExtraActivity", "obtenerHorasExtra onFailure", t)
            }
        })
    }

    private fun mostrarMensaje(mensaje: String, esError: Boolean) {
        tvMensaje.text = mensaje
        tvMensaje.setTextColor(if (esError) 0xFFFF0000.toInt() else 0xFF00FF00.toInt())
    }

    private fun limpiarCampos() {
        findViewById<EditText>(R.id.etDescripcion).text?.clear()
        findViewById<EditText>(R.id.etFecha).text?.clear()
        findViewById<EditText>(R.id.etCantidadHoras).text?.clear()
        findViewById<EditText>(R.id.etEstado).text?.clear()
        findViewById<EditText>(R.id.etContratoId).text?.clear()
        spinnerTipoHora.setSelection(0)
    }
    private fun mostrarDialogoEditarHorasExtra(horasExtra: HorasExtraDto) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_editar_horasextra)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        // Referencias a los campos del diálogo
        val etDescripcion = dialog.findViewById<EditText>(R.id.etDescripcion)
        val etFecha = dialog.findViewById<EditText>(R.id.etFecha)
        val etCantidadHoras = dialog.findViewById<EditText>(R.id.etCantidadHoras)
        val etEstado = dialog.findViewById<EditText>(R.id.etEstado)
        val etContratoId = dialog.findViewById<EditText>(R.id.etContratoId)

        // Rellenar con datos existentes
        etDescripcion.setText(horasExtra.descripcion)
        etFecha.setText(horasExtra.fecha)
        etCantidadHoras.setText(horasExtra.nHorasExtra.toString())
        etEstado.setText(horasExtra.estado.toString())
        etContratoId.setText(horasExtra.contratoId.toString())

        // Botón guardar cambios
        dialog.findViewById<Button>(R.id.btnGuardarCambios).setOnClickListener {
            val nuevaDescripcion = etDescripcion.text.toString()
            val nuevaFecha = etFecha.text.toString()
            val nuevaCantidad = etCantidadHoras.text.toString().toIntOrNull()
            val nuevoEstado = etEstado.text.toString().toIntOrNull()
            val nuevoContratoId = etContratoId.text.toString().toIntOrNull()

            if (nuevaDescripcion.isEmpty() || nuevaFecha.isEmpty() || nuevaCantidad == null ||
                nuevoEstado == null || nuevoContratoId == null) {
                Toast.makeText(this, "Complete todos los campos correctamente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidDateFormat(nuevaFecha)) {
                Toast.makeText(this, "Formato de fecha debe ser YYYY-MM-DD", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val horasExtraActualizada = horasExtra.copy(
                descripcion = nuevaDescripcion,
                fecha = nuevaFecha,
                nHorasExtra = nuevaCantidad,
                estado = nuevoEstado,
                contratoId = nuevoContratoId
            )

            actualizarHorasExtraEnApi(horasExtraActualizada)
            dialog.dismiss()
        }

        // Botón eliminar
        dialog.findViewById<Button>(R.id.btnEliminarHorasExtra).setOnClickListener {
            dialog.dismiss()
            mostrarDialogoConfirmarEliminacion(horasExtra)
        }

        // Botón cancelar
        dialog.findViewById<Button>(R.id.btnCancelar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarDialogoConfirmarEliminacion(horasExtra: HorasExtraDto) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirmar_eliminar_horasextra)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val tvMensaje = dialog.findViewById<TextView>(R.id.tvMensajeConfirmacion)
        tvMensaje.text = "¿Estás seguro de que deseas eliminar las horas extra del contrato: ${horasExtra.contratoId}?\n\nFecha: ${horasExtra.fecha}\nHoras: ${horasExtra.nHorasExtra}"

        dialog.findViewById<Button>(R.id.btnCancelarEliminar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnConfirmarEliminar).setOnClickListener {
            horasExtra.idHorasExtra?.let { id ->
                eliminarHorasExtraEnApi(id)
            } ?: run {
                Toast.makeText(this, "ID de horas extra no válido", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun actualizarHorasExtraEnApi(horasExtra: HorasExtraDto) {
        horasExtra.idHorasExtra?.let { id ->
            RetrofitInstance.api2kotlin.actualizarHorasExtra(id, horasExtra).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@HorasExtraActivity, "Horas extra actualizadas", Toast.LENGTH_SHORT).show()
                        mostrarHorasExtra()
                    } else {
                        Toast.makeText(this@HorasExtraActivity, "Error en actualización: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@HorasExtraActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun eliminarHorasExtraEnApi(id: Long) {
        RetrofitInstance.api2kotlin.eliminarHorasExtra(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@HorasExtraActivity, "Horas extra eliminadas", Toast.LENGTH_SHORT).show()
                    mostrarHorasExtra()
                } else {
                    Toast.makeText(this@HorasExtraActivity, "Error al eliminar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@HorasExtraActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
