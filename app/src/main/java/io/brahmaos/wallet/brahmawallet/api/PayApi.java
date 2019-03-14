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
    Observable<ApiRespResult> rechargePreOrder(@Body Map<String, Object> params);

    /**
     * Account recharge order
     */
    @POST("/pay/payment/deposit")
    Observable<ApiRespResult> payAccountRecharge(@Body Map<String, Object> params);

    /**
     * Get account balance
     */
    @GET("/pay/balance/info")
    Observable<ApiRespResult> getAccountBalance();

    /**
     * Pay request order by merchant
     */
    @POST("/pay/request/order")
    Observable<ApiRespResult> payRequestOrder(@Body Map<String, Object> params);
}
