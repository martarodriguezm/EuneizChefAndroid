package com.euneiz.euneizchef

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.euneiz.euneizchef.database.FavoriteDao
import com.euneiz.euneizchef.database.FavoriteRecipe
import com.euneiz.euneizchef.database.RecipeDatabase
import com.euneiz.euneizchef.databinding.ActivityMainBinding
import com.euneiz.euneizchef.databinding.ActivityRecipeDetailBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeDetailBinding
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var recipeDatabase: RecipeDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Ocultar la ActionBar
        supportActionBar?.hide()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa la base de datos y DAO
        recipeDatabase = RecipeDatabase.getDatabase(this)
        favoriteDao = recipeDatabase.favoriteDao()


    // Obtener el objeto Recipe del Intent
        val recipe = intent.getSerializableExtra("recipe") as? Recipe

        recipe?.let {
            // Configuración de la UI con los detalles de la receta
            binding.recipeTitle.text = it.strMeal
            binding.recipeCategory.text = "Category: ${it.strCategory}"
            binding.recipeArea.text = "Area: ${it.strArea}"
            binding.recipeInstructions.text = it.strInstructions ?: "No instructions available"

            // Cargar imagen con Glide
            Glide.with(this)
                .load(it.strMealThumb)
                .placeholder(R.drawable.ic_placeholder_image)
                .into(binding.recipeImage)

            // Cargar más detalles si están disponibles
            binding.recipeYoutubeLink.text = "YouTube: ${it.strYoutube ?: "N/A"}"

            // Si hay enlace de YouTube, mostrar el video en el WebView
            it.strYoutube?.let { youtubeUrl ->
                val videoId = youtubeUrl.split("v=")[1] // Obtén el ID del video de la URL
                loadYoutubeVideo(videoId)
            }

            // Configuración del botón de favoritos
            setupFavoriteButton(recipe)

            // Configuración de ingredientes y medidas
            val ingredients = extractIngredients(it)
            if (ingredients.isNotEmpty()) {
                loadIngredients(ingredients)
            }
        } ?: run {
            // Manejo del caso en que la receta no se haya pasado correctamente
            finish() // Cierra la actividad si no se pudo cargar el objeto
        }

        // Configuración del Floating Action Button para regresar
        val fabBack: FloatingActionButton = binding.fabBack
        fabBack.setOnClickListener {
            onBackPressed()  // Esto manejará el regreso a la actividad anterior
        }
    }

    private fun setupFavoriteButton(recipe: Recipe) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isFavorite = favoriteDao.isFavorite(recipe.idMeal)
                withContext(Dispatchers.Main) {
                    binding.favoriteButton.isSelected = isFavorite
                }
            } catch (e: Exception) {
                Log.e("RecipeDetailActivity", "Error checking favorite status", e)
            }
        }

        binding.favoriteButton.setOnClickListener {
            it.isSelected = !it.isSelected
            val favoriteRecipe = FavoriteRecipe(
                idMeal = recipe.idMeal,
                strMeal = recipe.strMeal,
                strCategory = recipe.strCategory,
                strArea = recipe.strArea,
                strMealThumb = recipe.strMealThumb
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (it.isSelected) {
                        favoriteDao.insertFavorite(favoriteRecipe)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(binding.root.context, "La receta se ha añadido a favoritos", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        favoriteDao.deleteFavorite(favoriteRecipe)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(binding.root.context, "La receta se ha eliminado de favoritos", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RecipeDetailActivity", "Error updating favorite status", e)
                }
            }
        }
    }

    private fun extractIngredients(recipe: Recipe): List<Pair<String, String>> {
        val ingredients = mutableListOf<Pair<String, String>>()
        for (i in 1..20) {
            val ingredientField = recipe.javaClass.getDeclaredField("strIngredient$i")
            val measureField = recipe.javaClass.getDeclaredField("strMeasure$i")
            ingredientField.isAccessible = true
            measureField.isAccessible = true

            val ingredient = ingredientField.get(recipe) as? String
            val measure = measureField.get(recipe) as? String

            if (!ingredient.isNullOrEmpty() && !measure.isNullOrEmpty()) {
                ingredients.add(Pair(ingredient, measure))
            }
        }
        return ingredients
    }

    private fun loadIngredients(ingredients: List<Pair<String, String>>) {
        for ((ingredient, measure) in ingredients) {
            val ingredientView = layoutInflater.inflate(R.layout.ingredient_item, null)

            val ingredientImage: ImageView = ingredientView.findViewById(R.id.ingredientImage)
            val ingredientName: TextView = ingredientView.findViewById(R.id.ingredientName)


            // Cargar la imagen del ingrediente (puedes usar un recurso estático o un API para obtener la URL)
            Glide.with(this)
                .load("https://www.themealdb.com/images/ingredients/$ingredient.png")
                .placeholder(R.drawable.ic_placeholder_image)
                .into(ingredientImage)

            ingredientName.text = "$measure $ingredient"

            binding.ingredientsContainer.addView(ingredientView)
        }
    }

    private fun loadYoutubeVideo(videoId: String) {
        val youtubeWebView: WebView = binding.youtubeWebView

        // Configuración del WebView
        youtubeWebView.settings.javaScriptEnabled = true
        youtubeWebView.settings.pluginState = WebSettings.PluginState.ON
        youtubeWebView.webViewClient = WebViewClient()

        // Formato de URL para incrustar el video
        val embedUrl = "https://www.youtube.com/embed/$videoId"
        youtubeWebView.loadUrl(embedUrl)

        // Muestra el WebView
        youtubeWebView.visibility = View.VISIBLE
    }
}