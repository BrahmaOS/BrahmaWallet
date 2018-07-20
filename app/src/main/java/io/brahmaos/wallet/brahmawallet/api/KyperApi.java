package io.brahmaos.wallet.brahmawallet.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * kyber api
 */
public interface KyperApi {

    @GET("/api/tokens/pairs")
    Observable<LinkedHashMap<String, Object>> getKyberPairsTokens();
}
