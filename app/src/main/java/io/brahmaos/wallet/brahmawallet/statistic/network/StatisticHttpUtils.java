package io.brahmaos.wallet.brahmawallet.statistic.network;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.statistic.database.StatisticDatabaseHelper;
import io.brahmaos.wallet.brahmawallet.statistic.database.StatisticEventBean;
import io.brahmaos.wallet.brahmawallet.statistic.utils.StatisticLog;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class StatisticHttpUtils {
    private static final String TAG = "StatisticHttpUtils";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String STATISTIC_LOG_URL_DEV = "http://api.dev.brahmaos.io/v1/log";
    private static final String APPKEY_DEV = "66d087f7ca5a88763a2f8f3909672861d84e1440";

    private static final String STATISTIC_LOG_URL_PRODUCTION = "https://api.brahmaos.io/v1/log";
    private static final String APPKEY_PRODUCTION = "41c399c01843bb97a4022063668abf7c55336292";

    // for "device"
    private static final String DEVICE = "device";
    private static final String APP_KEY = "appkey";
    private static final String APP_VER = "app_ver";
    private static final String NET = "net";
    private static final String SYS_VER = "sys_ver";
    private static final String MODEL = "model";
    private static final String UDID = "udid";
//    private static final String LOC = "loc";
//    private static final String LAT = "lat";
//    private static final String LNG = "lng";

    // for events
    private static final String EVENTS = "events";
    //    private static final String T = "t";//is must
    private static final String TYPE = "type";//is must
    private static final String COMP_NAME = "comp_name";//is must
    private static final String COMP_ID = "comp_id";

    // for crashes
    private static final String CRASHS = "crashs";
    private static final String LOG = "log";
    private static final String T = "t";

    public static void uploadStatisticEvents (Context context, StatisticDatabaseHelper db) {
        if (!checkWiFiConnected(context)) {
            return;
        }

        // Check whether have events data
        ArrayList<StatisticEventBean> eventList = db.getStatisticEventList();
        if (eventList == null && eventList.size() <= 0) {
            return;
        }

        JSONArray jsonArray = new JSONArray();
        // Get events list info from DB.
        for (int i = 0; i < eventList.size(); i++) {
            StatisticEventBean event = eventList.get(i);
            // If have no must info, skip this event data.
            if (event.getTime() <= 0 || null == event.getType() || event.getType().isEmpty() ||
                    null == event.getCompName() || event.getCompName().isEmpty()) {
                continue;
            }
            try {
                JSONObject object = new JSONObject();
                object.put(T, event.getTime());
                object.put(TYPE, event.getType());
                object.put(COMP_NAME, event.getCompName());
                if (event.getCompId() != null && !event.getCompId().isEmpty()) {
                    object.put(COMP_ID, event.getCompId());
                }
                jsonArray.put(object);
            } catch (Exception e) {
                StatisticLog.LogE(TAG, i + "----" + e.toString());
            }
        }
        JSONObject finalObj = new JSONObject();
        try {
            finalObj.put(DEVICE, getDeviceJson(context));
            finalObj.put(EVENTS, jsonArray);
        } catch (Exception e) {
            StatisticLog.LogE(TAG, "Format events json string error: " + e.toString());
        }
        StatisticLog.LogD(TAG, "" + finalObj.toString());

        RequestBody requestBody = RequestBody.create(JSON, finalObj.toString());
        sendOkHttpResponse(getStatisticUrl() + "/events", requestBody, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final String res = response.body().string();
                    StatisticLog.LogD(TAG, res);
                    JSONObject obj = new JSONObject(res);
                    JSONObject status = obj.getJSONObject("status");
                    int error_code = status.getInt("error_code");
                    if (0 == error_code) {
                        db.deleteStatisticEvents(/*eventsCount*/);
                    }
                } catch (Exception e) {
                    StatisticLog.LogE(TAG, "[JSON] Failed to parse response: " + e.toString());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    public static void uploadCrashExceptions(Context context, String dirPath) {
        // Only upload by WIFI.
        if (!checkWiFiConnected(context)) {
            return;
        }
        // Check whether have crash log files.
        if (null == dirPath || dirPath.isEmpty() || null == context) {
            return;
        }
        File dir = new File(dirPath);
        if (!dir.exists()) {
            return;
        }
        File[] list = dir.listFiles();
        if (null == list || list.length <= 0) {
            return;
        }

        // Get crash list.
        JSONArray jsonArray = new JSONArray();
        for (File file : list) {//file name is time
            try {
                JSONObject jObject = new JSONObject();
                BufferedReader bfr = new BufferedReader(new FileReader(file));
                String line = bfr.readLine();
                StringBuilder sb = new StringBuilder();
                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = bfr.readLine();
                }
                bfr.close();
                jObject.put(LOG, sb.toString());
                jObject.put(T, Long.parseLong(file.getName()));
                jsonArray.put(jObject);
            } catch (Exception e) {
                StatisticLog.LogE(TAG, "failed to add " + file.getName());
            }
        }

        // Format upload logs string.
        JSONObject finalJson = new JSONObject();
        try {
            finalJson.put(DEVICE, getDeviceJson(context));
            finalJson.put(CRASHS, jsonArray);
        } catch (Exception e) {
            StatisticLog.LogE(TAG, "failed to format crashes json: " + e.toString());
        }
        StatisticLog.LogD(TAG, "" + finalJson.toString());

        // Send logs string to server.
        RequestBody requestBody = RequestBody.create(JSON, finalJson.toString());
        sendOkHttpResponse(getStatisticUrl() + "/crashs", requestBody, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                try {
                    final String res = response.body().string();
                    StatisticLog.LogD(TAG, res);
                    JSONObject obj = new JSONObject(res);
                    JSONObject status = obj.getJSONObject("status");
                    int error_code = status.getInt("error_code");
                    if (0 == error_code) {
                        for (File file : list) {
                            try {
                                if (file.exists()) {
                                    file.delete();
                                }
                            } catch (Exception ef) {
                            }
                        }
                    }
                } catch (Exception e) {
                    StatisticLog.LogE(TAG, "[JSON] Failed to parse response: " + e.toString());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    private static boolean checkWiFiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null
                    && networkInfo.getType() == ConnectivityManager.TYPE_WIFI
                    && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }
        StatisticLog.LogD(TAG, "WIFI not connected.");
        return false;
    }

    private static String getAppKey() {
        if (BrahmaConfig.debugFlag) {
            return APPKEY_DEV;
        } else {
            return APPKEY_PRODUCTION;
        }
    }

    private static JSONObject getDeviceJson(Context context) {
        WifiManager wifiManager =
                (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        JSONObject devObj = new JSONObject();
        // Get dev info
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            devObj.put(APP_KEY, getAppKey());
            devObj.put(APP_VER, /*pi.versionName + "_" + */pi.versionCode);
            if (!wifiManager.isWifiEnabled()) {
                devObj.put(NET, "MOBILE");
            } else {
                devObj.put(NET, "WIFI");
            }
            devObj.put(SYS_VER, Build.VERSION.RELEASE/* + "_" + Build.VERSION.SDK_INT*/);
            devObj.put(MODEL, Build.MODEL);
            devObj.put(UDID, getUDID(context));
        } catch (Exception e) {
            StatisticLog.LogE(TAG, "Failed to get dev info: " + e.toString());
        }
        return devObj;
    }

    public static String getUDID(Context context) {
        StringBuilder udid = new StringBuilder();
        String vendor = Build.MANUFACTURER;
        String sn = android.os.Build.SERIAL;
        String macAddress =null;

        try {
            InetAddress ip = getLocalNetAddress();
            byte[] b = NetworkInterface.getByInetAddress(ip).getHardwareAddress();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                if (i != 0) {
                    buffer.append(':');
                }
                String str = Integer.toHexString(b[i] & 0xFF);
                buffer.append(str.length() == 1 ? 0 + str : str);
            }
            macAddress = buffer.toString().toUpperCase();
        } catch (Exception e) {
        }
        udid.append(vendor);
        udid.append(sn);
        udid.append(macAddress);
        return shaEncrypt(udid.toString(), "SHA-256");
    }

    private static void sendOkHttpResponse(final String address, final RequestBody requestBody, final okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(address).post(requestBody).build();
        client.newCall(request).enqueue(callback);
    }

    private static String getStatisticUrl() {
        if (BrahmaConfig.debugFlag) {
            return STATISTIC_LOG_URL_DEV;
        } else {
            return STATISTIC_LOG_URL_PRODUCTION;
        }
    }

    private static InetAddress getLocalNetAddress() {
        InetAddress ip = null;
        try {
            Enumeration<NetworkInterface> en_netInterface = NetworkInterface.getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) en_netInterface.nextElement();
                Enumeration<InetAddress> en_ip = ni.getInetAddresses();
                while (en_ip.hasMoreElements()) {
                    ip = en_ip.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1)
                        break;
                    else ip = null;
                }
                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }

    private static String shaEncrypt(String strSrc, String algorithm) {
        MessageDigest md = null;
        String strDes = null;
        byte[] bt = strSrc.getBytes();
        if (null == bt || bt.length <= 0) {
            return "UNKNOWN";
        }
        try {
            md = MessageDigest.getInstance(algorithm);
            md.update(bt);
            strDes = bytes2Hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return strDes;
    }

    private static String bytes2Hex(byte[] bts) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bts.length; i++) {
            String hex = Integer.toHexString(bts[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

}
