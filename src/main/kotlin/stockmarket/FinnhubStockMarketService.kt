package stockmarket

import io.finnhub.api.apis.DefaultApi
import io.finnhub.api.infrastructure.ApiClient

class FinnhubStockMarketService(apiKey: String) : StockMarketService {
    private val client: DefaultApi

    init {
        ApiClient.apiKey["token"] = apiKey
        client = DefaultApi()
    }

    override fun fetchCurrentValueFor(symbol: String): StockValue {
        val quote = client.quote(symbol)
        return StockValue(symbol, quote.c ?: 0F, quote.dp ?: 0F)
    }
}
