package com.example.appinterface

import android.annotation.SuppressLint
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
import com.example.appInterface.Models.UsuarioDto
import com.example.appinterface.Adapter.UsuarioAdapter
import com.example.appinterface.Api.RetrofitInstance
import com.example.appinterface.Models.UserDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UsuariosActivity : BaseActivity() {
    override val selfMenuItemId: Int = R.id.nav_usuarios
    private lateinit var adapter: UsuarioAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvMensaje: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setModuleContent(R.layout.activity_usuarios)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        setupClickListeners()
        mostrarUsuarios()
    }

    private fun initViews() {
        tvMensaje = findViewById(R.id.tvMensaje)
        recyclerView = findViewById(R.id.RecyUsuarios)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UsuarioAdapter { usuario:UsuarioDto ->
            mostrarDialogoEditarUsuario(usuario)
        }
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        val btnCrear = findViewById<Button>(R.id.btnCrearUsuario)
        val btnMostrar = findViewById<Button>(R.id.btnMostrarUsuarios)

        btnCrear.setOnClickListener {
            Log.d("UsuariosActivity", "Boton Crear clickeado")
            crearUsuario(it)
        }

        btnMostrar.setOnClickListener {
            Log.d("UsuariosActivity", "Boton Mostrar clickeado")
            mostrarUsuarios(it)
        }
    }

    fun crearUsuario(v: View) {
        val etNumDocumento = findViewById<EditText>(R.id.etNumDocumento)
        val etPrimerNombre = findViewById<EditText>(R.id.etPrimerNombre)
        val etSegundoNombre = findViewById<EditText>(R.id.etSegundoNombre)
        val etPrimerApellido = findViewById<EditText>(R.id.etPrimerApellido)
        val etSegundoApellido = findViewById<EditText>(R.id.etSegundoApellido)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etFechaNac = findViewById<EditText>(R.id.etFechaNac)
        val etNumHijos = findViewById<EditText>(R.id.etNumHijos)
        val etContactoEmergencia = findViewById<EditText>(R.id.etContactoEmergencia)
        val etNumContactoEmergencia = findViewById<EditText>(R.id.etNumContactoEmergencia)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etDireccion = findViewById<EditText>(R.id.etDireccion)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)

        if (etNumDocumento.text.isNullOrEmpty() || etPrimerNombre.text.isNullOrEmpty() ||
            etPrimerApellido.text.isNullOrEmpty() || etPassword.text.isNullOrEmpty() ||
            etFechaNac.text.isNullOrEmpty() || etContactoEmergencia.text.isNullOrEmpty() ||
            etNumContactoEmergencia.text.isNullOrEmpty() || etEmail.text.isNullOrEmpty() ||
            etDireccion.text.isNullOrEmpty() || etTelefono.text.isNullOrEmpty()) {
            mostrarMensaje("Error: Campos con * son obligatorios", true)
            return
        }

        val fechaNac = etFechaNac.text.toString()
        if (!isValidDateFormat(fechaNac)) {
            mostrarMensaje("Error: Formato debe ser YYYY-MM-DD", true)
            return
        }

        crearUserYDespuesUsuario(
            numDocumento = etNumDocumento.text.toString().toLong(),
            primerNombre = etPrimerNombre.text.toString(),
            segundoNombre = etSegundoNombre.text.toString().takeIf { it.isNotEmpty() },
            primerApellido = etPrimerApellido.text.toString(),
            segundoApellido = etSegundoApellido.text.toString().takeIf { it.isNotEmpty() },
            password = etPassword.text.toString(),
            fechaNac = fechaNac,
            numHijos = etNumHijos.text.toString().toIntOrNull(),
            contactoEmergencia = etContactoEmergencia.text.toString(),
            numContactoEmergencia = etNumContactoEmergencia.text.toString(),
            email = etEmail.text.toString(),
            direccion = etDireccion.text.toString(),
            telefono = etTelefono.text.toString()
        )
    }

    private fun isValidDateFormat(date: String): Boolean {
        return date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
    }

    private fun crearUserYDespuesUsuario(
        numDocumento: Long,
        primerNombre: String,
        segundoNombre: String?,
        primerApellido: String,
        segundoApellido: String?,
        password: String,
        fechaNac: String,
        numHijos: Int?,
        contactoEmergencia: String,
        numContactoEmergencia: String,
        email: String,
        direccion: String,
        telefono: String
    ) {
        val nombreCompleto = "$primerNombre ${segundoNombre ?: ""} $primerApellido ${segundoApellido ?: ""}".trim()

        val userDto = UserDto(
            name = nombreCompleto,
            email = email,
            password = password,
            rol = 2,
            remember_token = null,
            created_at = null,
            updated_at = null
        )

        mostrarMensaje("Creando usuario del sistema...", false)
        Log.d("FLUJO_USUARIO", "Paso 1: Creando User en la base de datos")

        RetrofitInstance.api2kotlin.crearUser(userDto).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("FLUJO_USUARIO", "User creado exitosamente")
                    Log.d("FLUJO_USUARIO", "Paso 2: Obteniendo ID del User creado")

                    obtenerUserIdPorEmail(email) { usersId ->
                        if (usersId != null) {
                            Log.d("FLUJO_USUARIO", "ID del User obtenido: $usersId")
                            Log.d("FLUJO_USUARIO", "Paso 3: Creando Usuario con usersId: $usersId")

                            crearUsuarioConUserId(
                                numDocumento = numDocumento,
                                primerNombre = primerNombre,
                                segundoNombre = segundoNombre,
                                primerApellido = primerApellido,
                                segundoApellido = segundoApellido,
                                password = password,
                                fechaNac = fechaNac,
                                numHijos = numHijos,
                                contactoEmergencia = contactoEmergencia,
                                numContactoEmergencia = numContactoEmergencia,
                                email = email,
                                direccion = direccion,
                                telefono = telefono,
                                usersId = usersId
                            )
                        } else {
                            Log.e("FLUJO_USUARIO", "No se pudo obtener el ID del User")
                            mostrarMensaje("Error: No se pudo obtener el ID del usuario creado", true)
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("FLUJO_USUARIO", "Error al crear User: $errorBody")
                    mostrarMensaje("Error al crear usuario del sistema: $errorBody", true)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("FLUJO_USUARIO", "Error de conexion al crear User: ${t.message}")
                mostrarMensaje("Error de conexion al crear usuario del sistema", true)
            }
        })
    }

    private fun obtenerUserIdPorEmail(email: String, callback: (Long?) -> Unit) {
        Log.d("FLUJO_USUARIO", "Buscando User por email: $email")

        RetrofitInstance.api2kotlin.obtenerUserPorEmail(email).enqueue(object : Callback<UserDto> {
            override fun onResponse(call: Call<UserDto>, response: Response<UserDto>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        Log.d("FLUJO_USUARIO", "User encontrado - ID: ${user.id}")
                        callback(user.id)
                    } else {
                        Log.e("FLUJO_USUARIO", "User no encontrado en la respuesta")
                        callback(null)
                    }
                } else {
                    Log.e("FLUJO_USUARIO", "Error al obtener user por email: ${response.code()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<UserDto>, t: Throwable) {
                Log.e("FLUJO_USUARIO", "Falla al obtener user por email: ${t.message}")
                callback(null)
            }
        })
    }

    private fun crearUsuarioConUserId(
        numDocumento: Long,
        primerNombre: String,
        segundoNombre: String?,
        primerApellido: String,
        segundoApellido: String?,
        password: String,
        fechaNac: String,
        numHijos: Int?,
        contactoEmergencia: String,
        numContactoEmergencia: String,
        email: String,
        direccion: String,
        telefono: String,
        usersId: Long
    ) {
        Log.d("FLUJO_USUARIO", "Creando Usuario con usersId: $usersId")

        val usuarioDto = UsuarioDto(
            numDocumento = numDocumento,
            primerNombre = primerNombre,
            segundoNombre = segundoNombre,
            primerApellido = primerApellido,
            segundoApellido = segundoApellido,
            password = password,
            fechaNac = fechaNac,
            numHijos = numHijos,
            contactoEmergencia = contactoEmergencia,
            numContactoEmergencia = numContactoEmergencia,
            email = email,
            direccion = direccion,
            telefono = telefono,
            nacionalidadId = 1,
            epsCodigo = "EPS001",
            generoId = 1,
            tipoDocumentoId = 1,
            estadoCivilId = 1,
            pensionesCodigo = "231001",
            usersId = usersId
        )

        crearUsuarioEnApi(usuarioDto)
    }

    private fun crearUsuarioEnApi(usuario: UsuarioDto) {
        RetrofitInstance.api2kotlin.crearUsuario(usuario).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    mostrarMensaje("Usuario creado exitosamente", false)
                    limpiarCampos()
                    mostrarUsuarios()
                } else {
                    mostrarMensaje("Error al crear: ${response.code()}", true)
                    Log.e("API", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                mostrarMensaje("Error de conexion: ${t.message}", true)
                Log.e("API", "Falla de red", t)
            }
        })
    }

    fun mostrarUsuarios(v: View? = null) {
        println("UsuariosActivity: Solicitando usuarios...")

        RetrofitInstance.api2kotlin.obtenerUsuarios().enqueue(object : Callback<List<UsuarioDto>> {
            override fun onResponse(call: Call<List<UsuarioDto>>, response: Response<List<UsuarioDto>>) {
                println("UsuariosActivity: Respuesta recibida - Codigo: ${response.code()}")

                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    println("UsuariosActivity: ${data.size} usuarios recibidos")

                    data.forEachIndexed { index, usuario ->
                        println("Usuario $index: ${usuario.primerNombre} - ${usuario.email}")
                    }

                    adapter.submitList(data)

                    if (data.isEmpty()) {
                        mostrarMensaje("No hay usuarios registrados", false)
                    } else {
                        mostrarMensaje(" ${data.size} usuarios cargados", false)
                    }
                } else {
                    mostrarMensaje("Error al cargar: ${response.code()}", true)
                    println("UsuariosActivity: Error response - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<UsuarioDto>>, t: Throwable) {
                mostrarMensaje("Error de conexion al cargar", true)
                println("UsuariosActivity: Error de conexion - ${t.message}")
                t.printStackTrace()
            }
        })
    }

    private fun mostrarMensaje(mensaje: String, esError: Boolean) {
        tvMensaje.text = mensaje
        tvMensaje.setTextColor(if (esError) 0xFFFF0000.toInt() else 0xFF00FF00.toInt())
    }

    private fun mostrarDialogoEditarUsuario(usuario: UsuarioDto) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_editar_usuario)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val tvNumDocumento = dialog.findViewById<TextView>(R.id.tvEditNumDocumento)
        val etPrimerNombre = dialog.findViewById<EditText>(R.id.etEditPrimerNombre)
        val etSegundoNombre = dialog.findViewById<EditText>(R.id.etEditSegundoNombre)
        val etPrimerApellido = dialog.findViewById<EditText>(R.id.etEditPrimerApellido)
        val etSegundoApellido = dialog.findViewById<EditText>(R.id.etEditSegundoApellido)
        val etFechaNac = dialog.findViewById<EditText>(R.id.etEditFechaNac)
        val etNumHijos = dialog.findViewById<EditText>(R.id.etEditNumHijos)
        val etEmail = dialog.findViewById<EditText>(R.id.etEditEmail)
        val etTelefono = dialog.findViewById<EditText>(R.id.etEditTelefono)
        val etDireccion = dialog.findViewById<EditText>(R.id.etEditDireccion)
        val etContactoEmergencia = dialog.findViewById<EditText>(R.id.etEditContactoEmergencia)
        val etNumContactoEmergencia = dialog.findViewById<EditText>(R.id.etEditNumContactoEmergencia)

        tvNumDocumento.text = usuario.numDocumento.toString()
        etPrimerNombre.setText(usuario.primerNombre)
        etSegundoNombre.setText(usuario.segundoNombre ?: "")
        etPrimerApellido.setText(usuario.primerApellido)
        etSegundoApellido.setText(usuario.segundoApellido ?: "")
        etFechaNac.setText(usuario.fechaNac)
        etNumHijos.setText(usuario.numHijos?.toString() ?: "")
        etEmail.setText(usuario.email)
        etTelefono.setText(usuario.telefono)
        etDireccion.setText(usuario.direccion)
        etContactoEmergencia.setText(usuario.contactoEmergencia)
        etNumContactoEmergencia.setText(usuario.numContactoEmergencia)

        dialog.findViewById<Button>(R.id.btnGuardarUsuario).setOnClickListener {
            val primerNombre = etPrimerNombre.text.toString()
            val segundoNombre = etSegundoNombre.text.toString()
            val primerApellido = etPrimerApellido.text.toString()
            val segundoApellido = etSegundoApellido.text.toString()
            val fechaNac = etFechaNac.text.toString()
            val numHijos = etNumHijos.text.toString().toIntOrNull()
            val email = etEmail.text.toString()
            val telefono = etTelefono.text.toString()
            val direccion = etDireccion.text.toString()
            val contactoEmergencia = etContactoEmergencia.text.toString()
            val numContactoEmergencia = etNumContactoEmergencia.text.toString()

            if (primerNombre.isEmpty() || primerApellido.isEmpty() || fechaNac.isEmpty() ||
                email.isEmpty() || telefono.isEmpty() || direccion.isEmpty() ||
                contactoEmergencia.isEmpty() || numContactoEmergencia.isEmpty()) {
                Toast.makeText(this, "Campos con * son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidDateFormat(fechaNac)) {
                Toast.makeText(this, "Error: Formato debe ser YYYY-MM-DD", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usuarioActualizado = usuario.copy(
                primerNombre = primerNombre,
                segundoNombre = segundoNombre.takeIf { it.isNotEmpty() },
                primerApellido = primerApellido,
                segundoApellido = segundoApellido.takeIf { it.isNotEmpty() },
                fechaNac = fechaNac,
                numHijos = numHijos,
                email = email,
                telefono = telefono,
                direccion = direccion,
                contactoEmergencia = contactoEmergencia,
                numContactoEmergencia = numContactoEmergencia
            )

            actualizarUsuarioEnApi(usuarioActualizado)
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnEliminarUsuario).setOnClickListener {
            dialog.dismiss()
            mostrarDialogoConfirmarEliminacionUsuario(usuario)
        }

        dialog.findViewById<Button>(R.id.btnCancelarEditar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarDialogoConfirmarEliminacionUsuario(usuario: UsuarioDto) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_confirmar_eliminar_usuario)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val tvMensajeConfirmacion = dialog.findViewById<TextView>(R.id.tvMensajeConfirmacion)
        tvMensajeConfirmacion.text = "Estas seguro de que deseas eliminar al usuario: ${usuario.primerNombre} ${usuario.primerApellido}?\n\nEsta accion no se puede deshacer."

        dialog.findViewById<Button>(R.id.btnCancelarEliminar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnConfirmarEliminar).setOnClickListener {
            eliminarUsuarioEnApi(usuario.numDocumento)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun actualizarUsuarioEnApi(usuario: UsuarioDto) {
        RetrofitInstance.api2kotlin.actualizarUsuario(usuario.numDocumento, usuario).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("API_UPDATE", "Respuesta recibida - Codigo: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d("API_UPDATE", " Actualizacion exitosa en servidor")
                    mostrarMensaje("Usuario actualizado correctamente", false)
                    mostrarUsuarios()
                    Toast.makeText(this@UsuariosActivity, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("API_UPDATE", " Error en respuesta: ${response.code()}")
                    when (response.code()) {
                        404 -> mostrarMensaje("Error: Usuario no encontrado", true)
                        400 -> mostrarMensaje("Error: Datos invalidos", true)
                        else -> mostrarMensaje("Error al actualizar: ${response.code()}", true)
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("API_UPDATE", " Error de conexion: ${t.message}")
                mostrarMensaje("Error de conexion al actualizar", true)
                t.printStackTrace()
            }
        })
    }

    private fun eliminarUsuarioEnApi(numDocumento: Long) {
        RetrofitInstance.api2kotlin.eliminarUsuario(numDocumento).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    mostrarMensaje("Usuario eliminado correctamente", false)
                    mostrarUsuarios()
                    Toast.makeText(this@UsuariosActivity, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                } else {
                    when (response.code()) {
                        404 -> mostrarMensaje("Error: Usuario no encontrado", true)
                        400 -> mostrarMensaje("Error: No se pudo eliminar el usuario", true)
                        else -> mostrarMensaje("Error al eliminar: ${response.code()}", true)
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                mostrarMensaje("Error de conexion al eliminar", true)
            }
        })
    }

    private fun limpiarCampos() {
        findViewById<EditText>(R.id.etNumDocumento).text.clear()
        findViewById<EditText>(R.id.etPrimerNombre).text.clear()
        findViewById<EditText>(R.id.etSegundoNombre).text.clear()
        findViewById<EditText>(R.id.etPrimerApellido).text.clear()
        findViewById<EditText>(R.id.etSegundoApellido).text.clear()
        findViewById<EditText>(R.id.etPassword).text.clear()
        findViewById<EditText>(R.id.etFechaNac).text.clear()
        findViewById<EditText>(R.id.etNumHijos).text.clear()
        findViewById<EditText>(R.id.etContactoEmergencia).text.clear()
        findViewById<EditText>(R.id.etNumContactoEmergencia).text.clear()
        findViewById<EditText>(R.id.etEmail).text.clear()
        findViewById<EditText>(R.id.etDireccion).text.clear()
        findViewById<EditText>(R.id.etTelefono).text.clear()
    }
}