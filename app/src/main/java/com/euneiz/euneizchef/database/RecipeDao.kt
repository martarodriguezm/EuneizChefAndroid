package com.euneiz.euneizchef.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(recipe: FavoriteRecipe)

    @Delete
    suspend fun deleteFavorite(recipe: FavoriteRecipe)

    @Query("SELECT * FROM favorite_recipes")
    suspend fun getAllFavorites(): List<FavoriteRecipe>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_recipes WHERE idMeal = :idMeal)")
    suspend fun isFavorite(idMeal: String): Boolean
}