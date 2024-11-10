package com.euneiz.euneizchef.categories

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.euneiz.euneizchef.ApiService
import com.euneiz.euneizchef.RecipesResponse
import com.euneiz.euneizchef.database.RecipeDatabase
import com.euneiz.euneizchef.databinding.ActivitySearchResultsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchResultsBinding
    private lateinit var recipesAdapter: RecipesAdapter
    private val apiService = ApiService.create()

    private lateinit var db: RecipeDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar la base de datos Room
        db = RecipeDatabase.getDatabase(this)
        val favoriteDao = db.favoriteDao()  // Obtener el DAO

        // Configurar el RecyclerView
        binding.recipesRecyclerView.layoutManager = LinearLayoutManager(this)
        recipesAdapter = RecipesAdapter(favoriteDao)
        binding.recipesRecyclerView.adapter = recipesAdapter

        // Obtener el término de búsqueda inicial desde el Intent
        val initialQuery = intent.getStringExtra("query")
        initialQuery?.let {
            binding.searchEditText.setText(it) // Mostrar el término inicial en el campo de búsqueda
            searchRecipes(it)
        }

        // Configurar el botón de búsqueda en SearchResultsActivity
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotEmpty()) {
                searchRecipes(query)
            } else {
                Toast.makeText(this, "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchRecipes(query: String) {
        apiService.searchRecipesByName(query).enqueue(object : Callback<RecipesResponse> {
            override fun onResponse(call: Call<RecipesResponse>, response: Response<RecipesResponse>) {
                if (response.isSuccessful) {
                    val recipes = response.body()?.meals
                    if (!recipes.isNullOrEmpty()) {
                        recipesAdapter.submitList(recipes)
                    } else {
                        Toast.makeText(this@SearchResultsActivity, "No se encontraron recetas", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SearchResultsActivity, "Error en la búsqueda", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecipesResponse>, t: Throwable) {
                Toast.makeText(this@SearchResultsActivity, "Error en la conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }
}