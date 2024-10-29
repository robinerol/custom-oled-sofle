package weather

import org.json.JSONObject
import java.net.http.HttpResponse
import java.time.Instant

interface WeatherForecastService {
    fun fetchWeatherDataFor(latitude: String, longitude: String): Forecast
}

data class Forecast(val updatedAt: Instant, val snapshots: List<Snapshot>) {
    companion object {
        fun fromHttpResponse(httpResponse: HttpResponse<String>): Forecast {
            return Forecast(Instant.now(), MetSnapshot.fromJson(JSONObject(httpResponse.body())))
        }
    }
}

interface Snapshot {
    val time: String
    val airTemperature: Double
    val cloudAreaFraction: Int
    val precipitationAmount: Double
}
