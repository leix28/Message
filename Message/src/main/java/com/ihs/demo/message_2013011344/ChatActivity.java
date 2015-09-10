package com.ihs.demo.message_2013011344;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ihs.app.framework.HSSessionMgr;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.message_2013011344.R;
import com.ihs.message_2013011344.managers.HSMessageChangeListener.HSMessageChangeType;
import com.ihs.message_2013011344.managers.HSMessageManager;
import com.ihs.message_2013011344.types.HSBaseMessage;
import com.ihs.message_2013011344.types.HSImageMessage;
import com.ihs.message_2013011344.types.HSTextMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends HSActionBarActivity implements INotificationObserver {

    String mid, name;
    EditText chatText;
    Button sendButton, chatVoice;
    ImageView moreChoice, sendImage, sendLocation, sendVoiceButton, sendTextButton;
    LinearLayout moreChoiceBar;
    ListView chatHistoryListView;
    List<HSBaseMessage> chatHistoryList = new ArrayList<HSBaseMessage>();
    MsgAdapter chatHistoryListAdapter;
    private final static String TAG = SampleFragment.class.getName();

    private void flushData() {
        chatHistoryListAdapter.notifyDataSetChanged();
        chatHistoryListView.setSelection(chatHistoryList.size() - 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Bundle b = getIntent().getExtras();
        mid = b.getString("message_mid");
        name = b.getString("message_name");
        setTitle(name);

        chatText = (EditText)findViewById(R.id.chat_text);
        chatVoice = (Button)findViewById(R.id.chat_voice);
        sendButton = (Button)findViewById(R.id.chat_send);
        sendTextButton = (ImageView)findViewById(R.id.chat_send_text);
        sendVoiceButton = (ImageView)findViewById(R.id.chat_send_voice);
        moreChoice = (ImageView)findViewById(R.id.chat_more_choice);
        moreChoiceBar = (LinearLayout)findViewById(R.id.chat_more_btn);
        sendImage = (ImageView)findViewById(R.id.chat_send_image);
        sendLocation = (ImageView)findViewById(R.id.chat_send_location);
        chatHistoryListView = (ListView)findViewById(R.id.chat_history_list);
        chatHistoryListAdapter = new MsgAdapter(this, R.layout.msg_item, chatHistoryList);
        chatHistoryListView.setAdapter(chatHistoryListAdapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textString = chatText.getText().toString();
                if (textString.equals("")) return;
                chatText.setText("");

                HSMessageManager.getInstance().send(new HSTextMessage(mid, textString), new HSMessageManager.SendMessageCallback() {

                    @Override
                    public void onMessageSentFinished(HSBaseMessage message, boolean success, HSError error) {
                        HSLog.d(TAG, "success: " + success);
                    }
                }, new Handler());

            }
        });

        chatText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                moreChoiceBar.setVisibility(View.GONE);
                return false;
            }
        });


        moreChoice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                moreChoiceBar.setVisibility(View.VISIBLE);
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return false;
            }
        });

        chatHistoryListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                moreChoiceBar.setVisibility(View.GONE);
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return false;
            }
        });

        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });

        sendTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVoiceButton.setVisibility(View.VISIBLE);
                sendTextButton.setVisibility(View.GONE);
                moreChoice.setVisibility(View.VISIBLE);
                chatVoice.setVisibility(View.GONE);
                chatText.setVisibility(View.VISIBLE);
            }
        });

        sendVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                sendVoiceButton.setVisibility(View.GONE);
                sendTextButton.setVisibility(View.VISIBLE);
                moreChoice.setVisibility(View.GONE);
                chatVoice.setVisibility(View.VISIBLE);
                chatText.setVisibility(View.GONE);
            }
        });

        HSGlobalNotificationCenter.addObserver(DemoApplication.APPLICATION_NOTIFICATION_MESSAGE_CHANGE, this);
        HSGlobalNotificationCenter.addObserver(MessagesFragment.MESSAGE_DELETE_NOTIFICATION, this);
        init();
    }

    private void init() {

        ArrayList<String> messages = ContactMstManager.getInstance().getMsgs(mid);
        for (String messageId : messages) {
            chatHistoryList.add(HSMessageManager.getInstance().queryMessage(messageId));
        }
        flushData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Integer.valueOf(mid));
        HSMessageManager.getInstance().markRead(mid);
        HSGlobalNotificationCenter.sendNotificationOnMainThread(DemoApplication.APPLICATION_NOTIFICATION_UNREAD_CHANGE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mid = intent.getExtras().getString("message_mid");
        name = intent.getExtras().getString("message_name");
        setTitle(name);
        init();
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Integer.valueOf(mid));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReceive(String name, HSBundle bundle) {
        if (name.equals(DemoApplication.APPLICATION_NOTIFICATION_MESSAGE_CHANGE)) {
            HSMessageChangeType changeType = (HSMessageChangeType)bundle.getObject("changeType");
            List<HSBaseMessage> messages = (List<HSBaseMessage>)bundle.getObject("messages");
            boolean flag = false, sendUnreadNotification = false;

            for (HSBaseMessage message : messages)
                if (message.getTo().equals(mid) || message.getFrom().equals(mid)) {
                    if (changeType == HSMessageChangeType.ADDED) {
                        chatHistoryList.add(message);
                    }
                    flag = true;
                }

            if (flag) {
                if (HSSessionMgr.getTopActivity() == this && HSMessageManager.getInstance().queryUnreadCount(mid) > 0) {
                    HSMessageManager.getInstance().markRead(mid);
                    HSGlobalNotificationCenter.sendNotificationOnMainThread(DemoApplication.APPLICATION_NOTIFICATION_UNREAD_CHANGE);
                }
                flushData();
            }
        }

        if (name.equals(MessagesFragment.MESSAGE_DELETE_NOTIFICATION)) {
            String delMid = (String)bundle.getObject("mid");
            if (mid.equals(delMid)) {
                chatHistoryList.clear();
                flushData();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        Uri img = data.getData();//得到新Activity 关闭后返回的数据
        String path = getRealPathFromURI(img);
        if (path == null) {
            HSLog.e(TAG, img.toString());
            return;
        }
        HSMessageManager.getInstance().send(new HSImageMessage(mid, path), new HSMessageManager.SendMessageCallback() {

            @Override
            public void onMessageSentFinished(HSBaseMessage message, boolean success, HSError error) {
                HSLog.d(TAG, "success: " + success);
            }
        }, new Handler());
    }

    public String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return uri.getPath();
        cursor.moveToFirst();

        Log.d(TAG, DatabaseUtils.dumpCursorToString(cursor));

        int columnIndex = cursor.getColumnIndex(projection[0]);
        String picturePath = cursor.getString(columnIndex); // returns null
        cursor.close();
        HSLog.e(TAG, picturePath);
        return picturePath;
    }

    @Override
    protected void onDestroy() {
        HSGlobalNotificationCenter.removeObserver(DemoApplication.APPLICATION_NOTIFICATION_MESSAGE_CHANGE, this);
        super.onDestroy();
    }
}
