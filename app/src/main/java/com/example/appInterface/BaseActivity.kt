package com.example.appinterface

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.appInterface.HojasvidaActivity
import com.google.android.material.navigation.NavigationView

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base) // layout con Drawer + Toolbar

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

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
//                R.id.nav_experiencia -> if (this !is ExperienciaActivity) startActivity(Intent(this, ExperienciaActivity::class.java))
                R.id.nav_hojasvida -> if (this !is HojasvidaActivity) startActivity(Intent(this, HojasvidaActivity::class.java))
//                R.id.nav_estudios    -> if (this !is EstudiosActivity) startActivity(Intent(this, EstudiosActivity::class.java))
                R.id.nav_horas_extra -> if (this !is HorasExtraActivity) startActivity(Intent(this, HorasExtraActivity::class.java))
//                R.id.nav_productos   -> if (this !is ProductosActivity) startActivity(Intent(this, ProductosActivity::class.java))
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    /** Inserta el layout propio del módulo dentro del contenedor del layout base */
    protected fun setModuleContent(layoutResId: Int) {
        val container = findViewById<android.view.ViewGroup>(R.id.content_container)
        LayoutInflater.from(this).inflate(layoutResId, container, true)
    }
}
