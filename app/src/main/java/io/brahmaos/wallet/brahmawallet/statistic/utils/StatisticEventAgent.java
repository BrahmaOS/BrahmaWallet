package io.brahmaos.wallet.brahmawallet.statistic.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.statistic.database.StatisticDatabaseHelper;
import io.brahmaos.wallet.brahmawallet.statistic.database.StatisticEventBean;
import io.brahmaos.wallet.brahmawallet.statistic.network.StatisticHttpUtils;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class StatisticEventAgent extends HandlerThread {
    private static final String TAG = "StatisticEventAgent";
    private static final String THREAD_NAME = "StatisticAgent";
    private static int msgCount = 1;
    private static StatisticDatabaseHelper mStatisticDBHelper;
    private static StatisticEventAgent mInstance = null;
    private static Handler mWorkHandler;
    private Context mContext = null;

    public static final int MSG_UPLOAD = 1000;

    public static StatisticEventAgent getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new StatisticEventAgent(context, THREAD_NAME);
        }
        return mInstance;
    }

    private StatisticEventAgent(Context context, String threadName) {
        super(threadName);
        mContext = context;
        mStatisticDBHelper = StatisticDatabaseHelper.getInstance(context.getApplicationContext());
    }

    public void init() {
        if (!BrahmaConfig.getInstance().isStatisticAllowed()) {
            return;
        }
        mInstance.start();
        mWorkHandler = new Handler(mInstance.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                /** insert event into events table in DB **/
                StatisticEventBean event = (StatisticEventBean) msg.obj;
                mStatisticDBHelper.insertStatisticEvent(event);
                if ((msgCount++) % 5 == 0) {
                    StatisticHttpUtils.uploadStatisticEvents(mContext, mStatisticDBHelper);
                    msgCount = 1;
                } else if (MSG_UPLOAD == msg.what) {
                    StatisticLog.LogD(TAG, "upload called...");
                    StatisticHttpUtils.uploadStatisticEvents(mContext, mStatisticDBHelper);
                }
                return false;
            }
        });
    }

    public static void onApplicationStart(Context context/*String compName*/) {
        StatisticEventBean event = new StatisticEventBean();
        event.setTime(System.currentTimeMillis() / 1000);
        event.setType("inapp");
        event.setCompName(context.getClass().getName()/*compName*/);
        event.setCompId(null);
        if (mWorkHandler != null) {
            Message msg = new Message();
            msg.obj = event;
            mWorkHandler.sendMessage(msg);
        }
    }

    public static void onApplicationStop(String compName) {
        StatisticEventBean event = new StatisticEventBean();
        event.setTime(System.currentTimeMillis() / 1000);
        event.setType("outapp");
        event.setCompName(compName);
        event.setCompId(null);
        if (mWorkHandler != null) {
            Message msg = new Message();
            msg.what = MSG_UPLOAD;
            msg.obj = event;
            mWorkHandler.sendMessage(msg);
        }
    }

    public static void onResume(Context context) {
        if (null == context) {
            StatisticLog.LogD(TAG,"unexpected null context in onResume");
        }
        StatisticEventBean event = new StatisticEventBean();
        event.setTime(System.currentTimeMillis() / 1000);
        event.setType("inact");
        event.setCompName(context.getClass().getName());
        event.setCompId(null);
        if (mWorkHandler != null) {
            Message msg = new Message();
            msg.obj = event;
            mWorkHandler.sendMessage(msg);
        }
    }

    public static void onPause(Context context) {
        if (null == context) {
            StatisticLog.LogD(TAG,"unexpected null context in onPause");
        }
        StatisticEventBean event = new StatisticEventBean();
        event.setTime(System.currentTimeMillis() / 1000);
        event.setType("outact");
        event.setCompName(context.getClass().getName());
        event.setCompId(null);
        if (mWorkHandler != null) {
            Message msg = new Message();
            msg.obj = event;
            mWorkHandler.sendMessage(msg);
        }
    }

    /**
     * @param context the context of this activity
     * @param compId the id string of the control be clicked
     **/
    public static void onClick(Context context, String compId) {
        if (null == context) {
            StatisticLog.LogD(TAG,"unexpected null context in onClick");
        }
        StatisticEventBean event = new StatisticEventBean();
        event.setTime(System.currentTimeMillis() / 1000);
        event.setType("click");
        event.setCompName(context.getClass().getName());
        event.setCompId(compId);
        if (mWorkHandler != null) {
            Message msg = new Message();
            msg.obj = event;
            mWorkHandler.sendMessage(msg);
        }
    }

    public static void allowStatistic(Context context, boolean allow) {
        if(allow) {
            getInstance(context).init();
        } else {
            if (mInstance != null) {
                mInstance.getLooper().quitSafely();
            }
            mWorkHandler = null;
            mInstance = null;
        }
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
    }
}
