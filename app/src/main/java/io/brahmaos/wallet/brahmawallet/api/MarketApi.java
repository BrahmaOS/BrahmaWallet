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
    @GET("/v1/ticker/")
    Observable<List<CryptoCurrency>> getCryptoCurrencies(@Query("start") int start,
                                                         @Query("limit") int limit,
                                                         @Query("convert") String convertFormat);

    /**
     * Get the currency of the specified tokens
     */
    @GET("/v1/ticker/{id}/")
    Observable<List<CryptoCurrency>> getCryptoCurrency(@Path("id") String id,
                                                       @Query("convert") String convertFormat);
}
