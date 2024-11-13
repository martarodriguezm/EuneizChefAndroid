package com.euneiz.euneizchef.database

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.euneiz.euneizchef.ApiService
import com.euneiz.euneizchef.R
import com.euneiz.euneizchef.Recipe
import com.euneiz.euneizchef.RecipeDetailActivity
import com.euneiz.euneizchef.databinding.ItemRecipeBinding
import com.euneiz.euneizchef.databinding.ItemRecipeFavouriteBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesAdapter(private val favoriteDao: FavoriteDao) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    private val favoriteRecipes = mutableListOf<FavoriteRecipe>()
    private val apiService = ApiService.create()

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

        fun bind(favoriteRecipe: FavoriteRecipe) {
            // Vincular los datos de la receta a las vistas
            binding.recipeTitleTextView.text = favoriteRecipe.strMeal
            binding.recipeAreaTextView.text = favoriteRecipe.strArea
            binding.recipeCategoryTextView.text = favoriteRecipe.strCategory

            // Cargar la imagen de la receta con Glide
            if (!favoriteRecipe.strMealThumb.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(favoriteRecipe.strMealThumb)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .into(binding.recipeImageView)
            } else {
                Glide.with(binding.root.context)
                    .load(R.drawable.ic_placeholder_image)
                    .into(binding.recipeImageView)
            }
            // Configurar el clic para abrir los detalles
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, RecipeDetailActivity::class.java)

                // Aquí obtenemos el objeto Recipe usando el idMeal
                CoroutineScope(Dispatchers.IO).launch {
                    val recipe = getRecipeById(favoriteRecipe.idMeal)

                    // Si la receta se encontró, pasamos el objeto Recipe al intent
                    if (recipe != null) {
                        withContext(Dispatchers.Main) {
                            intent.putExtra("recipe", recipe) // Pasamos el objeto Recipe
                            context.startActivity(intent)
                        }
                    }
                }
            }

            // Configuración del botón para eliminar la receta de favoritos
            binding.favoriteDeleteButton.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    favoriteDao.deleteFavorite(favoriteRecipe)
                    CoroutineScope(Dispatchers.Main).launch {
                        favoriteRecipes.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                    }
                }
            }
        }
        // Llamada a la API para obtener los detalles de la receta por su ID
        private suspend fun getRecipeById(idMeal: String): Recipe? {
            return try {
                // Llamada a la API usando Retrofit
                val response = apiService.getRecipeDetails(idMeal).execute()

                // Si la respuesta es exitosa, se devuelve la receta
                if (response.isSuccessful) {
                    val recipeDetailsResponse = response.body()
                    recipeDetailsResponse?.meals?.firstOrNull()?.let { meal ->
                        // Se mapea la respuesta a nuestro modelo Recipe
                        Recipe(
                            idMeal = meal.idMeal,
                            strMeal = meal.strMeal,
                            strCategory = meal.strCategory,
                            strArea = meal.strArea,
                            strMealThumb = meal.strMealThumb,
                            strInstructions = meal.strInstructions,
                            strTags = meal.strTags,
                            strYoutube = meal.strYoutube,
                            strSource = meal.strSource,
                            strImageSource = meal.strImageSource,
                            strCreativeCommonsConfirmed = meal.strCreativeCommonsConfirmed,
                            dateModified = meal.dateModified,
                            strIngredient1 = meal.strIngredient1,
                            strIngredient2 = meal.strIngredient2,
                            strIngredient3 = meal.strIngredient3,
                            strIngredient4 = meal.strIngredient4,
                            strIngredient5 = meal.strIngredient5,
                            strIngredient6 = meal.strIngredient6,
                            strIngredient7 = meal.strIngredient7,
                            strIngredient8 = meal.strIngredient8,
                            strIngredient9 = meal.strIngredient9,
                            strIngredient10 = meal.strIngredient10,
                            strIngredient11 = meal.strIngredient11,
                            strIngredient12 = meal.strIngredient12,
                            strIngredient13 = meal.strIngredient13,
                            strIngredient14 = meal.strIngredient14,
                            strIngredient15 = meal.strIngredient15,
                            strIngredient16 = meal.strIngredient16,
                            strIngredient17 = meal.strIngredient17,
                            strIngredient18 = meal.strIngredient18,
                            strIngredient19 = meal.strIngredient19,
                            strIngredient20 = meal.strIngredient20,
                            strMeasure1 = meal.strMeasure1,
                            strMeasure2 = meal.strMeasure2,
                            strMeasure3 = meal.strMeasure3,
                            strMeasure4 = meal.strMeasure4,
                            strMeasure5 = meal.strMeasure5,
                            strMeasure6 = meal.strMeasure6,
                            strMeasure7 = meal.strMeasure7,
                            strMeasure8 = meal.strMeasure8,
                            strMeasure9 = meal.strMeasure9,
                            strMeasure10 = meal.strMeasure10,
                            strMeasure11 = meal.strMeasure11,
                            strMeasure12 = meal.strMeasure12,
                            strMeasure13 = meal.strMeasure13,
                            strMeasure14 = meal.strMeasure14,
                            strMeasure15 = meal.strMeasure15,
                            strMeasure16 = meal.strMeasure16,
                            strMeasure17 = meal.strMeasure17,
                            strMeasure18 = meal.strMeasure18,
                            strMeasure19 = meal.strMeasure19,
                            strMeasure20 = meal.strMeasure20
                        )
                    }
                } else {
                    // Si la respuesta no es exitosa, devolver null
                    null
                }
            } catch (e: Exception) {
                // Si ocurre algún error en la llamada a la API, devolver null
                null
            }
        }
    }
}