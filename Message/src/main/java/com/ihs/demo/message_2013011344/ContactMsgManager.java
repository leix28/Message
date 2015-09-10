package com.ihs.demo.message_2013011344;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ihs.account.api.account.HSAccountManager;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.message_2013011344.managers.MessageDBManager;
import com.ihs.message_2013011344.types.HSBaseMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LazyLie on 15/9/8.
 */

public class ContactMsgManager extends SQLiteOpenHelper implements INotificationObserver {
    private static final String DBNAME = "contact_msg.db";
    private static final String TABLENAME = "contact_msg_tb";
    private static final int DBVERSION = 1;
    public static final String MSG_ID = "_msg_id";
    public static final String CONTACT_ID = "_contact_id";
    private static final String CREATE_SQL = "create table if not exists " + TABLENAME + " (" + MSG_ID + " text, " + CONTACT_ID + " long );";
    private static final String INSERT_SQL = "insert into " + TABLENAME + "(" + MSG_ID + "," + CONTACT_ID + ")" + "values (?, ?)";
    private static final String DELETE_SQL = "delete from " + TABLENAME + " where " + CONTACT_ID + " = ?";
    private static final String QUERY_SQL = "select " + MSG_ID + " from " + TABLENAME + " where " + CONTACT_ID + " = ?";
    private static final String QUERY_NAME = "select distinct " + CONTACT_ID + " from " + TABLENAME;
    private static final String DELETE_ALL = "delete from " + TABLENAME;

    private final static String TAG = MessageDBManager.class.getName();

    private static ContactMsgManager manager = null;

    private ContactMsgManager(Context context) {
        super(context, DBNAME, null, DBVERSION);
        HSGlobalNotificationCenter.addObserver(HSAccountManager.HS_ACCOUNT_NOTIFICATION_SIGNIN_DID_FINISH, this);
        HSLog.d(TAG, CREATE_SQL);
        HSLog.d(TAG, INSERT_SQL);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static ContactMsgManager getInstance() {
        if (manager == null) manager = new ContactMsgManager(DemoApplication.getContext());
        return manager;
    }

    public void insertMsgs(List<HSBaseMessage> messages) {
        for (HSBaseMessage message : messages) {
            ContentValues values = new ContentValues();
            values.put(MSG_ID, message.getMsgID());
            String contact = null;
            if (message.getFrom().equals(HSAccountManager.getInstance().getMainAccount().getMID()))
                contact = message.getTo();
            if (message.getTo().equals(HSAccountManager.getInstance().getMainAccount().getMID()))
                contact = message.getFrom();
            values.put(CONTACT_ID, Integer.valueOf(contact));

            getWritableDatabase().execSQL(INSERT_SQL, new Object[] { values.get(MSG_ID), values.get(CONTACT_ID)});
        }
    }

    public void deleteMsgs(String mid) {
        getWritableDatabase().execSQL(DELETE_SQL, new Object[]{mid});
    }

    public ArrayList<String> getMsgs(String mid) {
        Cursor cursor = getReadableDatabase().rawQuery(QUERY_SQL, new String [] {mid});
        ArrayList<String> list = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(cursor.getString(cursor.getColumnIndex(MSG_ID)));
            cursor.move(1);
        }
        return list;
    }

    public boolean hasMsgs(String mid) {
        Cursor cursor = getReadableDatabase().rawQuery(QUERY_SQL, new String [] {mid});
        ArrayList<String> list = new ArrayList<>();
        cursor.moveToFirst();
        return !cursor.isAfterLast();
    }

    public String getFirstMsg(String mid) {
        Cursor cursor = getReadableDatabase().rawQuery(QUERY_SQL, new String [] {mid});
        if (cursor.moveToLast())
            return cursor.getString(cursor.getColumnIndex(MSG_ID));
        else
            return null;
    }

    public ArrayList<String> getMids() {
        Cursor cursor = getReadableDatabase().rawQuery(QUERY_NAME, new String[]{});
        ArrayList<String> list = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(cursor.getString(cursor.getColumnIndex(CONTACT_ID)));
            cursor.move(1);
        }
        return list;
    }
    
    @Override
    public void onReceive(String name, HSBundle bundle) {
        if (name.equals(HSAccountManager.HS_ACCOUNT_NOTIFICATION_SIGNIN_DID_FINISH)) {   
            getWritableDatabase().execSQL(DELETE_ALL);
        }
    }
}

