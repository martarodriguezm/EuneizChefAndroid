package com.euneiz.euneizchef.categories

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.euneiz.euneizchef.R
import com.euneiz.euneizchef.database.RecipeDetailsResponse
import com.euneiz.euneizchef.database.RecipesResponse
import com.euneiz.euneizchef.databinding.ActivityAreaRecipesBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AreaRecipesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAreaRecipesBinding
    private lateinit var recipesAdapter: RecipesAdapter
    private val apiService = ApiService.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAreaRecipesBinding.inflate(layoutInflater)
        setContentView(binding.main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configuración del RecyclerView
        binding.recipesRecyclerView.layoutManager = LinearLayoutManager(this)
        recipesAdapter = RecipesAdapter()
        binding.recipesRecyclerView.adapter = recipesAdapter

        // Obtiener la area desde el Intent
        val area = intent.getStringExtra("area")
        area?.let { loadRecipes(it) }
    }

    private fun loadRecipes(area: String) {
        apiService.getRecipesByArea(area).enqueue(object : Callback<RecipesResponse> {
            override fun onResponse(call: Call<RecipesResponse>, response: Response<RecipesResponse>) {
                if (response.isSuccessful) {
                    val recipes = response.body()?.meals
                    recipes?.forEach { recipe ->
                        // Cargar detalles de cada receta
                        loadRecipeDetails(recipe.idMeal)
                    }
                } else {
                    Log.e("AreaRecipesActivity", "Error en la respuesta de la API")
                }
            }

            override fun onFailure(call: Call<RecipesResponse>, t: Throwable) {
                Log.e("AreaRecipesActivity", "Error al cargar recetas", t)
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
                    Log.e("AreaRecipesActivity", "Error en los detalles de la API")
                }
            }

            override fun onFailure(call: Call<RecipeDetailsResponse>, t: Throwable) {
                Log.e("AreaRecipesActivity", "Error al cargar detalles de la receta", t)
            }
        })
    }
}