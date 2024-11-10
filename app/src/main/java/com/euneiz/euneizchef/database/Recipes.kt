package com.euneiz.euneizchef.database

data class Recipe(
    val idMeal: String,
    val strMeal: String,
    val strCategory: String,
    val strArea: String,
    val strMealThumb: String?,
    val description: String?,
    val name: String,
    val id: Int,
    val strDescription: String?
)

// Response wrapper para la lista de recetas
data class RecipesResponse(
    val meals: List<Recipe>
)
data class RecipeDetailsResponse(
    val meals: List<Recipe>
)