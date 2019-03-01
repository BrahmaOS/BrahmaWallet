package io.brahmaos.wallet.brahmawallet.statistic.database;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import io.brahmaos.wallet.brahmawallet.WalletApp;
import io.brahmaos.wallet.brahmawallet.statistic.utils.StatisticLog;

public class StatisticDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "StatisticDatabaseHelper";
    public final static String DB_NAME = "statistic.db";
    public final static int VERSION = 1;
    private static StatisticDatabaseHelper instance = null;

    private SQLiteDatabase mDB;
    private static final String TABLE_EVENT = "events";
    private static final String EVENT_ID = "id";
    private static final String EVENT_TIME = "t";
    private static final String EVENT_TYPE = "type";
    private static final String EVENT_COMP_NAME = "comp_name";
    private static final String EVENT_COMP_ID = "comp_id";


    public static StatisticDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new StatisticDatabaseHelper(context);
        }
        return instance;
    }

    private StatisticDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_EVENTS = "create table " + TABLE_EVENT + "("
                /*+ EVENT_ID + " integer primary key autoincrement, "*/
                + EVENT_TIME + " integer not null, "
                + EVENT_TYPE + " text not null, "
                + EVENT_COMP_NAME + " text not null, "
                + EVENT_COMP_ID + " text)";
        db.execSQL(CREATE_EVENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void openDatabase() {
        if (mDB == null) {
            mDB = getWritableDatabase();
        }
    }

    public boolean insertStatisticEvent(StatisticEventBean event) {
        if (null == event) {
            return true;
        }
//        StatisticLog.LogD(TAG, "[test]insertStatisticEvent--" + event.toString());
        synchronized (this) {
            openDatabase();
            try {
                ContentValues values = new ContentValues();
                values.put(EVENT_TIME, event.getTime());
                values.put(EVENT_TYPE, event.getType());
                values.put(EVENT_COMP_NAME, event.getCompName());
                if (event.getCompId() != null) {
                    values.put(EVENT_COMP_ID, event.getCompId());
                }
                mDB.insert(TABLE_EVENT, null, values);
                return true;
            } catch (Exception e) {
                StatisticLog.LogE(TAG, "Failed to insert event: " + e.toString());
                return false;
            }
        }
    }

    public ArrayList<StatisticEventBean> getStatisticEventList() {
        synchronized (this) {
            openDatabase();
            Cursor cursor = mDB.query("events", null, null, null, null, null, null);
            ArrayList<StatisticEventBean> eventList = new ArrayList<>();
            while (cursor.moveToNext()) {
                StatisticEventBean event = new StatisticEventBean();
                event.setTime(cursor.getLong(cursor.getColumnIndex(EVENT_TIME)));
                event.setType(cursor.getString(cursor.getColumnIndex(EVENT_TYPE)));
                event.setCompName(cursor.getString(cursor.getColumnIndex(EVENT_COMP_NAME)));
                if (cursor.getColumnIndex(EVENT_COMP_ID) >= 0) {
                    event.setCompId(cursor.getString(cursor.getColumnIndex(EVENT_COMP_ID)));
                }
                eventList.add(event);
//                StatisticLog.LogD(TAG, "[test]getStatisticEventList--" + event.toString());
            }
            cursor.close();
            return eventList;
        }
    }

    public boolean deleteStatisticEvents(/*int count*/) {
        StatisticLog.LogD(TAG, "[test]deleteStatisticEvents----"/* + count*/);
        synchronized (this) {
            try {
                openDatabase();
                mDB.execSQL("delete from " + TABLE_EVENT);
                //mDB.execSQL("delete from " + TABLE_EVENT + " where id  in (select id from " + TABLE_EVENT + " order by id limit " + count + ")");
                return true;
            } catch (Exception e) {
                StatisticLog.LogE(TAG, "Failed to clear table events: " + e.toString());
                return false;
            }
        }
    }


}
