package com.euneiz.euneizchef.categories

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.euneiz.euneizchef.ApiService
import com.euneiz.euneizchef.R
import com.euneiz.euneizchef.RecipeDetailsResponse
import com.euneiz.euneizchef.RecipesResponse
import com.euneiz.euneizchef.database.RecipeDatabase
import com.euneiz.euneizchef.databinding.ActivityCategoryRecipesBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryRecipesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryRecipesBinding
    private lateinit var recipesAdapter: RecipesAdapter
    private val apiService = ApiService.create()

    private lateinit var db: RecipeDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCategoryRecipesBinding.inflate(layoutInflater)
        setContentView(binding.main)

        // Cambiar color de fondo de la ActionBar
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)))
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar la base de datos Room
        db = RecipeDatabase.getDatabase(this)
        val favoriteDao = db.favoriteDao()  // Obtener el DAO

        // Configuración del RecyclerView
        binding.recipesRecyclerView.layoutManager = LinearLayoutManager(this)
        recipesAdapter = RecipesAdapter(favoriteDao)
        binding.recipesRecyclerView.adapter = recipesAdapter

        // Obtiener la categoría desde el Intent
        val category = intent.getStringExtra("category")
        category?.let { loadRecipes(it) }
    }

    private fun loadRecipes(category: String) {
        apiService.getRecipesByCategory(category).enqueue(object : Callback<RecipesResponse> {
            override fun onResponse(call: Call<RecipesResponse>, response: Response<RecipesResponse>) {
                if (response.isSuccessful) {
                    val recipes = response.body()?.meals
                    recipes?.forEach { recipe ->
                        // Cargar detalles de cada receta
                        loadRecipeDetails(recipe.idMeal)
                    }
                } else {
                    Log.e("CategoryRecipesActivity", "Error en la respuesta de la API")
                }
            }

            override fun onFailure(call: Call<RecipesResponse>, t: Throwable) {
                Log.e("CategoryRecipesActivity", "Error al cargar recetas", t)
            }
        })
    }

    private fun loadRecipeDetails(recipeId: String) {
        apiService.getRecipeDetails(recipeId).enqueue(object : Callback<RecipeDetailsResponse> {
            override fun onResponse(call: Call<RecipeDetailsResponse>, response: Response<RecipeDetailsResponse>) {
                if (response.isSuccessful) {
                    val recipeDetails = response.body()?.meals?.firstOrNull()
                    recipeDetails?.let {
                        recipesAdapter.addRecipe(it)  // Agrega el detalle al adaptador
                    }
                } else {
                    Log.e("CategoryRecipesActivity", "Error en los detalles de la API")
                }
            }

            override fun onFailure(call: Call<RecipeDetailsResponse>, t: Throwable) {
                Log.e("CategoryRecipesActivity", "Error al cargar detalles de la receta", t)
            }
        })
    }
}
