package weather

import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MetWeatherForecastService(private val userAgent: String) : WeatherForecastService {
    private var client: HttpClient = HttpClient.newHttpClient()
    private var forecast: Forecast? = null
    private var expires: ZonedDateTime = ZonedDateTime.now()

    override fun fetchWeatherDataFor(latitude: String, longitude: String): Forecast {
        if (isCachedDataStillUpToDate() && forecast != null) {
            return forecast as Forecast
        }

        val response = client.send(
            prepareRequest(latitude, longitude, expires, userAgent),
            HttpResponse.BodyHandlers.ofString()
        )
        expires = getInstantFromFormattedDate(response.headers().firstValue("expires")?.get())

        forecast = Forecast.fromHttpResponse(response)
        return forecast as Forecast
    }

    private fun isCachedDataStillUpToDate(): Boolean {
        return expires.plusSeconds(60 * 60).isAfter(ZonedDateTime.now())
    }

    private fun getInstantFromFormattedDate(expires: String?): ZonedDateTime {
        if (expires == null) {
            return ZonedDateTime.now()
        }

        return ZonedDateTime.parse(expires, apiFormatter)
    }

    companion object {
        private val apiFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z")

        private fun prepareRequest(
            latitude: String,
            longitude: String,
            expires: ZonedDateTime,
            userAgent: String
        ): HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=$latitude&lon=$longitude"))
            .setHeader("If-Modified-Since", expires.format(apiFormatter))
            .setHeader("User-Agent", userAgent)
            .build()

        @Volatile
        private var instance: MetWeatherForecastService? = null

        fun getInstance(userAgent: String) = instance ?: synchronized(this) {
            instance ?: MetWeatherForecastService(userAgent).also { instance = it }
        }
    }
}

data class MetSnapshot(
    override val time: String,
    override val airTemperature: Double,
    override val cloudAreaFraction: Int,
    override val precipitationAmount: Double
) : Snapshot {
    companion object {
        fun fromJson(jsonObject: JSONObject): List<Snapshot> {
            return jsonObject.optJSONObject("properties").optJSONArray("timeseries").map {
                it as JSONObject
                val details: JSONObject = it.optJSONObject("data").optJSONObject("instant").optJSONObject("details")
                val nextHour: JSONObject? = it.optJSONObject("data").optJSONObject("next_1_hours")
                val nextHourDetails: JSONObject? = nextHour?.getJSONObject("details")

                MetSnapshot(
                    time = it.getString("time"),
                    airTemperature = details.getDouble("air_temperature"),
                    cloudAreaFraction = details.getInt("cloud_area_fraction"),
                    precipitationAmount = nextHourDetails?.getDouble("precipitation_amount") ?: 0.0
                )
            }
        }
    }
}
