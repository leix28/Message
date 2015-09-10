package com.ihs.demo.message_2013011344;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.ihs.account.api.account.HSAccountManager;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.message_2013011344.R;
import com.ihs.message_2013011344.managers.HSMessageChangeListener.HSMessageChangeType;
import com.ihs.message_2013011344.managers.HSMessageManager;
import com.ihs.message_2013011344.types.HSBaseMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 会话界面，包括未读消息的更新和排序。
 */
public class MessagesFragment extends Fragment implements INotificationObserver {

    private ListView listView;
    private ContactMsgAdapter adapter = null;
    private List<ContactMsg> contactMsgs;
    public final static String MESSAGE_DELETE_NOTIFICATION = "MESSAGE_DELETE_NOTIFICATION";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        listView = (ListView) view.findViewById(R.id.message_list);
        contactMsgs = new ArrayList<ContactMsg>();

        adapter = new ContactMsgAdapter(this.getActivity(), R.layout.contact_msg_item, contactMsgs);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mid = contactMsgs.get(position).getContactMid();
                String name = contactMsgs.get(position).getContactName();

                Intent intent = new Intent(MessagesFragment.this.getActivity(), ChatActivity.class);
                intent.putExtra("message_name", name);
                intent.putExtra("message_mid", mid);
                startActivity(intent);
            }

        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String mid = contactMsgs.get(position).getContactMid();
                String name = contactMsgs.get(position).getContactName();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Delete message " + name + "?");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ContactMsgManager.getInstance().deleteMsgs(mid);
                        HSBundle bundle = new HSBundle();
                        bundle.putObject("mid", mid);
                        HSGlobalNotificationCenter.sendNotificationOnMainThread(MESSAGE_DELETE_NOTIFICATION, bundle);
                        if (HSMessageManager.getInstance().queryUnreadCount(mid) > 0) {
                            HSMessageManager.getInstance().markRead(mid);
                            HSGlobalNotificationCenter.sendNotificationOnMainThread(DemoApplication.APPLICATION_NOTIFICATION_UNREAD_CHANGE);
                        }
                    }
                });


                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();
                return true;
            }
        });

        HSGlobalNotificationCenter.addObserver(DemoApplication.APPLICATION_NOTIFICATION_MESSAGE_CHANGE, this);
        for (String mid : ContactMsgManager.getInstance().getMids()) {
            if (mid.equals(HSAccountManager.getInstance().getMainAccount().getMID())) continue;
            if (!ContactMsgManager.getInstance().hasMsgs(mid)) continue;

            HSBaseMessage message = HSMessageManager.getInstance().queryMessage(ContactMsgManager.getInstance().getFirstMsg(mid));
            Contact contact = FriendManager.getInstance().getFriend(mid);
            if (contact != null)
                contactMsgs.add(new ContactMsg(contact, message));
            else
                contactMsgs.add(new ContactMsg(mid, message));

        }
        HSGlobalNotificationCenter.addObserver(DemoApplication.APPLICATION_NOTIFICATION_UNREAD_CHANGE, this);
        HSGlobalNotificationCenter.addObserver(MESSAGE_DELETE_NOTIFICATION, this);
        refresh();
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(this.getActivity(), "Hold to Delete. ", Toast.LENGTH_SHORT).show();
    }

    void refresh() {
        Collections.sort(contactMsgs, new Comparator<ContactMsg>() {
            @Override
            public int compare(ContactMsg lhs, ContactMsg rhs) {
                return -lhs.getMessage().getTimestamp().compareTo(rhs.getMessage().getTimestamp());
            }
        });
        adapter.notifyDataSetChanged();
        listView.setSelection(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onReceive(String name, HSBundle bundle) {
        if (name.equals(DemoApplication.APPLICATION_NOTIFICATION_MESSAGE_CHANGE)) {
            HSMessageChangeType changeType = (HSMessageChangeType)bundle.getObject("changeType");
            List<HSBaseMessage> messages = (List<HSBaseMessage>)bundle.getObject("messages");
            if (changeType == HSMessageChangeType.ADDED) {
                for (HSBaseMessage message : messages) {
                    boolean flag = false;
                    for (ContactMsg contactMsg : contactMsgs) {
                        if (message.getFrom().equals(contactMsg.getContactMid()) ||
                                message.getTo().equals(contactMsg.getContactMid())) {
                            contactMsg.setMessage(message);
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        String contactMid = message.getFrom();
                        if (contactMid.equals(HSAccountManager.getInstance().getMainAccount().getMID())) {
                            contactMid = message.getTo();
                        }
                        if (contactMid.equals(HSAccountManager.getInstance().getMainAccount().getMID())) {
                            continue;
                        }
                        Contact contact = FriendManager.getInstance().getFriend(contactMid);
                        ContactMsg contactMsg;

                        if (contact == null) {
                            contactMsg = new ContactMsg(contactMid, message);
                        } else {
                            contactMsg = new ContactMsg(contact, message);
                        }
                        contactMsgs.add(contactMsg);
                    }
                }
            }
        }

        if (name.equals(MESSAGE_DELETE_NOTIFICATION)) {
            String mid = (String)bundle.getObject("mid");
            if (contactMsgs != null) {
                for (int i = 0; i < contactMsgs.size(); i++)
                    if (contactMsgs.get(i).getContactMid().equals(mid)) {
                        contactMsgs.remove(i);
                        adapter.notifyDataSetChanged();
                        break;
                    }
            }
        }
        refresh();
    }
}
