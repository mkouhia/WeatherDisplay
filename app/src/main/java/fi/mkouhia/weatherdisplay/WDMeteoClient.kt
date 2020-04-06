package fi.mkouhia.weatherdisplay

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import no.api.meteo.client.MeteoClient
import no.api.meteo.client.MeteoClientException
import no.api.meteo.client.MeteoResponse
import no.api.meteo.service.AbstractMeteoDataParser
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.concurrent.ExecutionException


/**
 * Meteo Client Interface.
 *
 * A client is responsible for fetching the raw data from the MET api.
 */
class WDMeteoClient(context : Context) : MeteoClient {

//    private var defaultRequestConfig: RequestConfig? = null
    private var metDomain = "api.met.no"
//    private var userAgent: String? = null
    private var timeout = 2000
    var requestQueue: RequestQueue = Volley.newRequestQueue(context)


    /**
     * Fetch a MET resource from the given uri.
     *
     * @param uri The MET API uri.
     * @return Response object containing the MET data.
     * @throws MeteoClientException If invalid url or content couldn't be fetched.
     */
    override fun fetchContent(uri: URI): MeteoResponse {
        log.debug("Going to fetch content from : %", uri.toString());

        val future : RequestFuture<MeteoResponse> = RequestFuture.newFuture();
        val request = MeteoRequest(Request.Method.GET, uri.toString(), future, future);
        requestQueue.add(request);

        return try {
            future.get()
        } catch (e: InterruptedException) {
            throw MeteoClientException("Interrupted", e)
        } catch (e: ExecutionException) {
            throw MeteoClientException("Exception encountered", e)
        }
    }

    /**
     * Set proxy settings for the client.
     *
     * @param hostName The proxy hostname to be used.
     * @param port The proxy port to be used.
     */
    override fun setProxy(hostname: String, port: Int) {
        TODO("Not yet implemented")
    }

    /**
     * Lets you override the default api.met.no domain.
     * Some customers gets their own domain.
     *
     * @param metDomain The domain to use for fetching data from the MET api. Eg. 1234api.met.no
     */
    override fun setMetDomain(metDomain: String) {
        this.metDomain = metDomain
    }
    /**
     * The domain to be used when fetching data from the MET API.
     *
     * @return The domain as string.
     */
    override fun getMetDomain(): String {
        return metDomain
    }

    /**
     * Set the connection timeout to be used when fetching data from the MET API.
     *
     * @param timeout The timeout in seconds.
     */
    override fun setTimeout(timeout: Int) {
        this.timeout = timeout
    }

    /**
     * When HttpClient instance is no longer needed, shut down the client to ensure immediate deallocation
     * of all system resources.
     */
    override fun shutdown() {
        requestQueue.stop()
    }

    companion object {
        private val log =
            LoggerFactory.getLogger(WDMeteoClient::class.java)
    }

}