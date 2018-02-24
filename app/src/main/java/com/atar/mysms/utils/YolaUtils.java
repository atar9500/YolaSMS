package com.atar.mysms.utils;

/*
 * Created by Atar on 23-Feb-18.
 */

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Telephony;

import com.atar.mysms.structure.Contact;
import com.atar.mysms.structure.Conversation;
import com.atar.mysms.structure.Sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class YolaUtils {

    private YolaUtils(){}

    public static Sms getSMS(Cursor cursor, ContentResolver contentResolver){
        Sms sms = new Sms();
        sms.setId(cursor.getLong(cursor.getColumnIndex(Telephony.Sms._ID)));
        sms.setType(cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE)));
        sms.setAddress(cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS)));
        sms.setUnread(cursor.getInt(cursor.getColumnIndex(Telephony.Sms.READ)) == 0);
        sms.setBody(cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY)));
        sms.setStatus(cursor.getInt(cursor.getColumnIndex(Telephony.Sms.STATUS)));
        boolean isOutbox = sms.getType() == Telephony.Sms.MESSAGE_TYPE_OUTBOX;
        sms.setTimestamp(cursor.getLong(cursor.getColumnIndex
                (isOutbox ? Telephony.Sms.DATE_SENT : Telephony.Sms.DATE)));
        sms.setMms(sms.getBody() == null);
        if(sms.isMms()){
            getMms(sms, contentResolver);
        }
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

    private static void getMms(Sms sms, ContentResolver contentResolver){
        Uri uri = Uri.parse("content://mms/");
        String selection = Telephony.Mms._ID + "=?";
        String[] selectionArgs = new String[]{Long.toString(sms.getId())};
        String[] projection = new String[]{Telephony.Mms.READ, Telephony.Mms.DATE_SENT,
                Telephony.Mms.DATE, Telephony.Mms.STATUS};
        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
        if(cursor != null && cursor.moveToFirst()){
            try {
                sms.setUnread(cursor.getInt(cursor.getColumnIndex(Telephony.Mms.READ)) == 0);
                sms.setStatus(cursor.getInt(cursor.getColumnIndex(Telephony.Mms.STATUS)));
                boolean isOutbox = sms.getType() == Telephony.Sms.MESSAGE_TYPE_OUTBOX;
                sms.setTimestamp(cursor.getLong(cursor.getColumnIndex
                        (isOutbox ? Telephony.Mms.DATE_SENT : Telephony.Mms.DATE)) * 1000);
                getMmsContent(sms, contentResolver);
            } catch (SQLiteException e){
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
    }

    private static void getMmsContent(Sms sms, ContentResolver contentResolver){
        String selectionPart = "mid=" + sms.getId();
        Uri uri = Uri.parse("content://mms/part");
        Cursor cursor = contentResolver.query(uri, null,
                selectionPart, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String partId = cursor.getString(cursor.getColumnIndex(Telephony.Mms._ID));
                String type = cursor.getString(cursor.getColumnIndex("ct"));
                if ("text/plain".equals(type)) {
                    String data = cursor.getString(cursor.getColumnIndex("_data"));
                    String body;
                    if (data != null) {
                        body = getMms(partId, contentResolver);
                    } else {
                        body = cursor.getString(cursor.getColumnIndex("text"));
                    }
                    sms.setBody(body);
                } else if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
                        "image/jpg".equals(type) || "image/png".equals(type)) {
                    sms.setImageUri("content://mms/part/" + partId);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private static String getMms(String id, ContentResolver contentResolver) {
        Uri partURI = Uri.parse("content://mms/part/" + id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = contentResolver.openInputStream(partURI);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public static void sortSmsList(List<Sms> smsList){
        Collections.sort(smsList, new Comparator<Sms>() {
            @Override
            public int compare(Sms sms1, Sms sms2) {
                Long timestamp1 = sms1.getTimestamp();
                Long timestamp2 = sms2.getTimestamp();
                return timestamp1.compareTo(timestamp2);
            }
        });
        Collections.reverse(smsList);
    }

    public static void sortConversationsList(List<Conversation> conversations){
        Collections.sort(conversations, new Comparator<Conversation>() {
            @Override
            public int compare(Conversation conversation1, Conversation conversation2) {
                Sms sms1 = conversation1.getMessages().get(0);
                Sms sms2 = conversation2.getMessages().get(0);
                Long timestamp1 = sms1.getTimestamp();
                Long timestamp2 = sms2.getTimestamp();
                return timestamp1.compareTo(timestamp2);
            }
        });
        Collections.reverse(conversations);
    }

}
