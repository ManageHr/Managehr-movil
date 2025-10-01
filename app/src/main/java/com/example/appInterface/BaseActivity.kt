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

        navigationView.menu.setGroupCheckable(0, true, true)
        navigationView.setCheckedItem(selfMenuItemId)

        navigationView.setNavigationItemSelectedListener { item ->
            if (item.itemId != R.id.nav_logout) item.isChecked = true
            drawerLayout.closeDrawers()

            when (item.itemId) {
                R.id.nav_hojasvida     -> navigateIfNeeded(HojasvidaActivity::class.java, R.id.nav_hojasvida)
                R.id.nav_horas_extra   -> navigateIfNeeded(HorasExtraActivity::class.java, R.id.nav_horas_extra)
                R.id.nav_incapacidades -> navigateIfNeeded(IncapacidadesActivity::class.java, R.id.nav_incapacidades)
                R.id.nav_vacaciones    -> navigateIfNeeded(VacacionesActivity::class.java, R.id.nav_vacaciones)
                R.id.nav_usuarios      -> navigateIfNeeded(UsuariosActivity::class.java, R.id.nav_usuarios)
                R.id.nav_logout        -> { /* se maneja abajo */ }
            }
            true
        }

        navigationView.menu.findItem(R.id.nav_logout)?.actionView
            ?.findViewById<View>(R.id.btnLogout)
            ?.setOnClickListener {
                val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                prefs.edit().remove("TOKEN").apply()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
    }

    override fun onResume() {
        super.onResume()
        navigationView.setCheckedItem(selfMenuItemId)
    }

    protected fun setModuleContent(layoutResId: Int) {
        val container = findViewById<android.view.ViewGroup>(R.id.content_container)
        LayoutInflater.from(this).inflate(layoutResId, container, true)
    }

    private fun <T> navigateIfNeeded(target: Class<T>, menuId: Int) {
        if (this::class.java != target) {
            val intent = Intent(this, target)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        } else {
            navigationView.setCheckedItem(menuId)
        }
    }
}
