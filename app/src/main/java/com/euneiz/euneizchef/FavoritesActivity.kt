package com.euneiz.euneizchef

import com.euneiz.euneizchef.database.FavoriteRecipe
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.euneiz.euneizchef.database.DatabaseProvider
import com.euneiz.euneizchef.databinding.ActivityFavoritesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoritesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding=ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.main)

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
    }

    private fun testDatabaseOperations(recipeId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Accede a la base de datos usando `applicationContext`
                val dao = DatabaseProvider.getDatabase(applicationContext).favoriteRecipeDao()
                val favoriteRecipe = dao.getFavoriteById(recipeId)

                if (favoriteRecipe == null) {
                    // Añadir a favoritos
                    dao.addFavorite(FavoriteRecipe(recipeId, "Recipe Name", "Recipe Description"))
                    Log.d("DatabaseTest", "Receta añadida a favoritos.")
                } else {
                    // Eliminar de favoritos
                    dao.removeFavorite(favoriteRecipe)
                    Log.d("DatabaseTest", "Receta eliminada de favoritos.")
                }
            } catch (e: Exception) {
                Log.e("DatabaseTest", "Error en operaciones de prueba de la base de datos", e)
            }
        }
    }
}
