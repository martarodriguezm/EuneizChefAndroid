package com.euneiz.euneizchef

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.euneiz.euneizchef.database.FavoriteDao
import com.euneiz.euneizchef.database.FavoritesAdapter
import com.euneiz.euneizchef.database.RecipeDatabase
import com.euneiz.euneizchef.databinding.ActivityFavoritesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var db: RecipeDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding=ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Cambiar color de fondo de la ActionBar
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Marcar el ítem de favoritos en el BottomNavigationView
        binding.bottomNavigationView.selectedItemId = R.id.nav_favorites

        // Configurar el listener de navegación
        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    true
                }
                R.id.nav_favorites -> true // Ya estamos en Favoritos
                R.id.nav_categories -> {
                    startActivity(Intent(this, CategoriesActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    true
                }
                else -> false
            }
        }

        // Inicializar Room database
        db = RecipeDatabase.getDatabase(this)
        favoriteDao = db.favoriteDao()

        // Configurar RecyclerView
        binding.favoritesRecyclerView.layoutManager = GridLayoutManager(this,2)
        favoritesAdapter = FavoritesAdapter(favoriteDao)
        binding.favoritesRecyclerView.adapter = favoritesAdapter

        // Cargar recetas favoritas desde la base de datos
        loadFavoriteRecipes()
    }

    private fun loadFavoriteRecipes() {
        // Usar una corrutina para obtener las recetas favoritas desde la base de datos
        CoroutineScope(Dispatchers.IO).launch {
            val favoriteRecipes = db.favoriteDao().getAllFavorites()  // Obtener recetas favoritas
            CoroutineScope(Dispatchers.Main).launch {
                favoritesAdapter.submitList(favoriteRecipes)  // Pasar las recetas al adaptador
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Forzar que el ítem "Favorites" esté seleccionado cada vez que se vuelve a esta actividad
        binding.bottomNavigationView.selectedItemId = R.id.nav_favorites
    }
}
