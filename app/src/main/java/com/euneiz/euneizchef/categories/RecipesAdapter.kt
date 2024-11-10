package com.euneiz.euneizchef.categories

import com.euneiz.euneizchef.database.FavoriteRecipe
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.euneiz.euneizchef.R
import com.euneiz.euneizchef.database.DatabaseProvider
import com.euneiz.euneizchef.database.Recipe
import com.euneiz.euneizchef.databinding.ItemRecipeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipesAdapter : RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

    private val recipes = mutableListOf<Recipe>()

    fun addRecipe(recipe: Recipe) {
        recipes.add(recipe)
        notifyItemInserted(recipes.size - 1)
    }

    fun submitList(newRecipes: List<Recipe>) {
        recipes.clear()
        recipes.addAll(newRecipes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.bind(recipe)
    }

    override fun getItemCount(): Int = recipes.size

    inner class RecipeViewHolder(private val binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.recipeTitleTextView.text = recipe.strMeal
            binding.recipeAreaTextView.text = recipe.strArea
            binding.recipeCategoryTextView.text = recipe.strCategory

            // Usando Glide para cargar la imagen de la receta
            Glide.with(binding.root.context)
                .load(recipe.strMealThumb ?: R.drawable.ic_placeholder_image)
                .placeholder(R.drawable.ic_placeholder_image)
                .into(binding.recipeImageView)

            // Configurar el listener del botón de favoritos
            binding.favoriteButton.isSelected = false
            binding.favoriteButton.setOnClickListener {
                toggleFavorite(recipe)
            }
        }

        private fun toggleFavorite(recipe: Recipe) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dao = DatabaseProvider.getDatabase(binding.root.context).favoriteRecipeDao()
                    val existingFavorite = dao.getFavoriteById(recipe.id)

                    // Log para verificar si el estado de favoritos se ha encontrado
                    Log.d("Favorites", "Toggle Favorite: ${recipe.id}, Exists: ${existingFavorite != null}")

                    if (existingFavorite != null) {
                        dao.removeFavorite(existingFavorite)
                        Log.d("Favorites", "Removed from favorites: ${recipe.id}")
                        // Actualizar el botón en el contexto de Dispatchers.Main
                        binding.root.post {
                            binding.favoriteButton.isSelected = false
                            Toast.makeText(
                                binding.root.context,
                                "La receta se ha eliminado de favoritos",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        dao.addFavorite(
                            FavoriteRecipe(
                                recipe.id, recipe.strMeal,
                                recipe.strDescription ?: ""
                            )
                        )
                        Log.d("Favorites", "Added to favorites: ${recipe.id}")
                        binding.root.post {
                            binding.favoriteButton.isSelected = true
                            Toast.makeText(
                                binding.root.context,
                                "La receta se ha añadido a favoritos",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Favorites", "Error en toggleFavorite", e)
                    binding.root.post {
                        Toast.makeText(
                            binding.root.context,
                            "Error al actualizar favoritos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}
