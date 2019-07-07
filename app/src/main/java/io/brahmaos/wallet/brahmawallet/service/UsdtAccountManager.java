package io.brahmaos.wallet.brahmawallet.service;

import android.content.Context;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;

import java.math.BigDecimal;

import foundation.omni.rest.WalletAddressBalance;
import foundation.omni.rest.omniwallet.OmniwalletClient;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;

public class UsdtAccountManager extends BaseService{
    @Override
    protected String tag() {
        return UsdtAccountManager.class.getName();
    }

    // singleton
    private static UsdtAccountManager instance = new UsdtAccountManager();
    public static UsdtAccountManager getInstance() {
        return instance;
    }

    public static int BYTES_PER_BTC_KB = 1000;
    public static int MIN_CONFIRM_BLOCK_HEIGHT = 6;

    @Override
    public boolean init(Context context) {
        super.init(context);
        return true;
    }

    public NetworkParameters getNetworkParams() {
        if (BrahmaConfig.debugFlag) {
            return TestNet3Params.get();
        } else {
            return MainNetParams.get();
        }
    }

    public WalletAddressBalance balancesForAddress(String mainAddress) {
        LegacyAddress address = LegacyAddress.fromBase58(getNetworkParams(), mainAddress);
        try {
            return getUsdtWalletClient().balancesForAddress(address);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public OmniwalletClient getUsdtWalletClient() {
        return new OmniwalletClient();
    }

    public boolean transfer(String receiveAddress, BigDecimal amount, long fee, String accountFilename) {
        try {
            return true;
        } catch (Exception e) {
            e.fillInStackTrace();
            return false;
        }
    }

    public boolean isValidBtcAddress(String address) {
        try {
            Address.fromString(getNetworkParams(), address);
            return true;
        } catch (Exception e) {
            e.fillInStackTrace();
            return false;
        }
    }
}
