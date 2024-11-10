package com.euneiz.euneizchef.database


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_recipes")
data class FavoriteRecipe(
    @PrimaryKey val idMeal: String,
    val strMeal: String,
    val strCategory: String,
    val strArea: String,
    val strMealThumb: String?
)
