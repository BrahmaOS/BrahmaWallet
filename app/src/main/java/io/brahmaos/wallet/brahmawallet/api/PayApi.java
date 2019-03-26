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

    /**
     * Payment order
     */
    @POST("/pay/payment/order")
    Observable<ApiRespResult> paymentOrder(@Body Map<String, Object> params);

    /**
     * Pay orders
     */
    @GET("/pay/orders")
    Observable<ApiRespResult> getPayTransactions(@Query("order_type") int orderType,
                                               @Query("order_status") int orderStatus,
                                               @Query("start_time") String startTime,
                                               @Query("end_time") String endTime,
                                               @Query("page") int page,
                                               @Query("count") int count);

    /**
     * Get withdraw config
     */
    @GET("/pay/config/withdraw")
    Observable<ApiRespResult> getWithdrawConfig();

    /**
     * Account withdraw
     */
    @POST("/pay/create/withdraw")
    Observable<ApiRespResult> quickAccountWithdraw(@Body Map<String, Object> params);

    /**
     * Account transfer
     */
    @POST("/pay/create/transfer")
    Observable<ApiRespResult> quickAccountTransfer(@Body Map<String, Object> params);
}
