package io.brahmaos.wallet.brahmawallet.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import io.brahmaos.wallet.brahmawallet.common.ReqCode;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.CaptureActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.Intents;

public class Brahma {

    private final String TAG = "Brahma";
    public static final int CHOOSE_PHOTO = 2;
    public static final int SHARE = 3;
    WebView mWebView;
    private Activity dappActivity;

    public class ApiResult{
        String errorMsg;
        String result;

        public ApiResult(String errorMsg, String result){
            this.errorMsg = errorMsg;
            this.result = result;
        }

        public ApiResult(){
            this.errorMsg = null;
            this.result = null;
        }

        public String getError(){
            if (errorMsg != null){
                return String.format("{\"message\":\"%s\"}", errorMsg);
            }else {
                return null;
            }
        }

        public String getResult(){
            return result;
        }

        public void setErrorMsg(String errorMsg){
            this.errorMsg = errorMsg;
        }

        public void setResult(String result){
            this.result = result;
        }

        public String toString(){
            return this.errorMsg + this.result;
        }
    }

    private class SelectedResp{
        String data;
        int picwidth;
        int picheight;
        int fileSize;
        public SelectedResp(String data, int width, int height, int fileSize){
            this.data = data;
            this.picwidth = width;
            this.picheight = height;
            this.fileSize = fileSize;
        }

        public String toString(){
            Log.i(TAG, "datasize: " + data.length());
            String result = String.format("{\"data\":\"data:image/png;base64,%s\", \"width\":\"%d\", \"height\":\"%d\", \"fileSize\":\"%d\"}",data, picwidth, picheight, fileSize);
            Log.i(TAG, "resultlength: " + result.length());
            return result;
        }
    }

    interface TokenApi{
        ApiResult realApi(String parameter, String callBack);
    }

    public class BrahmaAPI{
        boolean needOptions;
        boolean needCallback;
        boolean callBackWait;
        TokenApi tokenApi;

        public BrahmaAPI(boolean boolOptions, boolean boolCallback,  boolean callBackWait, TokenApi tokenApi){
            this.needOptions = boolOptions;
            this.needCallback = boolCallback;
            this.callBackWait = callBackWait;
            this.tokenApi = tokenApi;
        }

        public String toString(){
            return "BrahmaAPI{"
                    + "needoptions=" + needOptions +
                    ", needcallback=" + needCallback +
                    ", callBackWait=" + callBackWait +
                    "}";
        }
    }


    Map<String, BrahmaAPI> methodMap = new HashMap<String, BrahmaAPI>();

