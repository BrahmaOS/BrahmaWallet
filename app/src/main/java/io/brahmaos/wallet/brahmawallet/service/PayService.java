package io.brahmaos.wallet.brahmawallet.service;

import android.content.Context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.protocol.ObjectMapperFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import io.brahmaos.wallet.brahmawallet.WalletApp;
import io.brahmaos.wallet.brahmawallet.api.ApiRespResult;
import io.brahmaos.wallet.brahmawallet.api.Networks;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.ReqParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.AllTokenEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;
import io.brahmaos.wallet.brahmawallet.model.KyberToken;
import io.brahmaos.wallet.brahmawallet.model.pay.PayRequestToken;
import io.brahmaos.wallet.brahmawallet.repository.DataRepository;
import io.brahmaos.wallet.brahmawallet.statistic.network.StatisticHttpUtils;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import io.rayup.sdk.RayUpApp;
import io.rayup.sdk.model.CoinQuote;
import io.rayup.sdk.model.EthToken;
import rx.Completable;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PayService extends BaseService{
    @Override
    protected String tag() {
        return PayService.class.getName();
    }

    // singleton
    private static PayService instance = new PayService();
    public static PayService getInstance() {
        return instance;
    }

    @Override
    public boolean init(Context context) {
        super.init(context);
        this.context = context;
        return true;
    }

    public void checkPayRequestToken() {
        String payRequestToken = BrahmaConfig.getInstance().getPayRequestToken();
        if (payRequestToken == null) {
            getPayRequestToken();
        }
    }

    /*
     * Fetch pay request token.
     */
    public void getPayRequestToken() {
        Map<String, Object> params = new HashMap<>();
        params.put(ReqParam.PARAM_UD_ID, StatisticHttpUtils.getUDID(context));
        Networks.getInstance().getPayApi()
                .getPayRequestToken(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ApiRespResult>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onNext(ApiRespResult apr) {
                        if (apr != null && apr.getResult() == 0 && apr.getData() != null) {
                            BLog.i(tag(), apr.toString());
                            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                            try {
                                PayRequestToken payRequestToken = objectMapper.readValue(objectMapper.writeValueAsString(apr.getData()), new TypeReference<PayRequestToken>() {});
                                BrahmaConfig.getInstance().setPayRequestToken(payRequestToken.getAccessToken());
                                BrahmaConfig.getInstance().setPayRequestTokenType(payRequestToken.getTokenType());
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    /*
     * Create quick pay account
     */
    public Observable<String> createPayAccount(String address, int type,
                                                            String payPassword, String privateKey,
                                                            String publicKey) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> e) {
                String uri = "/account/create";
                TreeMap<String, Object> params = new TreeMap<>();
                params.put(ReqParam.PARAM_ACCOUNT_ADDRESS, address);
                params.put(ReqParam.PARAM_ACCOUNT_TYPE, type);
                params.put(ReqParam.PARAM_PUBLIC_KEY, publicKey);
                params.put(ReqParam.PARAM_PAY_PASSWORD, payPassword);
                params.put(ReqParam.PARAM_NONCE, CommonUtil.getNonce());
                params.put(ReqParam.PARAM_TIMESTAMP, CommonUtil.getCurrentSecondTimestamp());
                params.put(ReqParam.PARAM_SIGN_TYPE, BrahmaConst.PAY_REQUEST_SIGN_TYPE);
                String sign = CommonUtil.generateSignature(uri, params, privateKey);
                params.put(ReqParam.PARAM_SIGN, sign);

                Networks.getInstance().getPayApi()
                        .setQuickPayAccount(params)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<ApiRespResult>() {
                            @Override
                            public void onCompleted() {
                                e.onCompleted();
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                throwable.printStackTrace();
                                e.onError(throwable);
                            }

                            @Override
                            public void onNext(ApiRespResult apr) {
                                if (apr != null && apr.getResult() == 0 && apr.getData() != null) {
                                    BLog.i(tag(), apr.toString());
                                }
                                e.onNext(apr.toString());
                            }
                        });
            }
        });
    }

    // Get alltokenEntity
    public Observable<AllTokenEntity> queryAllTokenEntity(String address) {
        return Observable.create(e -> {
            AllTokenEntity allTokenEntity = ((WalletApp) context.getApplicationContext()).getRepository().queryAllTokenByAddress(address);
            e.onNext(allTokenEntity);
            e.onCompleted();
        });
    }
}
