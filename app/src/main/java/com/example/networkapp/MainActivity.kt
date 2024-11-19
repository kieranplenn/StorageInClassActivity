package com.example.networkapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONObject

private const val PREFS_NAME = "ComicPrefs"
private const val TITLE_KEY = "title"
private const val ALT_KEY = "alt"
private const val IMG_URL_KEY = "img"

class MainActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var numberEditText: EditText
    private lateinit var showButton: Button
    private lateinit var comicImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        titleTextView = findViewById(R.id.comicTitleTextView)
        descriptionTextView = findViewById(R.id.comicDescriptionTextView)
        numberEditText = findViewById(R.id.comicNumberEditText)
        showButton = findViewById(R.id.showComicButton)
        comicImageView = findViewById(R.id.comicImageView)

        // Initialize RequestQueue for network requests
        requestQueue = Volley.newRequestQueue(this)

        // Load previously saved comic (if available)
        loadSavedComic()

        // Set up button click listener
        showButton.setOnClickListener {
            val comicId = numberEditText.text.toString().trim()
            if (comicId.isNotEmpty()) {
                downloadComic(comicId)
            } else {
                Toast.makeText(this, "Please enter a comic number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fetches comic from web as a JSON object
    private fun downloadComic(comicId: String) {
        val url = "https://xkcd.com/$comicId/info.0.json"
        val jsonObjectRequest = JsonObjectRequest(
            url,
            { response ->
                showComic(response)
                saveComic(response) // Save comic data after displaying it
            },
            { error ->
                Toast.makeText(this, "Error fetching comic: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    // Displays a comic using the given JSON object
    private fun showComic(comicObject: JSONObject) {
        val title = comicObject.getString("title")
        val alt = comicObject.getString("alt")
        val imgUrl = comicObject.getString("img")

        titleTextView.text = title
        descriptionTextView.text = alt
        Picasso.get().load(imgUrl).into(comicImageView)
    }

    // Saves comic data to SharedPreferences
    private fun saveComic(comicObject: JSONObject) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        with(prefs.edit()) {
            putString(TITLE_KEY, comicObject.getString("title"))
            putString(ALT_KEY, comicObject.getString("alt"))
            putString(IMG_URL_KEY, comicObject.getString("img"))
            apply()
        }
        Toast.makeText(this, "Comic saved!", Toast.LENGTH_SHORT).show()
    }

    // Loads saved comic data (if available) and displays it
    private fun loadSavedComic() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val title = prefs.getString(TITLE_KEY, null)
        val alt = prefs.getString(ALT_KEY, null)
        val imgUrl = prefs.getString(IMG_URL_KEY, null)

        if (title != null && alt != null && imgUrl != null) {
            titleTextView.text = title
            descriptionTextView.text = alt
            Picasso.get().load(imgUrl).into(comicImageView)
        } else {
            Toast.makeText(this, "No saved comic found", Toast.LENGTH_SHORT).show()
        }
    }
}
