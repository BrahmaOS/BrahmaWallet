package io.brahmaos.wallet.brahmawallet.api;

import org.json.JSONArray;

import java.util.List;

import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * ipfs api
 */
public interface IpfsApi {

    @GET("/ipfs/{hash}")
    Observable<List<List<Object>>> getIpfsInfo(@Path("hash") String hash);
}
