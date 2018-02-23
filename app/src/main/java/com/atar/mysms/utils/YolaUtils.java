package com.atar.mysms.utils;

/*
 * Created by Atar on 23-Feb-18.
 */

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Telephony;

import com.atar.mysms.structure.Contact;
import com.atar.mysms.structure.Sms;

public class YolaUtils {

    private YolaUtils(){

    }

    public static Sms getSMS(Cursor cursor){
        Sms sms = new Sms();
        sms.setId(cursor.getLong(cursor.getColumnIndex(Telephony.Sms._ID)));
        sms.setBody(cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY)));
        sms.setUnread(cursor.getInt(cursor.getColumnIndex(Telephony.Sms.READ)) == 0);
        sms.setType(cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE)));
        sms.setAddress(cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS)));
        boolean isOutbox = sms.getType() == Telephony.Sms.MESSAGE_TYPE_OUTBOX;
        sms.setTimestamp(cursor.getLong(cursor.getColumnIndex
                (isOutbox ? Telephony.Sms.DATE_SENT : Telephony.Sms.DATE)));
        return sms;
    }

    public static Contact getContact(Sms sms, ContentResolver contentResolver){
        Contact contact = new Contact();
        String address = sms.getAddress();
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.NORMALIZED_NUMBER,
                ContactsContract.PhoneLookup.PHOTO_URI, ContactsContract.PhoneLookup._ID};
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
        Cursor query = contentResolver.query(uri, projection, null, null, null);
        if(query != null && query.moveToFirst()){
            contact.setId(query.getLong(query.getColumnIndex(projection[4])));
            contact.setName(query.getString(query.getColumnIndex(projection[0])));
            contact.setImageUri(query.getString(query.getColumnIndex(projection[3])));
            contact.setDisplayedPhoneNumber(query.getString(query.getColumnIndex(projection[1])));
            contact.setNormalizedPhoneNumber(query.getString(query.getColumnIndex(projection[2])));
            query.close();
        } else {
            contact.setId(-1);
            contact.setName(address);
            contact.setImageUri(null);
            contact.setDisplayedPhoneNumber(address);
            contact.setNormalizedPhoneNumber(address);
        }
        return contact;
    }

}
