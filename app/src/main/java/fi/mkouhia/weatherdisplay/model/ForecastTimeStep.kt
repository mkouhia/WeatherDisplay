package fi.mkouhia.weatherdisplay.model

import kotlinx.serialization.Serializable
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

@Serializable
data class ForecastTimeStep (
	val time : String,
	val data : Data
) {
	val zonedDateTime : ZonedDateTime by lazy {
		ZonedDateTime.parse(time)
	}
	val localDateTime : LocalDateTime by lazy {
		zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
	}
}

@Serializable
data class Data (
	val instant : ForecastTimeInstant,
	val next_1_hours : ForecastTimePeriod? = null,
	val next_6_hours : ForecastTimePeriod? = null
)