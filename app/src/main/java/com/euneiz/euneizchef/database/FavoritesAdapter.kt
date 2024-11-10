package com.euneiz.euneizchef.database

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.euneiz.euneizchef.R
import com.euneiz.euneizchef.databinding.ItemRecipeBinding
import com.euneiz.euneizchef.databinding.ItemRecipeFavouriteBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesAdapter(
    private val favoriteDao: FavoriteDao  // Pasamos el DAO como parámetro
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    private val favoriteRecipes = mutableListOf<FavoriteRecipe>()

    // Función para actualizar la lista de recetas
    fun submitList(newFavoriteRecipes: List<FavoriteRecipe>) {
        favoriteRecipes.clear()
        favoriteRecipes.addAll(newFavoriteRecipes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemRecipeFavouriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val recipe = favoriteRecipes[position]
        holder.bind(recipe)
    }

    override fun getItemCount(): Int = favoriteRecipes.size

    inner class FavoriteViewHolder(private val binding: ItemRecipeFavouriteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: FavoriteRecipe) {
            // Vincular los datos de la receta a las vistas
            binding.recipeTitleTextView.text = recipe.strMeal
            binding.recipeAreaTextView.text = recipe.strArea
            binding.recipeCategoryTextView.text = recipe.strCategory

            // Cargar la imagen de la receta con Glide
            if (!recipe.strMealThumb.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(recipe.strMealThumb)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .into(binding.recipeImageView)
            } else {
                Glide.with(binding.root.context)
                    .load(R.drawable.ic_placeholder_image)
                    .into(binding.recipeImageView)
            }

            // Configurar el botón para eliminar la receta de favoritos
            binding.favoriteDeleteButton.setOnClickListener {
                // Usamos coroutines para eliminar la receta de la base de datos
                CoroutineScope(Dispatchers.IO).launch {
                    // Eliminar la receta de la base de datos
                    favoriteDao.deleteFavorite(recipe)

                    // Actualizamos la lista de favoritos en el hilo principal
                    CoroutineScope(Dispatchers.Main).launch {
                        favoriteRecipes.removeAt(adapterPosition)  // Eliminar la receta de la lista
                        notifyItemRemoved(adapterPosition)  // Notificar al adaptador que se eliminó un ítem
                    }
                }
            }
        }
    }
}