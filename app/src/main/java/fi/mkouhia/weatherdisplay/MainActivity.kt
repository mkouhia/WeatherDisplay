package fi.mkouhia.weatherdisplay

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.pwittchen.weathericonview.WeatherIconView
import com.jakewharton.threetenabp.AndroidThreeTen
import fi.mkouhia.weatherdisplay.model.ForecastTimeStep
import fi.mkouhia.weatherdisplay.model.MetJsonForecast
import fi.mkouhia.weatherdisplay.model.ForecastTimePeriod
import kotlinx.android.synthetic.main.activity_fullscreen.*
import org.slf4j.LoggerFactory
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log10
import kotlin.math.pow


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class MainActivity : AppCompatActivity() {

    private var metJsonForecast : MetJsonForecast? = null

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreen_content.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
//        supportActionBar?.show()
//        fullscreen_content_controls.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
//    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
//        if (AUTO_HIDE) {
//            delayedHide(AUTO_HIDE_DELAY_MILLIS)
//        }
//        false
//    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this) // Proper time handling, backport for API < 26

        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.hide()

        mVisible = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreen_content.setOnClickListener { toggle() }

//        // Upon interacting with UI controls, delay any scheduled hide()
//        // operations to prevent the jarring behavior of controls going away
//        // while interacting with the UI.
//        dummy_button.setOnTouchListener(mDelayHideTouchListener)



        updateForecast()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
//        fullscreen_content_controls.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        fullscreen_content.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    fun updateForecast() {
        val latitude = 60.16082
        val longitude = 24.88908
        val altitude = 17

        val url = "https://api.met.no/weatherapi/locationforecast/2.0/?lat=$latitude&lon=$longitude&altitude=$altitude"

        val request = MeteoRequest(Request.Method.GET, url,
            Response.Listener<MetJsonForecast> { response ->
                metJsonForecast = response
                renderForecast(response)
            },
            Response.ErrorListener { println("error?") })

        RequestQueueSingleton.getInstance(this).addToRequestQueue(request);
    }

    private fun renderForecast(forecast: MetJsonForecast) {
        renderSummaryForecasts(forecast)
        renderChart(forecast)
    }


    private fun renderSummaryForecasts(forecast: MetJsonForecast) {

        val localDateTime = LocalDateTime.now()


        // TODO fix indices - find correct points in time

        // Current
        // FIXME now origin is next forecast - replace with measurement values
        val currentTimeStep = forecast.properties.timeseries.find { ts -> ts.localDateTime >= localDateTime }
        val currentTemp = currentTimeStep?.data?.instant?.details?.air_temperature ?: Double.NaN
        renderTemperature(R.id.currentTemperature, currentTemp)
        renderTime(R.id.currentTime, localDateTime)


        // Main
        renderForecast(
            currentTimeStep,
            R.id.dayNameCurrent,
            R.id.mainForecastIconImg,
            R.id.mainForecastIconText,
            R.id.mainForecastWindArrow,
            R.id.mainForecastTemp,
            false
        )

        // Plus one
        val plusOneTime = localDateTime.plusDays(1).withHour(6).withMinute(0).withSecond(0)
        val plusOneTimeStep = forecast.properties.timeseries.find { ts -> ts.localDateTime >= plusOneTime }
        renderForecast(
            plusOneTimeStep,
            R.id.forecastPlusOneName,
            R.id.forecastPlusOneIconImg,
            R.id.forecastPlusOneIconText,
            R.id.forecastPlusOneWindArrow,
            R.id.forecastPlusOneTemps,
            true
        )

//        // Plus two
//        renderForecast(
//            forecast.properties.timeseries[2],
//            0,
//            R.id.forecastPlusTwoIcon,
//            R.id.forecastPlusTwoWindArrow
//        )
//
//        // Plus three
//        renderForecast(
//            forecast.properties.timeseries[3],
//            0,
//            R.id.forecastPlusThreeIcon,
//            R.id.forecastPlusThreeWindArrow
//        )

    }

    private fun renderForecast(
        forecastTimeStep: ForecastTimeStep?,
        timeTextViewId: Int,
        imgWeatherIconViewId: Int,
        textWeatherIconViewId: Int,
        windArrowImageViewId: Int,
        temperatureTextViewId: Int,
        showMinMaxTemp: Boolean
    ) {
        if (forecastTimeStep == null) {
            return
        }

        // Display forecast time
        if (timeTextViewId > 0) {
            val textView: TextView? = findViewById(timeTextViewId)
            val dayName = forecastTimeStep.localDateTime.dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, resources.configuration.locales[0])
            val startHour = forecastTimeStep.localDateTime.hour
            val str = String.format("%s %d-%d", dayName, startHour, startHour + 6)
            textView?.text = str
        }

        // Display icon
        if (textWeatherIconViewId > 0) {
            renderForecastIcon(
                imgWeatherIconViewId,
                textWeatherIconViewId,
                forecastTimeStep.data.next_6_hours
            )
        }

        // Display forecast temperature
        if (temperatureTextViewId > 0) {
//            if (showMinMaxTemp) {
                renderTemperature(
                    temperatureTextViewId,
                    forecastTimeStep.data.next_6_hours?.details?.air_temperature_min ?: Double.NaN,
                    forecastTimeStep.data.next_6_hours?.details?.air_temperature_max ?: Double.NaN
                )

//            } else {
//                renderTemperature(
//                    temperatureTextViewId,
//                    forecastTimeStep.data.next_6_hours?.details?.ai ?: Double.NaN
//                )
//            }
        }
        // TODO Display forecast precipitation

        // Display wind arrow
        if (windArrowImageViewId > 0) {
            setWindSpeedIcon(
                windArrowImageViewId,
                forecastTimeStep.data.instant.details.wind_speed ?: 0.0,
                forecastTimeStep.data.instant.details.wind_from_direction ?: 0.0
            )
        }

    }

    private fun renderForecastIcon(
        imgWeatherIconViewId: Int,
        textWeatherIconViewId: Int,
        period: ForecastTimePeriod?
    ) {
        val symbolName = period?.summary?.symbol_code
        // TODO if returning, set NA images

        val weatherIconView: WeatherIconView? = findViewById(textWeatherIconViewId)
        val imageView: ImageView? = findViewById(imgWeatherIconViewId)

        val useTextIcons = false
        if (useTextIcons) {
            weatherIconView?.visibility = View.VISIBLE
            imageView?.visibility = View.INVISIBLE

            if (weatherIconView == null || symbolName == null) {
                return
            }

            weatherIconView.setIconResource(getString(R.string.wi_hurricane_warning))

        } else {
            imageView?.visibility = View.VISIBLE
            weatherIconView?.visibility = View.INVISIBLE

            if (imageView == null || symbolName == null) {
                return
            }

            // Use met.no images, mapping directly from api symbol
            println("Setting symbol $symbolName")
            val weatherIconName = "metno_$symbolName"
            val iconStringId = resources.getIdentifier(weatherIconName, "drawable", packageName)
            if (iconStringId == 0) {
                logger.warn("No icon found with id $iconStringId")
                // TODO set default NA icon
                return;
            }

            imageView.setImageDrawable(getDrawable(iconStringId))
        }
    }

    private fun renderTime(textViewId: Int, localDateTime: LocalDateTime) {
        val textView: TextView = findViewById(textViewId) ?: return
        val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        textView.text = localDateTime.format(dateTimeFormatter)
    }

    private fun renderTemperature(textViewId: Int, temperature: Double) {
        val textView: TextView = findViewById(textViewId) ?: return
        textView.text = resources.getString(R.string.tempDegC, temperature)
    }
    private fun renderTemperature(textViewId: Int, minTemperature: Double, maxTemperature: Double) {
        val textView: TextView = findViewById(textViewId) ?: return
        textView.text = resources.getString(R.string.tempDegCRange, minTemperature, maxTemperature)
    }
    /**
     * Set wind speed icon
     * @param windArrowImageViewId ID of view, into which to set this wind speed icon
     * @param windSpeed wind speed in m/s, positive number
     * @param windDirection direction, from where the wind is blowing, in degrees
     */
    private fun setWindSpeedIcon(windArrowImageViewId: Int, windSpeed: Double, windDirection: Double) {
        val imageView : ImageView = findViewById(windArrowImageViewId) ?: return

        val windKnots = windSpeed.toFloat() * 3.6 / 1.852
        val windKnotsNearest5 = (5f * Math.round(windKnots / 5f)).toInt()
        val uri = "@drawable/ic_symbol_wind_speed_${windKnotsNearest5}kt"
        val imageResource = resources.getIdentifier(uri, null, packageName)
        if (imageResource == 0) {
            logger.warn("setWindSpeedIcon: could not find icon by name $uri")
        }

        imageView.setImageResource(imageResource);

        imageView.rotation = windDirection.toFloat() + 180f
    }


    private fun renderChart(forecast: MetJsonForecast) {
        val chart : CombinedChart = findViewById(R.id.weatherChart) ?: return
        println("forming chart data")
        val data : CombinedData = formChartData(forecast)
        println("ready?")

        val axisTextSize = 12f

//        val mv = MyMarkerView(applicationContext, R.layout.custom_marker_view)
//        mv.setChartView(mChart)
//        mChart.marker = mv

        chart.setTouchEnabled(false)
        chart.setPinchZoom(false)

        val chartDescription = Description()
        chartDescription.text = ""
        chart.description = chartDescription

        chart.legend.isEnabled = false

        val xAxis: XAxis = chart.getXAxis()
        xAxis.setDrawLabels(true)
        xAxis.textColor = Color.WHITE
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(false)
        xAxis.textSize = axisTextSize
        xAxis.valueFormatter = HourAxisValueFormatter()
        xAxis.setDrawLimitLinesBehindData(true)

        val tempAxis: YAxis = chart.getAxisLeft()
        tempAxis.setDrawLabels(true)
        tempAxis.textColor = Color.WHITE
        tempAxis.setDrawAxisLine(false)
        tempAxis.textSize = axisTextSize

        val rainAxis = chart.getAxisRight()
        rainAxis.setDrawAxisLine(false)
        rainAxis.setDrawLabels(true)
        rainAxis.setDrawGridLines(false)
        rainAxis.textColor = Color.WHITE
        rainAxis.textSize = axisTextSize

        chart.setData(data)

        val rainMax = data.getYMax(YAxis.AxisDependency.RIGHT)

        // Shift temperatures up if rain bars are high
        var tempMin = data.getYMin(YAxis.AxisDependency.LEFT)
        if (rainMax >= 3.0f) {
            tempMin -= 1.0f
        }

        val spec = computeLimits(tempMin, data.getYMax(YAxis.AxisDependency.LEFT))
        tempAxis.axisMinimum = spec.minimum
        tempAxis.axisMaximum = spec.maximum
        tempAxis.labelCount = spec.numLabels
        tempAxis.granularity = spec.spacing

        val specRight = computeSecondaryLimits(0.0f, maxOf(15.0f, rainMax), spec.numLabels)
        rainAxis.axisMinimum = specRight.minimum
        rainAxis.axisMaximum = specRight.maximum
        rainAxis.labelCount = specRight.numLabels
        rainAxis.granularity = specRight.spacing

        chart.invalidate() // refresh
    }

    private fun formChartData(forecast: MetJsonForecast) : CombinedData {
        val tempValues: ArrayList<Entry> = ArrayList()
        val rainValues: ArrayList<BarEntry> = ArrayList()

        val timeNow = LocalDateTime.now()
        val timeseries = forecast.properties.timeseries
        var count = 0
        var firstForecastTime = timeNow
        var hourFloatAdd = 0f
        for (ts : ForecastTimeStep in timeseries) {
            if (count == 12) {
                break
            }

            val forecastTime: LocalDateTime = ts.localDateTime
            if (forecastTime.plusMinutes(20) < timeNow) {
                continue
            } else if (count == 0) {
                firstForecastTime = forecastTime
            }
            hourFloatAdd = org.threeten.bp.Period.between(firstForecastTime.toLocalDate(), forecastTime.toLocalDate()).days * 24f
            ++count

            val timeFloat = forecastTime.hour.toFloat() + hourFloatAdd

            val temp = ts.data.instant.details.air_temperature
            if (temp != null) {
                tempValues.add(Entry(timeFloat, temp.toFloat()))
            }

            val precipitation    = ts.data.next_1_hours?.details?.precipitation_amount?.toFloat()
            val precipitationMin = ts.data.next_1_hours?.details?.precipitation_amount_min?.toFloat()
            val precipitationMax = ts.data.next_1_hours?.details?.precipitation_amount_max?.toFloat()
            if (precipitationMin != null && precipitationMax != null && precipitationMax > 0f) {
                rainValues.add(BarEntry(timeFloat, floatArrayOf(precipitationMin, precipitationMax)))
            } else if (precipitation != null) {
                rainValues.add(BarEntry(timeFloat, precipitation))
            }

        }

        val tempDataSet: LineDataSet
        tempDataSet = LineDataSet(tempValues, "Temperature")
        tempDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER)
        tempDataSet.color = Color.WHITE
        tempDataSet.setDrawCircles(false);

        val tempData = LineData(tempDataSet)
        tempData.setDrawValues(false)

        val rainDataSet = BarDataSet(rainValues, "Precipitation")
        rainDataSet.axisDependency = YAxis.AxisDependency.RIGHT
        rainDataSet.colors = listOf(
            Color.argb(255,255,255,255),
            Color.argb(192,255,255,255))
        val rainData = BarData(rainDataSet)
        rainData.setDrawValues(false)

        val combinedData = CombinedData();
        combinedData.setData(tempData)
        combinedData.setData(rainData)

        return combinedData
    }


    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300


        fun computeLimits(minData : Float, maxData : Float, minimumSeparation : Float = 1.0f) : AxisSpec {

            var div = 0.0f
            var nLabels : Int
            var thisMin : Float
            var thisMax : Float
            do {
                div += minimumSeparation
                thisMin = minData - (minData % div)
                thisMax = maxData - (maxData % div) + (if (maxData % div == 0f) 0f else div)
                val diff = thisMax - thisMin

                nLabels = (diff / div).toInt() + 1
            } while ( nLabels > 9)

            while (nLabels <= 6) {
                ++nLabels
                thisMin -= div
            }
            return AxisSpec(thisMin, thisMax, nLabels, div)
        }

        fun computeSecondaryLimits(minData: Float, maxData: Float, numLabels: Int) : AxisSpec {
            val divSeries = floatArrayOf(1.0f, 2.0f, 2.5f, 3.0f, 4.0f, 5.0f, 6.0f, 8.0f)
            val ord : Int = log10((minData + maxData) / 2).toInt()

            var thisMin = 0.0f
            var thisMax = 0.0f
            var nLabels = 0
            var found = false
            var div = 0.0f
            while (!found) {
                for (divBase in divSeries) {
                    div = divBase * 10.0f.pow(ord)
                    thisMin = minData - (minData % div)
                    thisMax = maxData - (maxData % div) + (if (maxData % div == 0f) 0f else div)
                    val diff = thisMax - thisMin
                    nLabels = (diff / div).toInt() + 1

                    if (nLabels <= numLabels) {
                        found = true
                        break
                    }
                }
            }
            while (nLabels < numLabels) {
                ++nLabels
                thisMax += div
            }
            return AxisSpec(thisMin, thisMax, nLabels, div)
        }

        data class AxisSpec(val minimum : Float, val maximum : Float, val numLabels : Int, val spacing : Float = (maximum - minimum) / (numLabels-1))

        val logger = LoggerFactory.getLogger(MainActivity::class.java)

    }

}
