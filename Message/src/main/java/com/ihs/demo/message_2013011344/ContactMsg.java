package com.ihs.demo.message_2013011344;

import com.ihs.message_2013011344.types.HSBaseMessage;
import com.ihs.message_2013011344.types.HSMessageType;
import com.ihs.message_2013011344.types.HSTextMessage;

/**
 * 用于处理用户和最新的一条消息
 * 用户可以不是好友，但保证contact和mid中至少有一个不是null
 * Created by LazyLie on 15/9/6.
 */
public class ContactMsg {
    Contact contact;
    String unknownMid;
    HSBaseMessage message;

    public ContactMsg(String mid, HSBaseMessage message) {
        contact = null;
        this.unknownMid = mid;
        this.message = message;
    }

    public ContactMsg(Contact contact, HSBaseMessage message) {
        this.contact = contact;
        this.message = message;
        unknownMid = null;
    }

    public void setUnknownMid(String mid) {
        unknownMid = mid;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public void setMessage(HSBaseMessage message) {
        this.message = message;
    }

    public String getContactName() {
        if (contact != null)
            return contact.getName();
        return "stranger";
    }

    public String getContactMid() {
        if (contact != null)
            return contact.getMid();
        else
            return unknownMid;
    }

    public HSBaseMessage getMessage() {
        return message;
    }

    public String getIntroduction() {
        if (message.getType() == HSMessageType.TEXT) {
            String tmp = ((HSTextMessage)message).getText().toString();
            if (tmp.length() < 15)
                return tmp;
            else
                return tmp.substring(13) + "...";
        } else {
            return "[" + message.getTypeString() + "]";
        }
    }
}
