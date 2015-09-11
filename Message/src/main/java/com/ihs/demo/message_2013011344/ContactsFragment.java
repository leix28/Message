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

import java.util.ArrayList;
import java.util.List;

/**
 * 联系人界面
 * 去掉了头像，添加了未读消息的数量
 */
public class ContactsFragment extends Fragment implements INotificationObserver {

    private ListView listView;
    private ContactAdapter adapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        listView = (ListView) view.findViewById(R.id.contact_list);
        final List<Contact> contacts = new ArrayList<Contact>();

        adapter = new ContactAdapter(this.getActivity(), R.layout.cell_item_contact, contacts);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mid = contacts.get(position).getMid();
                String name = contacts.get(position).getName();

                Intent intent = new Intent(ContactsFragment.this.getActivity(), ChatActivity.class);
                intent.putExtra("message_name", name);
                intent.putExtra("message_mid", mid);
                startActivity(intent);
            }

        });
        HSGlobalNotificationCenter.addObserver(FriendManager.NOTIFICATION_NAME_FRIEND_CHANGED, this);
        HSGlobalNotificationCenter.addObserver(DemoApplication.APPLICATION_NOTIFICATION_UNREAD_CHANGE, this);
        HSGlobalNotificationCenter.addObserver(HSAccountManager.HS_ACCOUNT_NOTIFICATION_LOGOUT_DID_FINISH, this);
        refresh();
        return view;
    }

    void refresh() {
        adapter.getContacts().clear();
        if  (FriendManager.getInstance() != null && HSAccountManager.getInstance().getMainAccount() != null) {
            adapter.getContacts().addAll(FriendManager.getInstance().getAllFriends());
            adapter.getContacts().remove(FriendManager.getInstance().getFriend(HSAccountManager.getInstance().getMainAccount().getMID()));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onReceive(String arg0, HSBundle arg1) {
        if (arg0.equals(HSAccountManager.HS_ACCOUNT_NOTIFICATION_LOGOUT_DID_FINISH)) {
            adapter.getContacts().clear();
            return;
        }
        refresh();
    }

}
