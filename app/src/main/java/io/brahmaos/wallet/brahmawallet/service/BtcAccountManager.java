package io.brahmaos.wallet.brahmawallet.service;

import android.content.Context;
import android.view.View;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import org.spongycastle.util.encoders.Hex;
import org.web3j.protocol.ObjectMapperFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.WalletApp;
import io.brahmaos.wallet.brahmawallet.api.ApiConst;
import io.brahmaos.wallet.brahmawallet.api.ApiRespResult;
import io.brahmaos.wallet.brahmawallet.api.Networks;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.AllTokenEntity;
import io.brahmaos.wallet.brahmawallet.event.EventTypeDef;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.model.BitcoinDownloadProgress;
import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;
import io.brahmaos.wallet.brahmawallet.model.KyberToken;
import io.brahmaos.wallet.brahmawallet.model.TokensVersionInfo;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.RxEventBus;
import rx.Completable;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BtcAccountManager extends BaseService{
    @Override
    protected String tag() {
        return BtcAccountManager.class.getName();
    }

    // singleton
    private static BtcAccountManager instance = new BtcAccountManager();
    public static BtcAccountManager getInstance() {
        return instance;
    }

    private Map<String, WalletAppKit> btcAccountKit = new HashMap<>();

    @Override
    public boolean init(Context context) {
        super.init(context);
        return true;
    }

    private NetworkParameters getNetworkParams() {
        return TestNet3Params.get();
    }

    private DownloadProgressTracker listener = new DownloadProgressTracker() {
        @Override
        public void onChainDownloadStarted(Peer peer, int blocksLeft) {
            super.onChainDownloadStarted(peer, blocksLeft);
            String tip = "the kit-----> start is" + peer.toString() + "------->:" + blocksLeft
                    + "thread id is:" + Thread.currentThread().getId();
            BLog.d(tag(), tip);
        }

        @Override
        protected void progress(double pct, int blocksSoFar, Date date) {
            String tip = String.format(Locale.US, "Chain download %d%% done with %d blocks to go, block date %s", (int) pct, blocksSoFar,
                    Utils.dateTimeFormat(date));
            System.out.println(tip);
            BitcoinDownloadProgress progress = new BitcoinDownloadProgress();
            progress.setProgressPercentage(pct);
            progress.setBlocksLeft(blocksSoFar);
            progress.setCurrentBlockDate(date);
            progress.setDownloaded(false);
            RxEventBus.get().post(EventTypeDef.BTC_ACCOUNT_SYNC, progress);
        }

        /**
         * Called when download is initiated.
         *
         * @param blocks the number of blocks to download, estimated
         */
        protected void startDownload(int blocks) {
            String tip = "the kit-----> " + "Downloading block chain of size " + blocks + ". " +
                    (blocks > 1000 ? "This may take a while." : "");
            BLog.d(tag(), tip);
            BitcoinDownloadProgress progress = new BitcoinDownloadProgress();
            progress.setBlocksLeft(blocks);
            progress.setDownloaded(false);
            RxEventBus.get().post(EventTypeDef.BTC_ACCOUNT_SYNC, progress);
        }

        /**
         * Called when we are done downloading the block chain.
         */
        protected void doneDownload() {
            BLog.d(tag(), "the kit-----> down loaded");
            BitcoinDownloadProgress progress = new BitcoinDownloadProgress();
            progress.setDownloaded(true);
            RxEventBus.get().post(EventTypeDef.BTC_ACCOUNT_SYNC, progress);
        }
    };

    public void initExistsWalletAppKit(AccountEntity accountEntity) {
        WalletAppKit kit = new WalletAppKit(getNetworkParams(), context.getFilesDir(),
                accountEntity.getFilename()) {
            @Override
            protected void onSetupCompleted() {
                // This is called in a background thread after startAndWait is called, as setting up various objects
                // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
                // on the main thread.
                System.out.println("the setup completed;");
                RxEventBus.get().post(EventTypeDef.BTC_APP_KIT_INIT_SET_UP, true);
            }
        };
        kit.setDownloadListener(listener);

        kit.setBlockingStartup(false);
        kit.startAsync();
        kit.awaitRunning();
        btcAccountKit.put(accountEntity.getFilename(), kit);
    }

    public void createWalletAppKit(String filePrefix, DeterministicSeed seed) {
        WalletAppKit kit = new WalletAppKit(getNetworkParams(), context.getFilesDir(), filePrefix) {
            @Override
            protected void onSetupCompleted() {
                // This is called in a background thread after startAndWait is called, as setting up various objects
                // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
                // on the main thread.
                System.out.println("the setup completed;");
            }
        };
        kit.restoreWalletFromSeed(seed);
        kit.setDownloadListener(listener);

        // set checkpoints
        InputStream ins = context.getResources().openRawResource(context.getResources().getIdentifier("checkpoints_testnet",
                "raw", context.getPackageName()));
        kit.setCheckpoints(ins);

        kit.setBlockingStartup(false);
        kit.startAsync();
        kit.awaitRunning();
        btcAccountKit.put(filePrefix, kit);
    }

    public void restoreWalletAppKit(String filePrefix, DeterministicSeed seed) {
        WalletAppKit kit = new WalletAppKit(getNetworkParams(), context.getFilesDir(), filePrefix){
            @Override
            protected void onSetupCompleted() {
                // This is called in a background thread after startAndWait is called, as setting up various objects
                // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
                // on the main thread.
                System.out.println("the setup completed;");
                //peerGroup().setFastCatchupTimeSecs(createTime);
            }
        };
        kit.restoreWalletFromSeed(seed);
        kit.setDownloadListener(listener);

        // set checkpoints
        InputStream ins = context.getResources().openRawResource(context.getResources().getIdentifier("checkpoints_testnet",
                "raw", context.getPackageName()));
        kit.setCheckpoints(ins);

        kit.setBlockingStartup(false);
        kit.startAsync();
        kit.awaitRunning();
        btcAccountKit.put(filePrefix, kit);
    }

    public WalletAppKit getBtcWalletAppKit(String filePrefix) {
        if (btcAccountKit.containsKey(filePrefix)) {
            return btcAccountKit.get(filePrefix);
        } else {
            return null;
        }
    }

    public void transfer(String receiveAddress, double amount, String accountFilename) {
        try {
            WalletAppKit kit = btcAccountKit.get(accountFilename);
            Coin value = Coin.valueOf(1000000);
            System.out.println("Forwarding " + value.toFriendlyString() + " BTC");
            // Now send the coins back! Send with a small fee attached to ensure rapid confirmation.
            final Coin amountToSend = value.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
            final Wallet.SendResult sendResult = kit.wallet().sendCoins(kit.peerGroup(),
                    new Address(getNetworkParams(), Hex.decode(receiveAddress)), amountToSend);
            System.out.println("Sending ...");
            // Register a callback that is invoked when the transaction has propagated across the network.
            // This shows a second style of registering ListenableFuture callbacks, it works when you don't
            // need access to the object the future returns.

            ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

            sendResult.broadcastComplete.addListener(new Runnable() {
                @Override
                public void run() {
                    // The wallet has changed now, it'll get auto saved shortly or when the app shuts down.
                    System.out.println("Sent coins onwards! Transaction hash is " +
                            sendResult.tx.getHashAsString());
                }
            }, executorService);
        } catch (Exception e) {
            e.fillInStackTrace();
        }

    }
}
