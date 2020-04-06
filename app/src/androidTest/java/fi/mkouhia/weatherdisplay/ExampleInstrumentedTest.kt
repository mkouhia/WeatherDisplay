package fi.mkouhia.weatherdisplay

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import no.api.meteo.MeteoException
import no.api.meteo.client.MeteoData
import no.api.meteo.entity.core.service.locationforecast.LocationForecast
import no.api.meteo.entity.core.service.locationforecast.PeriodForecast
import no.api.meteo.entity.core.service.locationforecast.PointForecast
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("fi.mkouhia.weatherdisplay", appContext.packageName)
    }

    @Test
    fun download() {
        val latitude = 60.0
        val longitude = 25.0
        val altitude = 17

        val meteoClient = WDMeteoClient(InstrumentationRegistry.getInstrumentation().targetContext)

//        val uri = URI("https://api.met.no/weatherapi/locationforecast/1.9/?lat=60.10&lon=9.58&msl=70");
//        meteoClient.fetchContent(uri);

        val service = LocationForecastService(meteoClient)
        try {
            val data : MeteoData<LocationForecast> = service.fetchContent(latitude, longitude, altitude)
            println(data.result.meta)
            println(data.result.location)
            for (f in data.result.forecasts) {

//                if (f is PointForecast) {
//                    f.
//                } else if (f is PeriodForecast) {
//                    f.
//                } else {
//                    log.warn("Unknown forecast type: " + f)
//                }
            }

//            println("DATA:")
//            println(data)
//            println("----")
        } catch (e: MeteoException) { // Handle exception.
            e.printStackTrace()
        }
    }

    companion object {
        private val log =
            LoggerFactory.getLogger(ExampleInstrumentedTest::class.java)
    }

}
