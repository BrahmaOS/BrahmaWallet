package io.brahmaos.wallet.brahmawallet.service;

import android.content.Context;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.event.EventTypeDef;
import io.brahmaos.wallet.brahmawallet.model.BitcoinDownloadProgress;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.RxEventBus;

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

    public static int BYTES_PER_BTC_KB = 1000;
    public static int MIN_CONFIRM_BLOCK_HEIGHT = 6;
    private String CHECK_POINTS_NAME = "checkpoints_testnet";

    private Map<String, WalletAppKit> btcAccountKit = new HashMap<>();

    @Override
    public boolean init(Context context) {
        super.init(context);
        return true;
    }

    public NetworkParameters getNetworkParams() {
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

    private TransactionConfidenceEventListener txListener = new TransactionConfidenceEventListener() {
        @Override
        public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
            if (tx.getConfidence().getDepthInBlocks() <= MIN_CONFIRM_BLOCK_HEIGHT) {
                RxEventBus.get().post(EventTypeDef.BTC_TRANSACTION_CHANGE, tx);
            }
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
        kit.wallet().addTransactionConfidenceEventListener(txListener);
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
        InputStream ins = context.getResources().openRawResource(context.getResources().getIdentifier(CHECK_POINTS_NAME,
                "raw", context.getPackageName()));
        kit.setCheckpoints(ins);

        kit.setBlockingStartup(false);
        kit.startAsync();
        kit.awaitRunning();
        kit.wallet().addTransactionConfidenceEventListener(txListener);
        btcAccountKit.put(filePrefix, kit);
    }

    public WalletAppKit restoreWalletAppKit(String filePrefix, DeterministicSeed seed) {
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

        kit.setBlockingStartup(false);
        kit.startAsync();
        kit.awaitRunning();
        kit.wallet().addTransactionConfidenceEventListener(txListener);
        btcAccountKit.put(filePrefix, kit);
        return kit;
    }

    public WalletAppKit getBtcWalletAppKit(String filePrefix) {
        if (btcAccountKit.containsKey(filePrefix)) {
            return btcAccountKit.get(filePrefix);
        } else {
            return null;
        }
    }

    public boolean transfer(String receiveAddress, BigDecimal amount, long fee, String accountFilename) {
        try {
            WalletAppKit kit = btcAccountKit.get(accountFilename);
            Coin value = Coin.valueOf(CommonUtil.convertSatoshiFromBTC(amount).longValue());
            System.out.println("Forwarding " + value.toFriendlyString() + " BTC");
            Address to = Address.fromBase58(getNetworkParams(), receiveAddress);
            Transaction transaction = new Transaction(getNetworkParams());
            transaction.addOutput(value, to);

            SendRequest request = SendRequest.forTx(transaction);
            request.feePerKb = Coin.valueOf(fee);

            Wallet.SendResult sendResult = kit.wallet().sendCoins(kit.peerGroup(), request);

            ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

            sendResult.broadcastComplete.addListener(new Runnable() {
                @Override
                public void run() {
                    // The wallet has changed now, it'll get auto saved shortly or when the app shuts down.
                    System.out.println("Sent coins onwards! Transaction hash is " +
                            sendResult.tx.getHashAsString());
                    RxEventBus.get().post(EventTypeDef.BTC_TRANSACTION_BROADCAST_COMPLETE, true);
                }
            }, executorService);
            return true;
        } catch (Exception e) {
            e.fillInStackTrace();
            return false;
        }

    }

    public boolean isValidBtcAddress(String address) {
        try {
            Address.fromBase58(getNetworkParams(), address);
            return true;
        } catch (Exception e) {
            e.fillInStackTrace();
            return false;
        }
    }
}
