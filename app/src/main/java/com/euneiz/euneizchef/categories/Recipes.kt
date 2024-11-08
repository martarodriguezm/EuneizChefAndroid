package com.euneiz.euneizchef.categories

data class Recipe(
    val idMeal: String,
    val strMeal: String,
    val strCategory: String,
    val strArea: String,
    val strMealThumb: String?
)

// Response wrapper para la lista de recetas
data class RecipesResponse(
    val meals: List<Recipe>
)
data class RecipeDetailsResponse(
    val meals: List<Recipe>
)