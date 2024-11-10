package com.euneiz.euneizchef.categories

import com.euneiz.euneizchef.database.RecipeDetailsResponse
import com.euneiz.euneizchef.database.RecipesResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    // Endpoint para buscar recetas por nombre
    @GET("search.php")
    fun searchRecipesByName(
        @Query("s") name: String
    ): Call<RecipesResponse>

    // Endpoint para obtener recetas por categoría
    @GET("filter.php")
    fun getRecipesByCategory(
        @Query("c") category: String
    ): Call<RecipesResponse>

    // Endpoint para obtener recetas por area
    @GET("filter.php")
    fun getRecipesByArea(
        @Query("a") area: String
    ): Call<RecipesResponse>

    // Endpoint para obtener detalles de una receta por ID
    @GET("lookup.php")
    fun getRecipeDetails(
        @Query("i") recipeId: String
    ): Call<RecipeDetailsResponse>

    companion object {
        private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

        fun create(): ApiService {

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }
}