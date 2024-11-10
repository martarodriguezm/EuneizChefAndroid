package com.euneiz.euneizchef.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

@Dao
interface FavoriteRecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(recipe: FavoriteRecipe)

    @Delete
    suspend fun removeFavorite(recipe: FavoriteRecipe)

    @Query("SELECT * FROM favorite_recipes WHERE recipeId = :id LIMIT 1")
    suspend fun getFavoriteById(id: Int): FavoriteRecipe?
}
