package com.euneiz.euneizchef.categories

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.euneiz.euneizchef.R
import com.euneiz.euneizchef.databinding.ActivityCategoryRecipesBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryRecipesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryRecipesBinding
    private lateinit var recipesAdapter: RecipesAdapter
    private val apiService = ApiService.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCategoryRecipesBinding.inflate(layoutInflater)
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

        // Obtiener la categoría desde el Intent
        val category = intent.getStringExtra("category")
        category?.let { loadRecipes(it) }
    }

    private fun loadRecipes(category: String) {
        apiService.getRecipesByCategory(category).enqueue(object : Callback<RecipesResponse> {
            override fun onResponse(call: Call<RecipesResponse>, response: Response<RecipesResponse>) {
                if (response.isSuccessful) {
                    val recipes = response.body()?.meals
                    if (recipes != null) {
                        recipesAdapter.submitList(recipes)
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
}