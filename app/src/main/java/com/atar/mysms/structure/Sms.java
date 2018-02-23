package com.atar.mysms.structure;

/*
 * Created by Atar on 03-Feb-18.
 */

import android.provider.Telephony;

public class Sms {

    private long mId;
    public long getId() {
        return mId;
    }
    public void setId(long id) {
        mId = id;
    }

    private String mBody;
    public String getBody() {
        return mBody;
    }
    public void setBody(String body) {
        mBody = body;
    }

    private long mTimestamp;
    public long getTimestamp() {
        return mTimestamp;
    }
    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    private boolean mIsUnread;
    public boolean isUnread() {
        return mIsUnread;
    }
    public void setUnread(boolean unread) {
        mIsUnread = unread;
    }

    private String mAddress;
    public String getAddress() {
        return mAddress;
    }
    public void setAddress(String address) {
        this.mAddress = address;
    }

    private int mType;
    public int getType() {
        return mType;
    }
    public void setType(int type) {
        mType = type;
    }

    private int mStatus;
    public int getStatus() {
        return mStatus;
    }
    public void setStatus(int status) {
        mStatus = status;
    }

    public boolean isSentSMS(){
        return mType != Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX;
    }

}
