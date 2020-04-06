package fi.mkouhia.weatherdisplay

import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.StringRequest
import no.api.meteo.client.MeteoClientException
import no.api.meteo.client.MeteoResponse
import no.api.meteo.client.MeteoResponseHeader
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset


class MeteoRequest(
    method: Int,
    url: String,
    val listener : Response.Listener<MeteoResponse>,
    errorListener: Response.ErrorListener
) : Request<MeteoResponse>(method, url, errorListener) {

    override fun parseNetworkResponse(response: NetworkResponse?): Response<MeteoResponse> {
        if (response == null) {
            throw MeteoClientException("Network response is null")
        }
        return try {
            val responseString = String(
                response.data,
                Charset.forName(HttpHeaderParser.parseCharset(response.headers))
            )
            val responseHeaders = ArrayList<MeteoResponseHeader>()
            for ((k, v) in response.headers) {
                responseHeaders.add(MeteoResponseHeader(k, v))
            }
            val meteoResponse = MeteoResponse(responseString, responseHeaders, response.statusCode, response.statusCode.toString())
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
    override fun deliverResponse(response: MeteoResponse) = listener.onResponse(response)

}