    public Brahma(Activity activity, WebView webView){
        this.mWebView = webView;
        this.dappActivity = activity;
        initBrahmaApi();
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "PageFinished");
                String jsString = "var imToken = {}; imToken.callAPI = function(apiname, options, func){"
                        + "if (options == null) { "
                            + "brahma.callAPIAsString(apiname);"
                        +  "}"
                        + "else if (options != null && func == null) {"
                            + "brahma.callAPIAsString(apiname, options.toString());"
                        +   "}"
                        +  "else if (options != null && func != null) { "
                            + "console.log(JSON.stringify(options));"
                            + "brahma.callAPIAsString(apiname, JSON.stringify(options), func.toString());"
                        +   "}"
                        + "};";
                view.evaluateJavascript(jsString, null);
                super.onPageFinished(view, url);
            }
        });
        Log.i(TAG, methodMap.toString());
    }

    @JavascriptInterface
    public void callAPI(String apiName){
        Log.i(TAG, "callAPI " + apiName);
        BrahmaAPI targetApi = methodMap.get(apiName);
        if (targetApi != null){
            targetApi.tokenApi.realApi(null, null);
        }else{
            Log.i(TAG, "can't find API: " + apiName);
        }
    }

    @JavascriptInterface
    public void callAPIAsString(String apiName){
        Log.i(TAG, "callAPIAsString one " + apiName);
        BrahmaAPI targetApi = methodMap.get(apiName);
        if (targetApi != null){
            targetApi.tokenApi.realApi(null, null);
        }else{
            Log.e(TAG, "Can't find API: " + apiName);
        }
    }

    @JavascriptInterface
    public void callAPIAsString(String apiName, final String secondParameter)  {
        Log.i(TAG, "callAPIAsString two " + apiName);
        ApiResult apiResult = new ApiResult();
        Log.i(TAG, "secondParameter: " + secondParameter);
        BrahmaAPI targetApi = methodMap.get(apiName);
        if (targetApi != null){
            // second parameter is Options Parameter
            if (targetApi.needOptions == true && targetApi.needCallback == false){
                Log.i(TAG, "secondParameter is options");
                apiResult = targetApi.tokenApi.realApi(secondParameter, null);
            }
            // second parameter is callback JS function
            else if(targetApi.needOptions == false && targetApi.needCallback == true){
                Log.i(TAG, "secondParameter is Js function");
                apiResult = targetApi.tokenApi.realApi(null, secondParameter);
                if (targetApi.callBackWait == false){
                    final String jsString;
                    if (apiResult.getError() == null){
                        jsString = String.format("var funcObj = %s; funcObj(null, \'%s\');", secondParameter, apiResult.getResult());
                    }else{
                        jsString = String.format("var funcObj = %s; funcObj(\'%s\', \'%s\');", secondParameter, apiResult.getError(), apiResult.getResult());
                    }

                    dappActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "JsScript:" + jsString);
                        mWebView.evaluateJavascript(jsString, null);
                    }
                    });
                }
            }
        }else{
            Log.e(TAG, "Can't find API: " + apiName);
        }
    }

    @JavascriptInterface
    public void callAPIAsString(String apiName, final String secondParameter, final String thirdParameter){
        Log.i(TAG, "callAPIAsString three " + apiName);
        ApiResult apiResult = new ApiResult();
        Log.i(TAG, "secondParameter: " + secondParameter + "thirdParameter: " + thirdParameter);
        BrahmaAPI targetApi = methodMap.get(apiName);
        if (targetApi != null){
            if (targetApi.needOptions == true && targetApi.needCallback == true){
                apiResult = targetApi.tokenApi.realApi(secondParameter, thirdParameter);
                if (targetApi.callBackWait == false){
                    final String jsString;
                    if (apiResult.getError() == null){
                        jsString = String.format("var funcObj = %s; funcObj(null, \'%s\');", thirdParameter, apiResult.getResult());
                    }else{
                        jsString = String.format("var funcObj = %s; funcObj(\'%s\', \'%s\');", thirdParameter, apiResult.getError(), apiResult.getResult());
                    }

                    dappActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "JsScript:" + jsString);
                            mWebView.evaluateJavascript(jsString, null);
                        }
                    });
                }
            }
            else{
                Log.e(TAG, "API " + apiName + " is not correct. It should contains options and callback");
            }
        }else{
            Log.e(TAG, "Can't find API: " + apiName);
        }
    }


    void addApi(String apiName, BrahmaAPI brahmaAPI){
        methodMap.put(apiName, brahmaAPI);
    }

//    Navigator API:
//      getOrientation
//      setOrientation
//      closeDapp
//      goBack


//    closeDapp: no options, no callback
    BrahmaAPI navigatorCloseDapp = new BrahmaAPI(false, false, false, new TokenApi() {
        @Override
        public ApiResult realApi(String parameter, String jsScript) {
            dappActivity.finish();
            return null;
        }
    });


//    goBack: no options, no callback
    BrahmaAPI navigatorGoBack = new BrahmaAPI(false, false, false ,new TokenApi() {
        @Override
        public ApiResult realApi(String parameter, String jsScript) {
            dappActivity.finish();
            return null;
        }
    });

//    getOrientation: no options, wich callback
    BrahmaAPI navigatorGetOrientation = new BrahmaAPI(false, true, false, new TokenApi() {
        @Override
        public ApiResult realApi(String parameter, String jsScript) {
            ApiResult apiResult = new ApiResult();
            int orientation = dappActivity.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE){
                apiResult.setResult("landscape");
            }else {
                apiResult.setResult("landscape");
            }
            return apiResult;
        }
    });

