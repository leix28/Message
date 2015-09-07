package com.ihs.demo.message_2013011344;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ihs.message_2013011344.R;
import com.ihs.message_2013011344.managers.HSMessageManager;

import java.util.List;

public class ContactAdapter extends ArrayAdapter<Contact> {

    private List<Contact> contacts;
    private Context context;


    private class ViewHolder {
        TextView contactNameView;
        TextView contactTelView;
        TextView contactMsgView;
        TextView contactUnread;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public ContactAdapter(Context context, int resource, List<Contact> objects) {
        super(context, resource, objects);
        this.contacts = objects;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.cell_item_contact, parent, false);
            holder.contactNameView = (TextView) convertView.findViewById(R.id.contact_name);
            holder.contactTelView = (TextView) convertView.findViewById(R.id.contact_tel);
            holder.contactMsgView = (TextView) convertView.findViewById(R.id.contact_last_msg);
            holder.contactUnread = (TextView) convertView.findViewById(R.id.contact_unread);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contact contact = contacts.get(position);
        holder.contactNameView.setText(contact.getName());
        holder.contactTelView.setText(contact.getContent());
        holder.contactMsgView.setText("mid: " + contact.getMid());
        String cnt = new Integer(HSMessageManager.getInstance().queryUnreadCount(contact.getMid())).toString();
        holder.contactUnread.setText(cnt);
        return convertView;
    }
}
