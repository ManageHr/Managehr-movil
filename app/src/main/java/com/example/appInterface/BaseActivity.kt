package com.example.appinterface

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    /** Cada sub-activity dirá cuál item del menú le corresponde (para resaltar) */
    protected abstract val selfMenuItemId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 🔹 Resaltar el item propio de la pantalla actual
        navigationView.menu.setGroupCheckable(0, true, true)
        navigationView.setCheckedItem(selfMenuItemId)

        // 🔹 Listener general de navegación
        navigationView.setNavigationItemSelectedListener { item ->
            // Evita marcar "Cerrar sesión"
            if (item.itemId != R.id.nav_logout) item.isChecked = true
            drawerLayout.closeDrawers()

            when (item.itemId) {
                R.id.nav_hojasvida      -> navigateIfNeeded(HojasvidaActivity::class.java, R.id.nav_hojasvida)
                R.id.nav_horas_extra    -> navigateIfNeeded(HorasExtraActivity::class.java, R.id.nav_horas_extra)
                R.id.nav_incapacidades  -> navigateIfNeeded(IncapacidadesActivity::class.java, R.id.nav_incapacidades)
                R.id.nav_vacaciones     -> navigateIfNeeded(VacacionesActivity::class.java, R.id.nav_vacaciones)
                // agrega aquí los demás módulos cuando los tengas
                R.id.nav_logout         -> { /* el click real del botón se cablea abajo */ }
            }
            true
        }

        // 🔹 Cablear el botón custom del item "Cerrar sesión" (actionLayout)
        navigationView.menu.findItem(R.id.nav_logout)?.actionView
            ?.findViewById<View>(R.id.btnLogout)
            ?.setOnClickListener {
                // 1) borrar token
                val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                prefs.edit().remove("TOKEN").apply()

                // 2) ir al login y limpiar back stack
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
    }

    override fun onResume() {
        super.onResume()
        // Reafirma el resaltado por si volviste desde otra pantalla
        navigationView.setCheckedItem(selfMenuItemId)
    }

    /** Inserta el layout propio del módulo dentro del contenedor del layout base */
    protected fun setModuleContent(layoutResId: Int) {
        val container = findViewById<android.view.ViewGroup>(R.id.content_container)
        LayoutInflater.from(this).inflate(layoutResId, container, true)
    }

    /** Navega solo si no estás ya en esa activity; además limpia duplicados en el stack. */
    private fun <T> navigateIfNeeded(target: Class<T>, menuId: Int) {
        if (this::class.java != target) {
            val intent = Intent(this, target)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            // opcional: no cierres la actual si quieres poder volver con back
            // finish()
        } else {
            // si ya estás en esa pantalla, solo re-marca el item
            navigationView.setCheckedItem(menuId)
        }
    }
}
