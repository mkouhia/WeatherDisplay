package fi.mkouhia.weatherdisplay.model

import kotlinx.serialization.Serializable

@Serializable
data class ForecastTimeInstant (
	val details : DetailsInstant
)

@Serializable
class DetailsInstant (

	/** Air pressure at sea level */
	val air_pressure_at_sea_level : Double? = null,

	/** Air temperature */
	val air_temperature : Double? = null,

	/** Amount of sky covered by clouds */
	val cloud_area_fraction : Double? = null,

	/** Amount of sky covered by clouds at high elevation */
	val cloud_area_fraction_high : Double? = null,

	/** Amount of sky covered by clouds at low elevation */
	val cloud_area_fraction_low : Double? = null,

	/** Amount of sky covered by clouds at medium elevation */
	val cloud_area_fraction_medium : Double? = null,

	/** Dew point temperature at sea level */
	val dew_point_temperature : Double? = null,

	/** Amount of area covered by fog */
	val fog_area_fraction : Double? = null,

	/** Amount of humidity in the air */
	val relative_humidity : Double? = null,

	/** Ultraviolet index, if sky is clear
	 *
	 * Not documented, but present in JSON
	 */
	val ultraviolet_index_clear_sky : Double? = null,

	/** The direction, frow which wind blows */
	val wind_from_direction : Double? = null,

	/** Speed of wind */
	val wind_speed : Double? = null,

	/** Speed of wind in gusts */
	val wind_speed_of_gust : Double? = null
)
