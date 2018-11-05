package io.brahmaos.wallet.util;

import android.util.Log;

import java.math.BigInteger;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class DataCryptoUtils {
    private static final String TAG = "DataCryptoUtils";

    /** Used to generate SecretKeySpec for AES 128 crypto **/
    private final static byte[] SALT = new byte[]{0x62, 0x72, 0x61, 0x68, 0x6d, 0x61, 0x6f, 0x73};//must be 8 bytes
    private final static int ITERATION_COUNT = 1024;
    private final static int KEY_STRENGTH = 128;
    private final static int DEFAULT_BLOCK_SIZE = 64;

    /**
     * The results returned by crypto APIs
     * */
    public static final int RESULT_CRYPTO_SUCCESS = 0;
    public static final int RESULT_CRYPTO_FAIL = 1;
    public static final int RESULT_PASSWORD_ERROR = 2;
    public static final int RESULT_FILE_ERROR= 3;

    /** Parameters for generating BCECPublicKey for crypto with Brahma OS default key pair **/
    private final BigInteger POINT_G_PRE =
            new BigInteger("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", 16);
    private final BigInteger POINT_G_POST =
            new BigInteger("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8", 16);
    private final BigInteger FACTOR_N =
            new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);
    private final BigInteger FIELD_P =
            new BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f", 16);

    public DataCryptoUtils () {
    }

    /**
     * AES 128 Encrypt
     *
     * @param content the clear text which want to be encrypted.
     * @param password the secret key
     *
     * @return the encrypted hex string, if null it means encrypt failed
     */
    public static String aes128Encrypt(String content, String password) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, genKey(password));
            byte[] result = cipher.doFinal(content.getBytes());
            return parseByte2HexStr(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * AES 128 Decrypt
     *
     * @param content the cipher text which want to be decrypted.
     * @param password the secret key
     *
     * @return the clear text hex string, if null it means decrypt failed
     */
    public static String aes128Decrypt(String content, String password) {
        try {
            byte[] decryptFrom = parseHexStr2Byte(content);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, genKey(password));
            byte[] result = cipher.doFinal(decryptFrom);
            return new String(result);
        } catch (Exception e) {
            Log.d(TAG, "" + e.toString());
        }
        return null;
    }

    /**
     * get SecretKeySpec according to the password using "PBKDF2WithHmacSHA1" algorithm
     * @return
     */
    private static SecretKeySpec genKey(String password){
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, ITERATION_COUNT, KEY_STRENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec key = new SecretKeySpec(tmp.getEncoded(), "AES");
            return key;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    private static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr == null || hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }
}