//    setOrientations: has options, no callback
    BrahmaAPI navigatorSetOrientation = new BrahmaAPI(true, false, false, new TokenApi() {
        @Override
        public ApiResult realApi(String parameter, String jsScript) {
            if (parameter.equals("landscape")){
                dappActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }else if(parameter.equals("portrait")){
                dappActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            return null;
        }
    });

//    Native API

//   toastInfo: has options, no callback
    BrahmaAPI nativeToastInfo = new BrahmaAPI(true, false, false, new TokenApi() {
        @Override
        public ApiResult realApi(String parameter, String jsScript) {
            Toast.makeText(dappActivity, parameter, Toast.LENGTH_SHORT).show();
            return null;
        }
    });

// alert: has options, no callback
    BrahmaAPI nativeAlert = new BrahmaAPI(true, false, false, new TokenApi() {
        @Override
        public ApiResult realApi(String parameter, String jsScript) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(dappActivity);
            dialog.setMessage(parameter);
            dialog.setCancelable(true);
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            dialog.show();
            return null;
        }
    });

// confirm: has options, has callback and callback after confirmed
    BrahmaAPI nativeConfirm = new BrahmaAPI(true, true, true, new TokenApi() {
        @Override
        public ApiResult realApi(String parameter, final String jsScript) {
            final ApiResult result = new ApiResult();
            final String title;
            final String message;
            final String cancelText;
            final String confirmText;
            final JSONObject strJson;

            try {
                strJson = new JSONObject(parameter);
            }catch (JSONException e){
                e.printStackTrace();
                return null;
            }
            try {
                title = strJson.getString("title");
                message = strJson.getString("message");
                cancelText = strJson.getString("cancelText");
                confirmText = strJson.getString("confirmText");
            }catch (JSONException e){
                e.printStackTrace();
                return null;
            }
            Log.i(TAG, "confirm: " + strJson.toString());
            AlertDialog.Builder dialog = new AlertDialog.Builder(dappActivity);
            dialog.setTitle(title);
            dialog.setMessage(message);
            dialog.setPositiveButton(confirmText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    result.setResult("Confirm Clicekd");
                    final String jsString = String.format("var funcObj = %s; funcObj(null, \'%s\');", jsScript, result.getError(), result.getResult());
                    dappActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "JsScript:" + jsString);
                            mWebView.evaluateJavascript(jsString, null);
                        }
                    });
                }
            });

            dialog.setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    result.setErrorMsg("Canceled");
                    result.setResult("Cancel Clicked");
                    final String jsString = String.format("var funcObj = %s; funcObj(\'%s\', \'%s\');", jsScript, result.getError(), result.getResult());
                    dappActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "JsScript:" + jsString);
                            mWebView.evaluateJavascript(jsString, null);
                        }
                    });

                }
            });

            dialog.show();

            return result;
        }
    });


