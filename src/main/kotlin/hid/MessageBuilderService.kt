package hid

import stockmarket.StockMarketService
import stockmarket.StockValue
import weather.Forecast
import weather.WeatherForecastService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessageBuilderService(
    private val stockSymbol: String,
    private val latitude: String,
    private val longitude: String,
    private val stockMarketService: StockMarketService,
    private val weatherForecastService: WeatherForecastService
) {
    private fun generateContentForLeftScreen(keyboardLayer: KeyboardLayer): String {
        val message: StringBuilder = StringBuilder()

        message.append("     ")
        message.append("Sofle")
        message.append("Choc ")
        message.append("     ")
        message.append("-----")
        message.append("     ")
        message.append("Layer")
        message.append(keyboardLayer.fiveCharacterLongName)
        message.append("     ")
        message.append("-----")
        message.append("     ")
        message.append(" ${LocalDate.now().format(DateTimeFormatter.ofPattern("EEE"))} ")
        message.append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM")))
        message.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
        message.append("     ")
        message.append("-----")

        return message.toString()
    }

    private fun generateContentForRightScreen(): String {
        val stockValue: StockValue = stockMarketService.fetchCurrentValueFor(stockSymbol)
        val weatherForecast: Forecast = weatherForecastService.fetchWeatherDataFor(latitude, longitude)
        val message: StringBuilder = StringBuilder()

        message.append("     ")
        message.append("${stockValue.symbol.uppercase()} ")
        message.append(getAsFiveCharacterString(stockValue.current))
        message.append(getAsFiveCharacterString(stockValue.change))
        message.append("     ")
        message.append("-----")
        message.append("     ")
        message.append("Temp ")
        message.append(getAsFiveCharacterString(weatherForecast.snapshots[0].airTemperature))
        message.append("     ")
        message.append("Cloud")
        message.append(getAsFiveCharacterString(weatherForecast.snapshots[0].cloudAreaFraction))
        message.append("     ")
        message.append("Rain ")
        message.append(getAsFiveCharacterString(weatherForecast.snapshots[0].precipitationAmount))
        message.append("-----")

        return message.toString()
    }

    fun assemble(keyboardLayer: KeyboardLayer): String {
        return generateContentForLeftScreen(keyboardLayer) + generateContentForRightScreen()
    }

    companion object {
        private fun getAsFiveCharacterString(value: Any): String {
            val valueAsString = value.toString()

            return if (valueAsString.length > 5) {
                valueAsString.substring(0, 5)
            } else if (valueAsString.length < 5) {
                String.format("%-5s", valueAsString)
            } else {
                valueAsString
            }
        }
    }
}
