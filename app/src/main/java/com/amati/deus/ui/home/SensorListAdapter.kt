package com.amati.deus.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amati.deus.R
import com.amati.deus.models.DeviceSensor

class SensorListAdapter : RecyclerView.Adapter<SensorListAdapter.SensorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        return SensorViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        val currentSensor = (position)

    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    class SensorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sensorNameTextView: TextView = itemView.findViewById(R.id.sensorNameTextView)

        fun bind(deviceSensor: DeviceSensor) {
            sensorNameTextView.text = deviceSensor.sensorName
        }

        companion object {
            fun create(parent: ViewGroup): SensorViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyler_device_sensor_item, parent, false)
                return SensorViewHolder(view)
            }
        }
    }
}