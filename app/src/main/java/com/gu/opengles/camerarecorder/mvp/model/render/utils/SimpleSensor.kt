package com.gu.opengles.camerarecorder.mvp.model.render.utils

import java.text.SimpleDateFormat
import java.util.*

class SimpleSensor {
    companion object {
        private val speedRange = (30..40)

        fun getSimulatedSpeed(): String {
            return "    ${speedRange.random()}km/h"
        }

        fun getTemperature(): String {
            return "26°C"
        }

        fun getDateTimeString(): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss ", Locale.getDefault())
            return formatter.format(Calendar.getInstance().time)
        }
    }
}