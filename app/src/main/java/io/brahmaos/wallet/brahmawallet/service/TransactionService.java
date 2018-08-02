package io.brahmaos.wallet.brahmawallet.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.protocol.ObjectMapperFactory;

import java.io.IOException;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.api.ApiConst;
import io.brahmaos.wallet.brahmawallet.api.ApiRespResult;
import io.brahmaos.wallet.brahmawallet.api.Networks;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.model.EthTransaction;
import io.brahmaos.wallet.brahmawallet.model.TokenTransaction;
import io.brahmaos.wallet.util.BLog;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TransactionService extends BaseService {
    @Override
    protected String tag() {
        return TransactionService.class.getName();
    }

    // singleton
    private static TransactionService instance = new TransactionService();
    public static TransactionService getInstance() {
        return instance;
    }

    /*
     * Get eth transactions.
     */
    public Observable<List<EthTransaction>> getEthTransactions(String accountAddress, int page, int count) {
        return Observable.create(e -> {
            Networks.getInstance().getWalletApi()
                    .getEthTransactions(accountAddress.toLowerCase(), page, count)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ApiRespResult>() {

                        @Override
                        public void onCompleted() {
                            BLog.d(tag(), "get eth transactions");
                            e.onCompleted();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                            BLog.d(tag(), "get eth transactions error");
                            e.onError(throwable);
                        }

                        @Override
                        public void onNext(ApiRespResult apr) {
                            if (apr.getResult() == 0 && apr.getData().containsKey(ApiConst.PARAM_TRANSACTIONS)) {
                                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                                try {
                                    List<EthTransaction> ethTransactions = objectMapper.readValue(objectMapper.writeValueAsString(apr.getData().get(ApiConst.PARAM_TRANSACTIONS)), new TypeReference<List<EthTransaction>>() {});
                                    e.onNext(ethTransactions);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                    e.onError(e1);
                                }
                            } else {
                                BLog.e(tag(), "onError - " + apr.getResult());
                                e.onNext(null);
                            }
                        }
                    });
        });
    }

    /*
     * Get token transactions.
     */
    public Observable<List<TokenTransaction>> getTokenTransactions(String tokenAddress, String accountAddress,
                                                                   int page, int count) {
        return Observable.create(e -> {
            Networks.getInstance().getWalletApi()
                    .getTokenTransactions(tokenAddress, accountAddress, page, count)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ApiRespResult>() {

                        @Override
                        public void onCompleted() {
                            BLog.d(tag(), "get token transactions");
                            e.onCompleted();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                            BLog.d(tag(), "get token transactions error");
                            e.onError(throwable);
                        }

                        @Override
                        public void onNext(ApiRespResult apr) {
                            if (apr.getResult() == 0 && apr.getData().containsKey(ApiConst.PARAM_TRANSACTIONS)) {
                                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                                try {
                                    List<TokenTransaction> ethTransactions = objectMapper.readValue(objectMapper.writeValueAsString(apr.getData().get(ApiConst.PARAM_TRANSACTIONS)), new TypeReference<List<TokenTransaction>>() {});
                                    e.onNext(ethTransactions);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                    e.onError(e1);
                                }
                            } else {
                                BLog.e(tag(), "onError - " + apr.getResult());
                                e.onNext(null);
                            }
                        }
                    });
        });
    }
}
