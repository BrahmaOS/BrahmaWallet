package io.brahmaos.wallet.brahmawallet.service;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.protocol.ObjectMapperFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.brahmaos.wallet.brahmawallet.api.ApiConst;
import io.brahmaos.wallet.brahmawallet.api.ApiRespResult;
import io.brahmaos.wallet.brahmawallet.api.Networks;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.ReqParam;
import io.brahmaos.wallet.brahmawallet.model.pay.AccountBalance;
import io.brahmaos.wallet.brahmawallet.model.pay.PayTransaction;
import io.brahmaos.wallet.brahmawallet.model.pay.MerchantReceiver;
import io.brahmaos.wallet.brahmawallet.model.pay.PayRequestToken;
import io.brahmaos.wallet.brahmawallet.statistic.network.StatisticHttpUtils;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
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

    private List<AccountBalance> accountBalances = new ArrayList<>();

    @Override
    public boolean init(Context context) {
        super.init(context);
        this.context = context;
        return true;
    }

    public List<AccountBalance> getAccountBalances() {
        return accountBalances;
    }

    public void setAccountBalances(List<AccountBalance> accountBalances) {
        this.accountBalances = accountBalances;
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

    private Observable<ApiRespResult> callMethod(String methodName, Map params) {
        Class<?> cls = PayService.class;
        try {
            Method method = cls.getMethod(methodName, Map.class);
            return (Observable<ApiRespResult>) method.invoke(PayService.getInstance(), params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Fetch pay request token.
     */
    public Observable<ApiRespResult> getPayTokenForQuickPay(String methodName, Map params) {
        return Observable.create(e -> {
            Map<String, Object> tokenParams = new HashMap<>();
            tokenParams.put(ReqParam.PARAM_UD_ID, StatisticHttpUtils.getUDID(context));
            Networks.getInstance().getPayApi()
                    .getPayRequestToken(tokenParams)
                    .flatMap((Func1<ApiRespResult, Observable<Boolean>>) apr -> {
                        if (apr != null && apr.getResult() == 0 && apr.getData() != null) {
                            BLog.i(tag(), apr.toString());
                            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                            try {
                                PayRequestToken payRequestToken = objectMapper.readValue(objectMapper.writeValueAsString(apr.getData()), new TypeReference<PayRequestToken>() {});
                                BrahmaConfig.getInstance().setPayRequestToken(payRequestToken.getAccessToken());
                                BrahmaConfig.getInstance().setPayRequestTokenType(payRequestToken.getTokenType());
                                return Observable.from(new Boolean[]{Boolean.TRUE});
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        return Observable.from(new Boolean[]{Boolean.FALSE});
                    })
                    .flatMap((Func1<Boolean, Observable<ApiRespResult>>) apr -> {
                        if (apr != null && apr) {
                            return callMethod(methodName, params);
                        }
                        return Observable.from((ApiRespResult[]) null);
                    })
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
                        public void onNext(ApiRespResult apiRespResult) {
                            e.onNext(apiRespResult);
                        }
                    });
        });
    }

    /*
     * Create quick pay account
     */
    public Observable<String> createPayAccount(String address, int type,
                                                            String payPassword, String privateKey,
                                                            String publicKey) {
        return Observable.create(e -> {
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

            createPayAccountByNet(params)
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
                            if (apr != null) {
                                if (apr.getResult() == 0) {
                                    BLog.i(tag(), apr.toString());
                                    BrahmaConfig.getInstance().setPayAccount(address);
                                    e.onNext(address);
                                } else if (apr.getResult() == ApiConst.INVALID_TOKEN) {
                                    getPayTokenForQuickPay("createPayAccountByNet", params)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Observer<ApiRespResult>() {
                                                @Override
                                                public void onNext(ApiRespResult apiRespResult) {
                                                    if (apiRespResult != null && apiRespResult.getResult() == 0) {
                                                        BrahmaConfig.getInstance().setPayAccount(address);
                                                        e.onNext(address);
                                                    } else {
                                                        e.onNext(null);
                                                    }
                                                }

                                                @Override
                                                public void onError(Throwable error) {
                                                    error.printStackTrace();
                                                }

                                                @Override
                                                public void onCompleted() {

                                                }
                                            });
                                } else {
                                    e.onNext(null);
                                }
                            } else {
                                e.onNext(null);
                            }
                        }
                    });
        });
    }

    public Observable<ApiRespResult> createPayAccountByNet(Map<String, Object> params) {
        return Networks.getInstance().getPayApi().setQuickPayAccount(params);
    }

    /*
     * Create credit pre order.
     */
    public Observable<Map> createRechargePreOrder(String address, int coinCode,
                                                   String amount, String remark) {
        return Observable.create(e -> {
            TreeMap<String, Object> params = new TreeMap<>();
            params.put(ReqParam.PARAM_SENDER, address);
            params.put(ReqParam.PARAM_COIN_CODE, coinCode);
            params.put(ReqParam.PARAM_AMOUNT, amount);
            params.put(ReqParam.PARAM_REMARK, remark);

            createRechargePreOrderByNet(params)
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
                            if (apr != null) {
                                if (apr.getResult() == 0 && apr.getData() != null) {
                                    BLog.i(tag(), apr.toString());
                                    Map result = apr.getData();
                                    Map<String, Object> orderInfo = new HashMap<>();
                                    orderInfo.put(ReqParam.PARAM_ORDER_ID, result.get(ReqParam.PARAM_ORDER_ID));
                                    if (result.containsKey(ReqParam.PARAM_RECEIVER_INFO) &&
                                            ((Map)result.get(ReqParam.PARAM_RECEIVER_INFO)).containsKey(ReqParam.PARAM_RECEIVER)) {
                                        orderInfo.put(ReqParam.PARAM_RECEIVER, ((Map)result.get(ReqParam.PARAM_RECEIVER_INFO)).get(ReqParam.PARAM_RECEIVER));
                                    }
                                    e.onNext(orderInfo);
                                } else if (apr.getResult() == ApiConst.INVALID_TOKEN) {
                                    getPayTokenForQuickPay("createRechargePreOrderByNet", params)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Observer<ApiRespResult>() {
                                                @Override
                                                public void onNext(ApiRespResult apiRespResult) {
                                                    if (apr.getResult() == 0 && apr.getData() != null) {
                                                        BLog.i(tag(), apr.toString());
                                                        Map result = apr.getData();
                                                        Map<String, Object> orderInfo = new HashMap<>();
                                                        orderInfo.put(ReqParam.PARAM_ORDER_ID, result.get(ReqParam.PARAM_ORDER_ID));
                                                        if (result.containsKey(ReqParam.PARAM_RECEIVER_INFO) &&
                                                                ((Map)result.get(ReqParam.PARAM_RECEIVER_INFO)).containsKey(ReqParam.PARAM_RECEIVER)) {
                                                            orderInfo.put(ReqParam.PARAM_RECEIVER, ((Map)result.get(ReqParam.PARAM_RECEIVER_INFO)).get(ReqParam.PARAM_RECEIVER));
                                                        }
                                                        e.onNext(orderInfo);
                                                    } else {
                                                        e.onNext(null);
                                                    }
                                                }

                                                @Override
                                                public void onError(Throwable error) {
                                                    error.printStackTrace();
                                                }

                                                @Override
                                                public void onCompleted() {

                                                }
                                            });
                                } else {
                                    e.onNext(null);
                                }
                            } else {
                                e.onNext(null);
                            }
                        }
                    });
        });
    }

    public Observable<ApiRespResult> createRechargePreOrderByNet(Map<String, Object> params) {
        return Networks.getInstance().getPayApi().rechargePreOrder(params);
    }

    /*
     * Recharge quick payment account.
     */
    public Observable<ApiRespResult> rechargeOrder(String orderId, String txHash) {
        return Observable.create(e -> {
            Map<String, Object> params = new HashMap<>();
            params.put(ReqParam.PARAM_ORDER_ID, orderId);
            params.put(ReqParam.PARAM_TX_HASH, txHash);

            rechargeOrderByNet(params)
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
                            if (apr != null) {
                                if (apr.getResult() == ApiConst.INVALID_TOKEN) {
                                    getPayTokenForQuickPay("rechargeOrderByNet", params)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Observer<ApiRespResult>() {
                                                @Override
                                                public void onNext(ApiRespResult apiRespResult) {
                                                    e.onNext(apiRespResult);
                                                }

                                                @Override
                                                public void onError(Throwable error) {
                                                    error.printStackTrace();
                                                }

                                                @Override
                                                public void onCompleted() {

                                                }
                                            });
                                } else {
                                    e.onNext(apr);
                                }
                            } else {
                                e.onNext(null);
                            }
                        }
                    });
        });
    }

    public Observable<ApiRespResult> rechargeOrderByNet(Map<String, Object> params) {
        return Networks.getInstance().getPayApi().payAccountRecharge(params);
    }

    /*
     * Get account balance.
     */
    public Observable<List<AccountBalance>> getAccountBalance() {
        return Observable.create(e -> {
            getAccountBalanceByNet()
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
                            if (apr != null) {
                                if (apr.getResult() == ApiConst.INVALID_TOKEN) {
                                    getPayTokenForQuickPay("getAccountBalanceByNet", null)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Observer<ApiRespResult>() {
                                                @Override
                                                public void onNext(ApiRespResult apiRespResult) {
                                                    if (apiRespResult.getData() != null && apiRespResult.getData().containsKey("balance_info")
                                                            && apiRespResult.getData().get("balance_info") != null){
                                                        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                                                        try {
                                                            ArrayList<AccountBalance> accountBalances = objectMapper.readValue(objectMapper.writeValueAsString(apiRespResult.getData().get("balance_info")), new TypeReference<List<AccountBalance>>() {});
                                                            for (AccountBalance balance : accountBalances) {
                                                                if (balance.getBalance() != null && balance.getBalance().length() > 0) {
                                                                    BigDecimal coinBalance = new BigDecimal(balance.getBalance());
                                                                    balance.setBalance(String.valueOf(coinBalance.setScale(4, BigDecimal.ROUND_HALF_UP)));
                                                                }
                                                            }
                                                            setAccountBalances(accountBalances);
                                                            e.onNext(accountBalances);
                                                        } catch (IOException e1) {
                                                            e1.printStackTrace();
                                                            e.onError(e1);
                                                        }
                                                    } else {
                                                        e.onNext(null);
                                                    }
                                                }

                                                @Override
                                                public void onError(Throwable error) {
                                                    error.printStackTrace();
                                                }

                                                @Override
                                                public void onCompleted() {

                                                }
                                            });
                                } else if (apr.getData() != null && apr.getData().containsKey("balance_info")
                                        && apr.getData().get("balance_info") != null){
                                    BLog.i(tag(), apr.toString());
                                    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                                    try {
                                        ArrayList<AccountBalance> accountBalances = objectMapper.readValue(objectMapper.writeValueAsString(apr.getData().get("balance_info")), new TypeReference<List<AccountBalance>>() {});
                                        for (AccountBalance balance : accountBalances) {
                                            if (balance.getBalance() != null && balance.getBalance().length() > 0) {
                                                BigDecimal coinBalance = new BigDecimal(balance.getBalance());
                                                balance.setBalance(String.valueOf(coinBalance.setScale(4, BigDecimal.ROUND_HALF_UP)));
                                            }
                                        }
                                        setAccountBalances(accountBalances);
                                        e.onNext(accountBalances);
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                        e.onError(e1);
                                    }
                                } else {
                                    e.onNext(null);
                                }
                            } else {
                                e.onNext(null);
                            }
                        }
                    });
        });
    }

    public Observable<ApiRespResult> getAccountBalanceByNet() {
        return Networks.getInstance().getPayApi().getAccountBalance();
    }

    /**
     * Pay request order
     */
    public Observable<MerchantReceiver> payRequestOrder(Map<String, Object> params) {
        return Observable.create(e -> {
            Networks.getInstance().getPayApi()
                    .payRequestOrder(params)
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
                                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                                try {
                                    MerchantReceiver merchantReceiver = objectMapper.readValue(objectMapper.writeValueAsString(apr.getData()), new TypeReference<MerchantReceiver>() {});
                                    e.onNext(merchantReceiver);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                    e.onError(e1);
                                }
                            } else {
                                e.onNext(null);
                            }
                        }
                    });
        });
    }

    /**
     * Quick payment order
     */
    public Observable<ApiRespResult> paymentOrder(Map<String, Object> params) {
        return Observable.create(e -> {
            paymentOrderByNet(params)
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
                            if (apr != null) {
                                if (apr.getResult() == ApiConst.INVALID_TOKEN) {
                                    getPayTokenForQuickPay("paymentOrderByNet", params)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Observer<ApiRespResult>() {
                                                @Override
                                                public void onNext(ApiRespResult apiRespResult) {
                                                    e.onNext(apiRespResult);
                                                }

                                                @Override
                                                public void onError(Throwable error) {
                                                    error.printStackTrace();
                                                }

                                                @Override
                                                public void onCompleted() {

                                                }
                                            });
                                } else {
                                    e.onNext(apr);
                                }
                            } else {
                                e.onNext(null);
                            }
                        }
                    });
        });
    }

    public Observable<ApiRespResult> paymentOrderByNet(Map<String, Object> params) {
        return Networks.getInstance().getPayApi().paymentOrder(params);
    }

    /**
     * Get account order list.
     * @param orderType 0 is for all orders
     * @param orderStatus 0 is for all order states
     * @param startTime get order from the start time, format: 2019-02-18 12:00:00
     * @param endTime get order to the end time, format: 2019-02-18 12:00:00
     * @param page start from 0 and the default is 0
     * @param count order count for each page, and the default is 10
     * @return count of order list
     */
    public Observable<List<PayTransaction>> getPayTransactions(int orderType, int orderStatus,
                                                            String startTime, String endTime,
                                                            int page, int count) {
        return Observable.create(e -> {
            Networks.getInstance().getPayApi()
                    .getPayTransactions(orderType, orderStatus, startTime, endTime, page, count)
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
                            BLog.i(tag(), apr.toString());
                            if (apr.getData() != null && apr.getData().containsKey("orders")
                                    && apr.getData().get("orders") != null){
                                Log.d(tag(), "" + apr);
                                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                                try {
                                    ArrayList<PayTransaction> payTransactions = objectMapper.readValue(
                                            objectMapper.writeValueAsString(apr.getData().get("orders")),
                                            new TypeReference<List<PayTransaction>>() {});
                                    e.onNext(payTransactions);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                    e.onError(e1);
                                }
                            } else {
                                e.onNext(null);
                            }
                        }
                    });
        });
    }

}
