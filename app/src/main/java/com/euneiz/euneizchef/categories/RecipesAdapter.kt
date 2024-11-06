package com.euneiz.euneizchef.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
            // Cargar imagen de la receta (si usas Glide o Picasso)
            // Glide.with(binding.recipeImageView.context).load(recipe.imageUrl).into(binding.recipeImageView)
            binding.favoriteButton.setOnClickListener {
                // Lógica para añadir a favoritos
            }
        }
    }
}
