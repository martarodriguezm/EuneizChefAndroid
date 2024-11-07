package com.euneiz.euneizchef

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.euneiz.euneizchef.categories.CategoryRecipesActivity
import com.euneiz.euneizchef.databinding.ActivityCategoriesBinding

class CategoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoriesBinding

    // Lista de categorías de la API
    private val categories = listOf(
        "Beef", "Chicken", "Dessert", "Lamb", "Miscellaneous", "Pasta", "Pork",
        "Seafood", "Side", "Starter", "Vegan", "Vegetarian", "Breakfast", "Goat"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Marcar el ítem de categorías en el BottomNavigationView
        binding.bottomNavigationView.selectedItemId = R.id.nav_categories

        // Configurar el listener de navegación
        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    true
                }

                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    true
                }

                R.id.nav_categories -> true // Ya estamos en Categorías
                else -> false
            }
        }

        // Agregar un botón para cada categoría en el LinearLayout
        categories.forEach { category ->
            val button = Button(this).apply {
                text = category
                setOnClickListener {
                    openCategoryRecipesActivity(category)
                }
            }
            binding.categoriesContainer.addView(button)
        }
    }

    // Función para abrir la actividad de recetas de la categoría seleccionada
    private fun openCategoryRecipesActivity(category: String) {
        val intent = Intent(this, CategoryRecipesActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }
    override fun onResume() {
        super.onResume()
        // Forzar que el ítem "Categories" esté seleccionado cada vez que se vuelve a esta actividad
        binding.bottomNavigationView.selectedItemId = R.id.nav_categories
    }
}