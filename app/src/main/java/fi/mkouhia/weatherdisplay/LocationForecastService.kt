package fi.mkouhia.weatherdisplay

import no.api.meteo.MeteoException
import no.api.meteo.client.MeteoClient
import no.api.meteo.client.MeteoData
import no.api.meteo.entity.core.MeteoServiceVersion
import no.api.meteo.entity.core.service.locationforecast.LocationForecast
import no.api.meteo.service.AbstractMeteoService
import no.api.meteo.service.MeteoDataParser
import no.api.meteo.service.locationforecast.LocationforcastLTSParser

class LocationForecastService (meteoClient: MeteoClient?) :
    AbstractMeteoService(meteoClient, "locationforecast", MeteoServiceVersion(1, 9)) {

    private val parser: MeteoDataParser<LocationForecast> = LocationforcastLTSParser()

    fun fetchContent(latitude: Double, longitude: Double, altitude: Int = 0): MeteoData<LocationForecast> {
        var builder = createServiceUriBuilder()
            .addParameter("lat", latitude)
            .addParameter("lon", longitude)
        if (altitude != 0) {
            builder = builder.addParameter("msl", altitude)
        }
        val response = meteoClient.fetchContent(builder.build())
        return MeteoData(parser.parse(response.data), response)
    }

}