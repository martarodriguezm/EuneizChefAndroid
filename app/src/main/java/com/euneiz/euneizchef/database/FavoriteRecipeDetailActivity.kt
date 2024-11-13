package com.euneiz.euneizchef.database

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.euneiz.euneizchef.R
import com.euneiz.euneizchef.Recipe
import com.euneiz.euneizchef.databinding.ActivityFavoriteRecipeDetailBinding
import com.euneiz.euneizchef.databinding.ActivityRecipeDetailBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteRecipeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteRecipeDetailBinding
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var recipeDatabase: RecipeDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFavoriteRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Cambiar color de fondo de la ActionBar
        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    this,
                    R.color.colorPrimary
                )
            )
        )

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
            binding.recipeTitle.text = it.strMeal
            binding.recipeCategory.text = "Category: ${it.strCategory}"
            binding.recipeArea.text = "Area: ${it.strArea}"
            binding.recipeInstructions.text = it.strInstructions ?: "No instructions available"

            Glide.with(this)
                .load(it.strMealThumb)
                .placeholder(R.drawable.ic_placeholder_image)
                .into(binding.recipeImage)

            }
        }
    }