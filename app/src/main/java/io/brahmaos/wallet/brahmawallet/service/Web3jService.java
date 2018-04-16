package io.brahmaos.wallet.brahmawallet.service;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class Web3jService {
    protected String tag() {
        return Web3jService.class.getName();
    }

    // singleton
    private static Web3jService instance = new Web3jService();
    public static Web3jService getInstance() {
        return instance;
    }

    public String generateLightNewWalletFile(String password, File destinationDirectory)
            throws CipherException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException, IOException {
        return WalletUtils.generateLightNewWalletFile(password, destinationDirectory);
    }

    public String getWalletAddress(String password, String filePath)
            throws IOException, CipherException {
        Credentials credentials = WalletUtils.loadCredentials(
                password, filePath);
        return credentials.getAddress();
    }
}
