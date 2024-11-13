package com.euneiz.euneizchef

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.euneiz.euneizchef.categories.AreaRecipesActivity
import com.euneiz.euneizchef.categories.SearchResultsActivity
import com.euneiz.euneizchef.database.FavoriteDao
import com.euneiz.euneizchef.database.FavoriteRecipe
import com.euneiz.euneizchef.database.RecipeDatabase
import com.euneiz.euneizchef.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var apiService: ApiService
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var recipeDatabase: RecipeDatabase
    private lateinit var areas: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.main)

        // Cambiar color de fondo de la ActionBar
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener la lista de áreas desde strings.xml
        areas = resources.getStringArray(R.array.areas_list)

        // Instancia del servicio API
        apiService = ApiService.create()

        // Cargar receta del día
        loadRecipeOfTheDay()

        // Inicializa la base de datos y DAO
        recipeDatabase = RecipeDatabase.getDatabase(this)
        favoriteDao = recipeDatabase.favoriteDao()

        // Marcar el ítem de favoritos en el BottomNavigationView
        binding.bottomNavigationView.selectedItemId = R.id.nav_home


        // Configurar el listener de navegación
        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true //Ya estamos en Home
                //Navegar a Favorites
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    true
                }
                //Navegar a Categories
                R.id.nav_categories -> {
                    startActivity(Intent(this, CategoriesActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    true
                }
                else -> false
            }
        }
        // Configurar el botón de búsqueda
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotEmpty()) {
                // Abrir SearchResultsActivity y pasar el término de búsqueda
                val intent = Intent(this, SearchResultsActivity::class.java).apply {
                    putExtra("query", query)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show()
            }
        }
        // Agregar un botón para cada area en el LinearLayout
        areas.forEach { area ->
            // Envolver el contexto con el estilo
            val themedContext = ContextThemeWrapper(this, R.style.AreaButtonStyle)
            val button = Button(themedContext).apply {
                text = area
                setOnClickListener {
                    openAreaRecipesActivity(area)
                }
            }
            // Establecer márgenes manualmente porque si no no los detecta
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, // Ajuste del ancho
                LinearLayout.LayoutParams.WRAP_CONTENT  // Ajuste del alto
            ).apply {
                setMargins(16, 8, 16, 8) // Márgenes personalizados: (izquierda, arriba, derecha, abajo)
            }

            // Aplicar los LayoutParams al botón
            button.layoutParams = params

            // Agregar el botón al contenedor
            binding.areasContainer.addView(button)

            // Cargar la bandera en segundo plano
            loadFlag(area, button)

        }
    }

    private fun loadRecipeOfTheDay() {
        // Realiza la solicitud a la API para obtener la receta del día.
        apiService.getRecipeDetails("52772") // El ID de la receta del día (puede ser dinámico o fijo)
            .enqueue(object : retrofit2.Callback<RecipeDetailsResponse> {
                override fun onResponse(
                    call: retrofit2.Call<RecipeDetailsResponse>,
                    response: retrofit2.Response<RecipeDetailsResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val recipe = response.body()!!.meals[0]  // Asumimos que la respuesta tiene al menos una receta
                        updateUIWithRecipe(recipe)
                    } else {
                        Toast.makeText(applicationContext, "No se pudo obtener la receta", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<RecipeDetailsResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "Error al cargar la receta: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateUIWithRecipe(recipe: Recipe) {
        // Actualizar la UI con los datos de la receta
        binding.recipeTitleTextView.text = recipe.strMeal
        binding.recipeAreaTextView.text = recipe.strArea
        binding.recipeCategoryTextView.text = recipe.strCategory

        // Usar Glide para cargar la imagen
        Glide.with(this)
            .load(recipe.strMealThumb)
            .placeholder(R.drawable.ic_placeholder_image) // Imagen por defecto en caso de no tener imagen
            .into(binding.recipeImageView)

        // Configurar el clic en la receta del día para abrir la pantalla de detalles
        binding.dayRecipeContainer.setOnClickListener {
            // Crear un Intent para abrir la RecipeDetailActivity
            val intent = Intent(this, RecipeDetailActivity::class.java)

            // Pasar los datos de la receta a la nueva actividad
            intent.putExtra("recipe", recipe)  // Pasamos el objeto Recipe

            // Iniciar la actividad de detalles de la receta
            startActivity(intent)
        }

        // Verificar si la receta ya está en favoritos para definir el estado inicial del botón
        CoroutineScope(Dispatchers.IO).launch {
            val existingFavorite = favoriteDao.getAllFavorites().find { it.idMeal == recipe.idMeal }

            withContext(Dispatchers.Main) {
                // Establecer el estado inicial del botón de favoritos
                binding.favoriteButton.isSelected = existingFavorite != null
            }
        }

        // Configuración del botón de favoritos
        binding.favoriteButton.setOnClickListener {
            it.isSelected = !it.isSelected
            val favoriteRecipe = FavoriteRecipe(
                idMeal = recipe.idMeal,
                strMeal = recipe.strMeal,
                strCategory = recipe.strCategory,
                strArea = recipe.strArea,
                strMealThumb = recipe.strMealThumb
            )

            if (it.isSelected) {
                // Guardar receta en favoritos
                CoroutineScope(Dispatchers.IO).launch {
                    favoriteDao.insertFavorite(favoriteRecipe)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(binding.root.context, "La receta se ha añadido a favoritos", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Eliminar receta de favoritos
                CoroutineScope(Dispatchers.IO).launch {
                    favoriteDao.deleteFavorite(favoriteRecipe)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(binding.root.context, "La receta se ha eliminado de favoritos", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Método para obtener la URL de la bandera y cargarla en el botón
    private fun loadFlag(area: String, button: Button) {
        // Mapeo entre áreas y nombres de países (ajusta según la API y los países que necesites)
        val countryName = mapAreaToCountry(area)

        // URL de la API de Rest Countries para obtener datos del país
        val url = "https://restcountries.com/v3.1/name/$countryName"

        // Crear una solicitud HTTP con OkHttp
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val json = JSONArray(responseBody.string())
                    val countryData = json.getJSONObject(0)
                    val flagUrl = countryData.getJSONObject("flags").getString("png")

                    // Cargar la bandera usando Glide en el hilo principal
                    runOnUiThread {
                        Glide.with(button.context)
                            .load(flagUrl)
                            .fitCenter() // Ajusta la imagen para que encaje bien dentro del botón
                            .into(object : CustomTarget<Drawable>() {
                                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                    // Crear un fondo de color semitransparente para el texto
                                    val backgroundColor = ColorDrawable(ContextCompat.getColor(button.context, R.color.overlay)) // Fondo semitransparente

                                    // Combinar la bandera y el fondo de color
                                    val layers = arrayOf<Drawable>(resource, backgroundColor)
                                    val layerDrawable = LayerDrawable(layers).apply {
                                        setLayerInset(1, 0, 0, 0, 0) // Ajusta el fondo a la mitad inferior del botón
                                    }

                                    // Asigna el LayerDrawable como fondo del botón
                                    button.background = layerDrawable
                                    button.setTextColor(ContextCompat.getColor(button.context, R.color.white))
                                    button.textSize = 18f
                                    button.setTypeface(button.typeface, Typeface.BOLD)
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    // Opcional: limpiar recursos si es necesario
                                }
                            })
                    }
                }
            }
        })
    }

    // Mapa para relacionar el área con el nombre del país esperado en la API
    private fun mapAreaToCountry(area: String): String {
        return when (area) {
            "American" -> "United States"
            "British" -> "United Kingdom"
            "Canadian" -> "Canada"
            "Chinese" -> "China"
            "Croatian" -> "Croatia"
            "Dutch" -> "Netherlands"
            "Egyptian" -> "Egypt"
            "Filipino" -> "Philippines"
            "French" -> "France"
            "Greek" -> "Greece"
            "Indian" -> "India"
            "Irish" -> "Ireland"
            "Italian" -> "Italy"
            "Jamaican" -> "Jamaica"
            "Japanese" -> "Japan"
            "Kenyan" -> "Kenya"
            "Malaysian" -> "Malaysia"
            "Mexican" -> "Mexico"
            "Moroccan" -> "Morocco"
            "Polish" -> "Poland"
            "Portuguese" -> "Portugal"
            "Russian" -> "Russia"
            "Spanish" -> "Spain"
            "Thai" -> "Thailand"
            "Tunisian" -> "Tunisia"
            "Turkish" -> "Turkey"
            "Ukrainian" -> "Ukraine"
            "Vietnamese" -> "Vietnam"
            else -> area
        }
    }

    // Función para abrir la actividad de recetas de la area seleccionada
    private fun openAreaRecipesActivity(area: String) {
        val intent = Intent(this, AreaRecipesActivity::class.java)
        intent.putExtra("area", area)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Forzar que el ítem "Home" esté seleccionado cada vez que se vuelve a esta actividad
        binding.bottomNavigationView.selectedItemId = R.id.nav_home
    }
}