// selectpicture: with options and callback, callback wait
    String callbackSelectPhoto;
    String photoParameter;

    BrahmaAPI nativeSelectPicture = new BrahmaAPI(true, true, true, new TokenApi() {
        @Override
        public ApiResult realApi(String parameter, String callBack) {
            photoParameter = parameter;
            callbackSelectPhoto = callBack;
            openAlbum();
            return null;
        }
    });

    // call in onActivityResult
    public void onPhotoSelected(Uri uri) {
        int picWidth = 0;
        int picHeight = 0;
        JSONObject strJson = null;
        ApiResult result = new ApiResult();

        try {
            strJson = new JSONObject(photoParameter);
        }catch (JSONException e){
            e.printStackTrace();
        }
        try {
            picWidth = strJson.getInt("maxWidth");
            picHeight = strJson.getInt("maxHeight");
        }catch (JSONException e){
            e.printStackTrace();
        }

        InputStream imageStream = null;
        try {
            imageStream = dappActivity.getContentResolver().openInputStream(uri);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

        String encodedImage = encodeImage(imageStream);
        SelectedResp selectedResp = new SelectedResp(encodedImage, picWidth, picHeight, 1000);

        result.setResult(selectedResp.toString());
        final String jsString = String.format("var funcObj = %s; funcObj(null, %s);", callbackSelectPhoto, result.getResult());
        dappActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "JsScript:" + jsString);
                mWebView.evaluateJavascript(jsString, null);
            }
        });
    }

    // transfer file to Base64
    public String encodeImage(InputStream inputStream){

        byte[] data = null;

        try {
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        // Base64.NO_WRAP without new line
        String encImage = Base64.encodeToString(data, Base64.NO_WRAP);
        return encImage;
    }


    private void openAlbum(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        dappActivity.startActivityForResult(intent, CHOOSE_PHOTO);
    }


    BrahmaAPI nativeSetClipboard = new BrahmaAPI(true, false, false, new TokenApi() {
        @Override
        public ApiResult realApi(String parameter, String callBack) {
            ClipboardManager clipboard = (ClipboardManager) dappActivity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText(null, parameter);
            clipboard.setPrimaryClip(clipData);
            return null;
        }
    });


    BrahmaAPI nativeShare = new BrahmaAPI(true, true, true, new TokenApi() {
        @Override
        public ApiResult realApi(String parameter, String callBack) {
            JSONObject strJson = null;
            ApiResult result = new ApiResult();
            String title = null;
            String message = null;
            String url = null;

            try {
                strJson = new JSONObject(parameter);
            }catch (JSONException e){
                e.printStackTrace();
            }
            try {
                title = strJson.getString("title");
                message = strJson.getString("message");
                url = strJson.getString("url");
            }catch (JSONException e){
                e.printStackTrace();
            }
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            sendIntent.setType("text/plain");
            dappActivity.startActivityForResult(Intent.createChooser(sendIntent, title),SHARE);
            return null;
        }
    });

    String callbackQRdetect;

    BrahmaAPI nativeScanQRCode = new BrahmaAPI(false, true, true, new TokenApi() {
        @Override
        public ApiResult realApi(String parameter, String callBack) {
            callbackQRdetect = callBack;
            Intent intent = new Intent(dappActivity, CaptureActivity.class);
            intent.putExtra(Intents.Scan.PROMPT_MESSAGE, "");
            dappActivity.startActivityForResult(intent, ReqCode.SCAN_QR_CODE);
            return null;
        }
    });

    public void onQRcodeDetected(boolean detected, String qrInfo){
        ApiResult result = new ApiResult();
        if (detected){
            result.setResult(qrInfo);
        }else {
            result.setErrorMsg("Not deteced");
        }

        final String jsString = String.format("var funcObj = %s; funcObj(%s, \'%s\');", callbackQRdetect, result.errorMsg, result.getResult());
        dappActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "JsScript:" + jsString);
                mWebView.evaluateJavascript(jsString, null);
            }
        });

    }

    BrahmaAPI userCurrentAccount = new BrahmaAPI(false, true, true, new TokenApi() {
        @Override
        public ApiResult realApi(String parameter, String callBack) {
            ApiResult result = new ApiResult();
            result.setResult(BrahmaConst.KYBER_WRAPPER_ADDRESS);
            final String jsString = String.format("var funcObj = %s; funcObj(%s, \'%s\');", callBack, result.getError(), result.getResult());
            dappActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "JsScript:" + jsString);
                    mWebView.evaluateJavascript(jsString, null);
                }
            });
            return null;
        }
    });

    void initBrahmaApi(){
        addApi("navigator.goBack", navigatorGoBack);
        addApi("navigator.closeDapp", navigatorCloseDapp);
        addApi("navigator.getOrientation", navigatorGetOrientation);
        addApi("navigator.setOrientation", navigatorSetOrientation);

        addApi("native.toastInfo", nativeToastInfo);
        addApi("native.alert", nativeAlert);
        addApi("native.confirm", nativeConfirm);
        addApi("native.selectPicture", nativeSelectPicture);
        addApi("native.setClipboard", nativeSetClipboard);
        addApi("native.share", nativeShare);
        addApi("native.scanQRCode", nativeScanQRCode);

        addApi("user.getCurrentAccount", userCurrentAccount);
    }
}