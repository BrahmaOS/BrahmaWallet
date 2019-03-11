package io.brahmaos.wallet.brahmawallet.api;

import java.util.List;
import java.util.Map;

import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * brm pay api
 */
public interface PayApi {

    /**
     * Get pay request token
     */
    @POST("/pay/util/request/token")
    Observable<ApiRespResult> getPayRequestToken(@Body Map<String, Object> params);

    /**
     * Create quick pay account
     */
    @POST("/account/create")
    Observable<ApiRespResult> setQuickPayAccount(@Body Map<String, Object> params);

    /**
     * Create quick pay account credit pre order
     */
    @POST("/pay/create/deposit")
    Observable<ApiRespResult> createPayAccountPreCredit(@Body Map<String, Object> params);

    /**
     * Account credit order
     */
    @POST("/pay/payment/deposit")
    Observable<ApiRespResult> createaPaymentOrder(@Body Map<String, Object> params);

    /**
     * Get the latest version of the tokens
     */
    @GET("/v1/wallet/tokens/latest")
    Observable<ApiRespResult> getLatestTokensVersion(@Query("type") int type);

    /**
     * Get the account transactions.
     */
    @GET("/v1/eth/txs/all")
    Observable<ApiRespResult> getEthTransactions(@Query("acct") String accountAddress,
                                                 @Query("page") int page,
                                                 @Query("count") int count);

    /**
     * Get the account token transactions.
     */
    @GET("/v1/eth/tokens/{token_address}/txs/all")
    Observable<ApiRespResult> getTokenTransactions(@Path("token_address") String tokenAddress,
                                                   @Query("acct") String accountAddress,
                                                   @Query("page") int page,
                                                   @Query("count") int count);
}
