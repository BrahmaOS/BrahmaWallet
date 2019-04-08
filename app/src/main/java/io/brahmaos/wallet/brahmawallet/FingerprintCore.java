package io.brahmaos.wallet.brahmawallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class FingerprintCore {
    private KeyStore mKeyStore;
    private FingerprintManager mFpManager;
    private byte [] mIV;
    private SimpleAuthenticationCallback callback;
    private SharedPreferences mSharedPref;
    private CancellationSignal mCancellationSignal;
    private boolean isSelfCancelled;
    private static final String KEYSTORE_TYPE = "AndroidKeyStore";
    private static final String SHARED_PREFERENCE_NAME = "finger";
    private static final String SUFFIX_DATA = "_data";
    private static final String SUFFIX_IV = "_iv";


    public FingerprintCore(Context context) {
        mFpManager = context.getSystemService(FingerprintManager.class);
        try {
            // Register provider
            mKeyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        } catch (KeyStoreException e) {
        }
        mSharedPref = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public int checkFingerprintAvailable() {
        if (!mFpManager.isHardwareDetected()) {
            return -1;
        } else if (!mFpManager.hasEnrolledFingerprints()) {
            return 0;
        } else {
            return 1;
        }
    }

    public void clearTouchIDPay(String accuntAddr) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove(accuntAddr + SUFFIX_DATA);
        editor.remove(accuntAddr + SUFFIX_IV);
        editor.commit();
    }

    public void stopListening() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
        isSelfCancelled = true;
    }

    public void generateKey(String accountAddr) throws NoSuchProviderException,
            NoSuchAlgorithmException, IOException, CertificateException, InvalidAlgorithmParameterException {
        // AES + CBC + PKCS7
        final KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_TYPE);
        mKeyStore.load(null);
        generator.init(new KeyGenParameterSpec.Builder(accountAddr,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setUserAuthenticationRequired(true)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build());
        // Generate (symmetric) key, and store to KeyStore
        generator.generateKey();
    }

    public void encryptData(String accountAddr, String data) throws CertificateException, NoSuchAlgorithmException,
            IOException, UnrecoverableKeyException, KeyStoreException, NoSuchPaddingException, InvalidKeyException {
        mKeyStore.load(null);
        final SecretKey sk = (SecretKey) mKeyStore.getKey(accountAddr, null);
        if (null == sk) {
            if (callback != null) {
                callback.onAuthenticationError(1111, "Can not get key");
            }
            return;
        }

        final Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        cipher.init(Cipher.ENCRYPT_MODE, sk);

        // Need authenticate by fingerprint
        final FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
        mCancellationSignal = new CancellationSignal();
        isSelfCancelled = false;
        mFpManager.authenticate(cryptoObject, mCancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (callback != null && !isSelfCancelled) {
                    callback.onAuthenticationError(errorCode, errString);
                }
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                super.onAuthenticationHelp(helpCode, helpString);
                if (callback != null) {
                    callback.onAuthenticationHelp(helpCode, helpString);
                }
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (null == result.getCryptoObject()) {
                    if (callback != null) {
                        callback.onAuthenticationFail();
                        return;
                    }
                }

                // Encrypt data by cipher
                final Cipher cipher = result.getCryptoObject().getCipher();
                try {
                    byte [] encrypted = cipher.doFinal(data.getBytes());
                    mIV = cipher.getIV();
                    final String encryptString = Base64.encodeToString(encrypted, Base64.URL_SAFE);
                    final String ivString = Base64.encodeToString(mIV, Base64.URL_SAFE);
                    // Store the encrypted data into local.
                    SharedPreferences.Editor editor = mSharedPref.edit();
                    editor.putString(accountAddr + SUFFIX_DATA, encryptString);
                    editor.putString(accountAddr + SUFFIX_IV, ivString);
                    editor.commit();
                    if (callback != null) {
                        callback.onAuthenticationSucceeded(encryptString);
                    }
                } catch (IllegalBlockSizeException | BadPaddingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                if (callback != null) {
                    callback.onAuthenticationFail();
                }
            }
        }, new Handler());
    }

    public void decryptWithFingerprint(String accountAddr) throws CertificateException,
            NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyStoreException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        mKeyStore.load(null);
        final SecretKey sk = (SecretKey) mKeyStore.getKey(accountAddr, null);
        if (null == sk) {
            if (callback != null) {
                callback.onAuthenticationError(1111, "Can not get key");
            }
            return;
        }

        final Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        String ivString = mSharedPref.getString(accountAddr + SUFFIX_IV, "");
        cipher.init(Cipher.DECRYPT_MODE, sk, new IvParameterSpec(Base64.decode(ivString, Base64.URL_SAFE)));

        // First need authenticate by fingerprint
        final FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
        mCancellationSignal = new CancellationSignal();
        isSelfCancelled = false;
        mFpManager.authenticate(cryptoObject, mCancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (callback != null && !isSelfCancelled) {
                    callback.onAuthenticationError(errorCode, errString);
                }
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                super.onAuthenticationHelp(helpCode, helpString);
                if (callback != null) {
                    callback.onAuthenticationHelp(helpCode, helpString);
                }
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (null == result.getCryptoObject()) {
                    if (callback != null) {
                        callback.onAuthenticationFail();
                        return;
                    }
                }

                // Decrypt data by cipher
                final String encryptedWithBase64 = mSharedPref.getString(accountAddr + SUFFIX_DATA, "");
                final byte [] encryptedBytes = Base64.decode(encryptedWithBase64, Base64.URL_SAFE);
                final Cipher cipher = result.getCryptoObject().getCipher();
                try {
                    byte [] decryptedBytes = cipher.doFinal(encryptedBytes);
                    String decryptedText = new String(decryptedBytes);
                    if (callback != null) {
                        callback.onAuthenticationSucceeded(decryptedText);
                    }
                } catch (IllegalBlockSizeException | BadPaddingException e) {
                    if (callback != null) {
                        callback.onAuthenticationFail();
                    }
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                if (callback != null) {
                    callback.onAuthenticationFail();
                }
            }
        }, new Handler());
    }

    public void setCallback(SimpleAuthenticationCallback callback) {
        this.callback = callback;
    }

    public interface SimpleAuthenticationCallback {
        void onAuthenticationSucceeded(String data);
        void onAuthenticationError(int errorCode, CharSequence errString);
        void onAuthenticationHelp(int helpCode, CharSequence helpString);
        void onAuthenticationFail();
    }
}