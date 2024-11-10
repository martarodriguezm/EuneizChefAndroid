package com.euneiz.euneizchef

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.euneiz.euneizchef.categories.AreaRecipesActivity
import com.euneiz.euneizchef.categories.CategoryRecipesActivity
import com.euneiz.euneizchef.categories.SearchResultsActivity
import com.euneiz.euneizchef.databinding.ActivityCategoriesBinding

class CategoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoriesBinding

    // Lista de categorías de la API
    private val categories = listOf(
        "Beef", "Chicken", "Dessert", "Lamb", "Miscellaneous", "Pasta", "Pork",
        "Seafood", "Side", "Starter", "Vegan", "Vegetarian", "Breakfast", "Goat"
    )
    // Lista de areas de la API
    private val areas = listOf(
        "American", "British", "Canadian", "Chinese", "Croatian", "Dutch", "Egyptian",
        "Filipino", "French", "Greek", "Indian", "Irish", "Italian", "Jamaican", "Japanese",
        "Kenyan", "Malaysian", "Mexican", "Moroccan", "Polish", "Portuguese", "Russian",
        "Spanish", "Thai", "Tunisian", "Turkish", "Ukrainian", "Vietnamese"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // Configurar el botón de búsqueda
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotEmpty()) {
                // Abrir SearchResultsActivity y pasar el término de búsqueda
                val intent = Intent(this, SearchResultsActivity::class.java).apply {
                    putExtra("query", query)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show()
            }
        }

        // Agregar un botón para cada categoría en el LinearLayout
        categories.forEach { category ->
            // Envolver el contexto con el estilo
            val themedContext = ContextThemeWrapper(this, R.style.CategoryButtonStyle)
            val button = Button(themedContext).apply {
                text = category
                setOnClickListener {
                    openCategoryRecipesActivity(category)
                }
            }
            binding.categoriesContainer.addView(button)
        }

        // Agregar un botón para cada area en el LinearLayout
        areas.forEach { area ->
            // Envolver el contexto con el estilo
            val themedContext = ContextThemeWrapper(this, R.style.AreaButtonStyle)
            val button = Button(themedContext).apply {
                text = area
                setOnClickListener {
                    openAreaRecipesActivity(area)
                }
            }
            binding.areasContainer.addView(button)
        }
    }

    // Función para abrir la actividad de recetas de la categoría seleccionada
    private fun openCategoryRecipesActivity(category: String) {
        val intent = Intent(this, CategoryRecipesActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }

    // Función para abrir la actividad de recetas de la area seleccionada
    private fun openAreaRecipesActivity(area: String) {
        val intent = Intent(this, AreaRecipesActivity::class.java)
        intent.putExtra("area", area)
        startActivity(intent)
    }
    override fun onResume() {
        super.onResume()
        // Forzar que el ítem "Categories" esté seleccionado cada vez que se vuelve a esta actividad
        binding.bottomNavigationView.selectedItemId = R.id.nav_categories
    }
}