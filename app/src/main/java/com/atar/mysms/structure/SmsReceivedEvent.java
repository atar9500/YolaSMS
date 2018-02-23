package com.atar.mysms.structure;

/*
 * Created by Atar on 23-Feb-18.
 */

public class SmsReceivedEvent {

    private Sms mSms;
    public Sms getSms() {
        return mSms;
    }
    public void setSms(Sms sms) {
        mSms = sms;
    }

    private Contact mContact;
    public Contact getContact() {
        return mContact;
    }
    public void setContact(Contact contact) {
        mContact = contact;
    }

}
