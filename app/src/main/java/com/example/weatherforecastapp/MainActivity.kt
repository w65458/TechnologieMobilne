package com.example.weatherforecastapp

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    lateinit var homeRL: RelativeLayout
    lateinit var loadingPB: ProgressBar
    lateinit var cityNameTV: TextView
    lateinit var tempTV: TextView
    lateinit var conditionTV: TextView
    lateinit var cityEdit: TextInputEditText
    lateinit var backIV: ImageView
    lateinit var iconIV: ImageView
    lateinit var searchIV: ImageView
    lateinit var weatherRV: RecyclerView

    private val weatherRVModalList: ArrayList<WeatherRVModal> = ArrayList()
    private val weatherRVAdapter = WeatherRVAdapter(this, weatherRVModalList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        homeRL = findViewById(R.id.idRLHome)
        loadingPB = findViewById(R.id.idPBLoading)
        cityNameTV = findViewById(R.id.idTVCityName)
        tempTV = findViewById(R.id.idTVTemp)
        conditionTV = findViewById(R.id.idTVCondition)
        cityEdit = findViewById(R.id.idEditCity)
        backIV = findViewById(R.id.idIVBack)
        iconIV = findViewById(R.id.idIVIcon)
        searchIV = findViewById(R.id.idIVSearch)
        weatherRV = findViewById(R.id.idRVWeather)
        weatherRV.adapter = weatherRVAdapter

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ), 100
            )
        }

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val cityName = if (location != null) {
            getCityName(location.longitude, location.latitude)
        } else {
            "London"
        }

        getWeatherInfo(cityName);

        searchIV.setOnClickListener {
            val city = cityEdit.text.toString()
            if (city.isEmpty()) {
                Toast.makeText(this, "Please enter city name", Toast.LENGTH_SHORT).show()
            } else {
                val inputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                cityNameTV.text = cityName
                getWeatherInfo(city)
                cityEdit.text?.clear()
                cityEdit.clearFocus()
            }
        }

        homeRL.setOnTouchListener { _, _ ->
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            false
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun getCityName(longitude: Double, latitude: Double): String {
        var cityName = "Not found"
        val geoCoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geoCoder.getFromLocation(latitude, longitude, 10)
            if (addresses != null) {
                for (address in addresses) {
                    if (address != null) {
                        val city = address.locality
                        if (city != null && city.length > 0) {
                            cityName = city
                        } else {
                            Toast.makeText(this, "City not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return cityName
    }

    private fun getWeatherInfo(cityName: String) {
        val url =
            "https://api.weatherapi.com/v1/forecast.json?key=4b59bb36090a4184908105345230806&q=$cityName&days=1&aqi=no&alerts=no"

        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(url,
            {
                cityNameTV.text = cityName
                loadingPB.visibility = android.view.View.GONE
                homeRL.visibility = android.view.View.VISIBLE
                weatherRVModalList.clear()
                try {
                    val temperature =
                        it.getJSONObject("current").getString("temp_c").substring(0, 2)
                    tempTV.text = "$temperature°C"
                    val isDay = it.getJSONObject("current").getInt("is_day")
                    val condition = it.getJSONObject("current").getJSONObject("condition")
                        .getString("text")
                    val icon = it.getJSONObject("current").getJSONObject("condition")
                        .getString("icon")
                    val largerIcon = icon.replace("64x64", "128x128")
                    Picasso.get().load("https:$largerIcon").into(iconIV)
                    conditionTV.text = condition
                    if (isDay == 1) {
                        getBackgroundPhoto(condition)
                    } else {
                        Picasso.get().load(
                            "https://images.unsplash.com/photo-1513628253939-010e64ac66cd?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mjd8fHN0YXIlMjBuaWdodCUyMCU1Q3xlbnwwfHwwfHx8MA%3D%3D&auto=format&fit=crop&w=500&q=60"
                        ).into(backIV)
                    }

                    val forecastObj = it.getJSONObject("forecast")
                    val forecast = forecastObj.getJSONArray("forecastday").getJSONObject(0)
                    val hourArray = forecast.getJSONArray("hour")
                    for (i in 0 until hourArray.length()) {
                        val hourObj = hourArray.getJSONObject(i)
                        val time = hourObj.getString("time")
                        val temp = hourObj.getString("temp_c")
                        val conditionIcon = hourObj.getJSONObject("condition").getString("icon")
                        val wind = hourObj.getString("wind_kph")
                        val weatherRVModal = WeatherRVModal(time, temp, conditionIcon, wind)
                        weatherRVModalList.add(weatherRVModal)
                    }
                    weatherRVAdapter.notifyDataSetChanged()

                } catch (e: org.json.JSONException) {
                    e.printStackTrace()
                }
            }, {
                Log.e("VolleyError", it.toString())
                Toast.makeText(this, "Please enter valid city name", Toast.LENGTH_SHORT).show()
            })

        requestQueue.add(jsonObjectRequest)
    }

    private fun getBackgroundPhoto(condition: String) {
        when (condition) {
            "Sunny", "Clear" -> {
                Picasso.get().load(
                    "https://images.unsplash.com/photo-1558418294-9da149757efe?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=687&q=80"
                ).into(backIV)
            }
            "Partly cloudy" -> {
                Picasso.get().load(
                    "https://images.unsplash.com/photo-1610736702440-9dfab24cd7da?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTJ8fHN1bm55JTIwZGF5fGVufDB8fDB8fHww&auto=format&fit=crop&w=500&q=60"
                ).into(backIV)
            }
            "Cloudy", "Overcast" -> {
                Picasso.get().load(
                    "https://images.unsplash.com/photo-1534088568595-a066f410bcda?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=902&q=80"
                ).into(backIV)
            }
            "Light rain shower", "Patchy rain possible", "Moderate rain" -> {
                Picasso.get().load(
                    "https://images.unsplash.com/photo-1618329397023-cc688d12bb79?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MzV8fHJhaW58ZW58MHx8MHx8fDA%3D&auto=format&fit=crop&w=500&q=60"
                ).into(backIV)
            }
            "Fog", "Mist" -> {
                Picasso.get().load(
                    "https://images.unsplash.com/photo-1585651686997-5516bd534e9d?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=687&q=80"
                ).into(backIV)
            }
            "Patchy light rain with thunder" -> {
                Picasso.get().load(
                    "https://images.unsplash.com/photo-1559087867-ce4c91325525?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1470&q=80"
                ).into(backIV)
            }
        }
    }
}