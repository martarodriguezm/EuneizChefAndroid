package com.euneiz.euneizchef

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.euneiz.euneizchef.categories.AreaRecipesActivity
import com.euneiz.euneizchef.categories.CategoryRecipesActivity
import com.euneiz.euneizchef.categories.SearchResultsActivity
import com.euneiz.euneizchef.databinding.ActivityCategoriesBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class CategoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoriesBinding

    // Lista de categorías de la API
    private val categories = listOf(
        "Beef", "Chicken", "Dessert", "Lamb", "Miscellaneous", "Pasta", "Pork",
        "Seafood", "Side", "Starter", "Vegan", "Vegetarian", "Breakfast", "Goat"
    )
    // Lista de areas de la API
    private val areas = listOf(
        "American", "British", "Canadian", "Chinese", "Croatian", "Dutch", "Egyptian",
        "Filipino", "French", "Greek", "Indian", "Irish", "Italian", "Jamaican", "Japanese",
        "Kenyan", "Malaysian", "Mexican", "Moroccan", "Polish", "Portuguese", "Russian",
        "Spanish", "Thai", "Tunisian", "Turkish", "Ukrainian", "Vietnamese"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Cambiar color de fondo de la ActionBar
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Marcar el ítem de categorías en el BottomNavigationView
        binding.bottomNavigationView.selectedItemId = R.id.nav_categories

        // Configurar el listener de navegación
        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    true
                }

                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    true
                }

                R.id.nav_categories -> true // Ya estamos en Categorías
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

        // Agregar un botón para cada categoría en el LinearLayout
        categories.forEach { category ->
            // Envolver el contexto con el estilo
            val themedContext = ContextThemeWrapper(this, R.style.CategoryButtonStyle)
            val button = Button(themedContext).apply {
                text = category
                setOnClickListener {
                    openCategoryRecipesActivity(category)
                }
            }
            binding.categoriesContainer.addView(button)
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

    // Función para abrir la actividad de recetas de la categoría seleccionada
    private fun openCategoryRecipesActivity(category: String) {
        val intent = Intent(this, CategoryRecipesActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }

    // Función para abrir la actividad de recetas de la area seleccionada
    private fun openAreaRecipesActivity(area: String) {
        val intent = Intent(this, AreaRecipesActivity::class.java)
        intent.putExtra("area", area)
        startActivity(intent)
    }


    override fun onResume() {
        super.onResume()
        // Forzar que el ítem "Categories" esté seleccionado cada vez que se vuelve a esta actividad
        binding.bottomNavigationView.selectedItemId = R.id.nav_categories
    }
}