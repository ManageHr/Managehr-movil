package com.example.appinterface


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appinterface.Api.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnOfertas: Button
    private lateinit var tvOlvidePassword: TextView

    data class LoginRequest(val email: String, val password: String)

    data class LoginResponse(
        val user: UserResponse,
        val token: String,
        val redirect: String
    )

    data class UserResponse(
        val id: Long,
        val name: String,
        val email: String,
        val password: String,
        val rol: String
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvOlvidePassword = findViewById(R.id.tvOlvidePassword)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            realizarLogin()
        }
        tvOlvidePassword.setOnClickListener {
            Toast.makeText(this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
        }
    }



    private fun realizarLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (validarCampos(email, password)) {
            val loginRequest = LoginRequest(email, password)

            btnLogin.isEnabled = false
            btnLogin.text = "Cargando..."

            RetrofitInstance.api2kotlin.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Iniciar Sesión"

                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        val token = loginResponse?.token ?: ""
                        val user = loginResponse?.user

                        if (token.isNotEmpty() && user != null) {
                            // VERIFICAR SI EL USUARIO TIENE ROL = "1" (ADMIN)
                            if (user.rol == "1") {
                                RetrofitInstance.setAuthToken(token)
                                saveAuthToken(token)

                                Toast.makeText(this@LoginActivity, "Login exitoso", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this@LoginActivity, UsuariosActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                // USUARIO NO TIENE PERMISOS DE ADMIN
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Acceso denegado: No tiene permisos para ingresar",
                                    Toast.LENGTH_LONG
                                ).show()

                                // SOLO ELIMINA ESTAS 2 LÍNEAS O COMENTA:
                                // RetrofitInstance.clearAuthToken()
                                // clearAuthToken()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Error: Token vacío o usuario no encontrado", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorMessage = when (response.code()) {
                            401 -> "Credenciales incorrectas"
                            403 -> "Acceso denegado"
                            404 -> "Servicio no encontrado"
                            else -> "Error: ${response.code()}"
                        }
                        Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Iniciar Sesión"

                    Toast.makeText(this@LoginActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LOGIN_ERROR", "Error: ${t.message}", t)
                }
            })
        } else {
            btnLogin.isEnabled = true
            btnLogin.text = "Iniciar Sesión"
        }
    }

    private fun saveAuthToken(token: String) {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("auth_token", token)
            apply()
        }
    }

    // Cargar token al iniciar la app
    private fun loadAuthToken() {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", "") ?: ""
        if (token.isNotEmpty()) {
            RetrofitInstance.setAuthToken(token)
        }
    }

    private fun validarCampos(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "Ingrese su correo electrónico"
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "Ingrese su contraseña"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Formato de email inválido"
            return false
        }

        return true
    }

}
