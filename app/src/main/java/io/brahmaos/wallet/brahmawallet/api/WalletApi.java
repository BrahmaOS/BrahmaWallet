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
public interface WalletApi {

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

    /**
     * Get the latest version of the wallet
     */
    @GET("/v1/wallet/versions/latest")
    Observable<ApiRespResult> getLatestVersion(@Query("app") int appId,
                                               @Query("os") int osId);

    /**
     * Get the latest version of the tokens
     */
    @GET("/v1/wallet/tokens/latest")
    Observable<ApiRespResult> getLatestTokensVersion(@Query("type") int type);
}
