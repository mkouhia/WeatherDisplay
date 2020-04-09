package fi.mkouhia.weatherdisplay

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import fi.mkouhia.weatherdisplay.model.MetJsonForecast
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.json.JSONException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset


class MeteoRequest(
    method: Int,
    url: String,
    val listener : Response.Listener<MetJsonForecast>,
    errorListener: Response.ErrorListener
) : Request<MetJsonForecast>(method, url, errorListener) {

    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String>? {
        val params: MutableMap<String, String> = HashMap()
        params["User-Agent"] = "fi.mkouhia.weatherdisplay version 0.0 mkouhia@iki.fi"
        return params
    }

    /**
     * Subclasses must implement this to parse the raw network response and return an appropriate
     * response type. This method will be called from a worker thread. The response will not be
     * delivered if you return null.
     *
     * @param response Response from the network
     * @return The parsed response, or null in the case of an error
     */
    override fun parseNetworkResponse(response: NetworkResponse?): Response<MetJsonForecast>? {
        if (response == null) {
            return null
        }
        return try {
            println("Parsing meteo response to objects")

            val responseString = String(
                response.data,
                Charset.forName(HttpHeaderParser.parseCharset(response.headers))
            )
            val json = Json(configuration = JsonConfiguration.Stable.copy(prettyPrint = true))
            val meteoResponse = json.parse(MetJsonForecast.serializer(), responseString)

            Response.success(
                meteoResponse,
                HttpHeaderParser.parseCacheHeaders(response)
            )
        } catch (e: UnsupportedEncodingException) {
            Response.error(ParseError(e))
        } catch (je: JSONException) {
            Response.error(ParseError(je))
        }
    }

    /**
     * Subclasses must implement this to perform delivery of the parsed response to their listeners.
     * The given response is guaranteed to be non-null; responses that fail to parse are not
     * delivered.
     *
     * @param response The parsed response returned by [     ][.parseNetworkResponse]
     */
    override fun deliverResponse(response: MetJsonForecast) = listener.onResponse(response)

}
