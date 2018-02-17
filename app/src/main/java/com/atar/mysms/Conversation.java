package com.atar.mysms;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Atar on 03-Feb-18.
 */

public class Conversation {

    public Conversation(){
        mMessages = new ArrayList<>();
    }

    private Contact mContact;
    public Contact getContact() {
        return mContact;
    }
    public void setContact(Contact contact) {
        mContact = contact;
    }

    private int mThreadId;
    public int getThreadId() {
        return mThreadId;
    }
    public void setThreadId(int threadId) {
        mThreadId = threadId;
    }

    private List<Sms> mMessages;
    public List<Sms> getMessages() {
        return mMessages;
    }
    public void setMessages(List<Sms> messages) {
        mMessages = messages;
    }

    public int getUnread() {
        int unread = 0;
        for(Sms sms: mMessages){
            if(sms.isUnread()){
                unread++;
            }
        }
        return unread;
    }

}
