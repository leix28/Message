package com.ihs.demo.message_2013011344;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

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

public class MessagesFragment extends Fragment implements INotificationObserver {

    private ListView listView;
    private ContactMsgAdapter adapter = null;
    private List<ContactMsg> contactMsgs;

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
        HSGlobalNotificationCenter.addObserver(DemoApplication.APPLICATION_NOTIFICATION_MESSAGE_CHANGE, this);
        for (Contact contact : FriendManager.getInstance().getAllFriends()) {
            if (contact.getMid().equals(HSAccountManager.getInstance().getMainAccount().getMID())) continue;
            List<HSBaseMessage> messages = HSMessageManager.getInstance().queryMessages(contact.getMid(), 1, -1).getMessages();
            if (messages.size() == 1) {
                contactMsgs.add(new ContactMsg(contact, messages.get(0)));
            }
        }
        refresh();
        return view;
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
        refresh();
    }
}
