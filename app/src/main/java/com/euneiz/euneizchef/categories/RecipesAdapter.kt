package com.euneiz.euneizchef.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.euneiz.euneizchef.R
import com.euneiz.euneizchef.databinding.ItemRecipeBinding

class RecipesAdapter : RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

    private val recipes = mutableListOf<Recipe>()

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
            // Usando Glide para cargar la imagen de la receta
            if (!recipe.strMealThumb.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(recipe.strMealThumb)
                    .placeholder(R.drawable.ic_placeholder_image) // Imagen por defecto
                    .into(binding.recipeImageView)
            } else {
                // Si no tiene imagen, cargar una imagen por defecto
                Glide.with(binding.root.context)
                    .load(R.drawable.ic_placeholder_image)
                    .into(binding.recipeImageView)
            }

            binding.favoriteButton.setOnClickListener {
                Toast.makeText(binding.root.context, "La receta se ha añadido a favoritos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
