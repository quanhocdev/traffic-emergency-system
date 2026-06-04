package com.example.canhbao.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class SearchViewModel : ViewModel() {

    private val client = OkHttpClient()

    private val _placeSuggestions = MutableStateFlow<List<String>>(emptyList())
    val placeSuggestions = _placeSuggestions.asStateFlow()

    private val placePointMap = mutableMapOf<String, Point>()

    fun onSearchQueryChanged(query: String) {
        if (query.length < 2) {
            _placeSuggestions.value = emptyList()
            return
        }

        viewModelScope.launch {
            val results = searchPlaces(query)

            _placeSuggestions.value = results.map { it.first }

            results.forEach {
                placePointMap[it.first] = it.second
            }
        }
    }

    fun getPointForName(name: String): Point? {
        return placePointMap[name]
    }

    private suspend fun searchPlaces(
        query: String
    ): List<Pair<String, Point>> = withContext(Dispatchers.IO) {

        try {
            val url =
                "https://nominatim.openstreetmap.org/search?q=$query&format=json&limit=5&countrycodes=vn"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "CanhBaoApp/1.0")
                .build()

            val response = client.newCall(request).execute()
            val jsonArray = JSONArray(response.body?.string() ?: "[]")

            val results = mutableListOf<Pair<String, Point>>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                results.add(
                    obj.getString("display_name") to
                            Point.fromLngLat(
                                obj.getDouble("lon"),
                                obj.getDouble("lat")
                            )
                )
            }

            results
        } catch (e: Exception) {
            emptyList()
        }
    }
}