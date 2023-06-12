package com.example.weatherforecastapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat

class WeatherRVAdapter(
    private val context: Context,
    private val weatherModals: ArrayList<WeatherRVModal>
) :
    RecyclerView.Adapter<WeatherRVAdapter.WeatherViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WeatherViewHolder {
        val adapterLayout =
            LayoutInflater.from(context).inflate(R.layout.weather_rv_item, parent, false)
        return WeatherViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val weatherModal = weatherModals[position * 4]

        holder.timeTV.text = weatherModal.time
        holder.tempTV.text = "${weatherModal.temp}°C"
        holder.windTV.text = "${weatherModal.windSpeed} km/h"
        Log.d("IconURL", "https:${weatherModal.icon}")
        Picasso.get().load("https:${weatherModal.icon}").into(holder.conditionIV)

        val input = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val output = SimpleDateFormat("HH:mm")
        try {
            val date = input.parse(weatherModal.time)
            holder.timeTV.text = output.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return weatherModals.size / 4
    }

    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val windTV: TextView = itemView.findViewById(R.id.idTVWindSpeed)
        val tempTV: TextView = itemView.findViewById(R.id.idTVTemp)
        val timeTV: TextView = itemView.findViewById(R.id.idTVTime)
        val conditionIV: ImageView = itemView.findViewById(R.id.idIVCondition)
    }
}