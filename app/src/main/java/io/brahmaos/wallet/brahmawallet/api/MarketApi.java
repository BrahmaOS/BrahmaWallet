package io.brahmaos.wallet.brahmawallet.api;

import java.util.List;

import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * coinmarketcap api
 */
public interface MarketApi {

    /**
     * Get the currency of tokens
     */
    @GET("/v1/eth/tokens/quotes/latest")
    Observable<ApiRespResult> getCryptoCurrencies(@Query("symbols") String symbols);

    /**
     * Get all tokens
     */
    @GET("/assets/erc20-tokens.json")
    Observable<List<List<Object>>> getAllTokens();

    /**
     * Get the currency of the specified tokens
     */
    @GET("/v1/ticker/{id}/")
    Observable<List<CryptoCurrency>> getCryptoCurrency(@Path("id") String id,
                                                       @Query("convert") String convertFormat);
}
