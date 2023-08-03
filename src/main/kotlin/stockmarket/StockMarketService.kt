package stockmarket

interface StockMarketService {
    fun fetchCurrentValueFor(symbol: String): StockValue
}

data class StockValue(val symbol: String, val current: Float, val change: Float)
