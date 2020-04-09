package fi.mkouhia.weatherdisplay

import com.github.mikephil.charting.formatter.ValueFormatter

class HourAxisValueFormatter() : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        val hour : Int = value.toInt() % 24
        return hour.toString().padStart(2, '0')
    }
}