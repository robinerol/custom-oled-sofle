import hid.HidCommunicationManager
import hid.MessageBuilderService
import io.github.cdimascio.dotenv.dotenv
import stockmarket.FinnhubStockMarketService
import stockmarket.StockMarketService
import weather.MetWeatherForecastService
import weather.WeatherForecastService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

val dotenv = dotenv {
    directory = "./src/main/resources"
}

fun main() {
    startHidService(setupMessageBuilderService())
}

fun setupMessageBuilderService(): MessageBuilderService {
    return MessageBuilderService(
        getFromDotEnvOrThrow("stockSymbol"),
        getFromDotEnvOrThrow("latitude"),
        getFromDotEnvOrThrow("longitude"),
        setupStockMarketService(),
        setupWeatherForecastService()
    )
}

fun setupStockMarketService(): StockMarketService {
    return FinnhubStockMarketService(getFromDotEnvOrThrow("finnhubApiKey"))
}

fun setupWeatherForecastService(): WeatherForecastService {
    return MetWeatherForecastService.getInstance(getFromDotEnvOrThrow("metUserAgent"))
}

fun startHidService(messageBuilderService: MessageBuilderService) {
    Executors.newScheduledThreadPool(1).schedule(
        HidCommunicationManager(
            Integer.decode(getFromDotEnvOrThrow("vendorId")),
            Integer.decode(getFromDotEnvOrThrow("productId")),
            java.lang.Long.decode(getFromDotEnvOrThrow("usagePage")).toInt(),
            Integer.decode(getFromDotEnvOrThrow("usage")),
            getFromDotEnvOrThrow("receiverRefreshIntervalInMilliseconds").toLong(),
            getFromDotEnvOrThrow("senderRefreshIntervalInSeconds").toLong(),
            messageBuilderService
        ),
        0L,
        TimeUnit.SECONDS
    )
}

fun getFromDotEnvOrThrow(key: String): String {
    val value: String = dotenv.get(key)
    if (value.isEmpty()) {
        throw IllegalStateException("Please provide a $key in the .env file within the resources folder.")
    }
    return value
}
