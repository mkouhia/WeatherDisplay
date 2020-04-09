package fi.mkouhia.weatherdisplay.model

import kotlinx.serialization.Serializable

/**
 * Weather parameters valid for a specified time period
 */
@Serializable
data class ForecastTimePeriod (
    val summary : ForecastSummary,
    val details : DetailsPeriod
)

@Serializable
data class ForecastSummary (
    val symbol_code : String
)

@Serializable
data class DetailsPeriod (

    /** Maximum air temperature in period */
    val air_temperature_max : Double? = null,

    /** Minimum air temperature in period */
    val air_temperature_min : Double? = null,

    /** Best estimate for amount of precipitation for this period */
    val precipitation_amount : Double? = null,

    /** Maximum amount of precipitation for this period */
    val precipitation_amount_max : Double? = null,

    /** Minimum amount of precipitation for this period */
    val precipitation_amount_min : Double? = null,

    /** Probability of any precipitation coming for this period */
    val probability_of_precipitation : Double? = null,

    /** Probability of any thunder coming for this period */
    val probability_of_thunder : Double? = null,

    /** Maximum ultraviolet index if sky is clear
     * Note: Documented, but not found in JSON
     */
    val ultraviolet_index_clear_sky_max : Double? = null
)