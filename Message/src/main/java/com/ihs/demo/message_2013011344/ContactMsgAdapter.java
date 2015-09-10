package com.ihs.demo.message_2013011344;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ihs.message_2013011344.R;
import com.ihs.message_2013011344.managers.HSMessageManager;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 创建会话界面的view
 * Created by LazyLie on 15/9/6.
 */
public class ContactMsgAdapter extends ArrayAdapter<ContactMsg> {
    private List<ContactMsg> contactMsgs;
    private Context context;
    static final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm MM/dd");


    private class ViewHolder {
        TextView contactMsgNameView;
        TextView contactMsgTimeView;
        TextView contactMsgLastMsgView;
        TextView contactMsgUnread;
    }

    public List<ContactMsg> getContacts() {
        return contactMsgs;
    }

    public ContactMsgAdapter(Context context, int resource, List<ContactMsg> objects) {
        super(context, resource, objects);
        this.contactMsgs = objects;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.contact_msg_item, parent, false);
            holder.contactMsgNameView = (TextView) convertView.findViewById(R.id.contact_msg_name);
            holder.contactMsgTimeView = (TextView) convertView.findViewById(R.id.contact_msg_time);
            holder.contactMsgLastMsgView = (TextView) convertView.findViewById(R.id.contact_msg_last_msg);
            holder.contactMsgUnread = (TextView)convertView.findViewById(R.id.contact_msg_unread);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ContactMsg contactMsg = contactMsgs.get(position);
        holder.contactMsgNameView.setText(contactMsg.getContactName());
        holder.contactMsgTimeView.setText(formatter.format(contactMsg.getMessage().getTimestamp()));
        holder.contactMsgLastMsgView.setText(contactMsg.getIntroduction());
        String cnt = new Integer(HSMessageManager.getInstance().queryUnreadCount(contactMsg.getContactMid())).toString();
        holder.contactMsgUnread.setText(cnt);


        return convertView;
    }

}
