package com.atar.mysms;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements ConversationsFragment.ConversationsCallback {

    /**
     * Request Codes
     */
    private static final int CHANGE_DEFAULT_REQUEST = 10;
    private static final int READ_CONTACTS_REQUEST = 11;

    /**
     * Permissions
     */
    private static final String[] PERMISSIONS = new String[]
            {Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.RECEIVE_MMS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS};

    /**
     * Fragments
     */
    private ConversationsFragment mConversationsFragment;

    /**
     * Activity Methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFragments();
        initUIWidgets();

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.am_container, mConversationsFragment, ConversationsFragment.TAG).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == R.id.action_settings;
    }

    /**
     * ConversationsFragment.ConversationsCallback Methods
     */
    @Override
    public void readContacts() {
        List<String> permissions = getNeededPermissions();
        if(permissions.size() > 0){
            requestPermissions(READ_CONTACTS_REQUEST, permissions);
        } else {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    final List<Conversation> conversations = new ArrayList<>();
                    String[] projection = new String[]{Telephony.Sms._ID, Telephony.Sms.ADDRESS,
                            Telephony.Sms.BODY, Telephony.Sms.DATE,
                            Telephony.Sms.DATE_SENT, Telephony.Sms.THREAD_ID,
                            Telephony.Sms.READ, Telephony.Sms.PERSON,
                            Telephony.Sms.TYPE, Telephony.Sms.STATUS};
                    Uri uri = Uri.parse("content://mms-sms/conversations/");
                    Cursor query = getContentResolver().query(uri, projection, null,
                            null, Telephony.Sms.DEFAULT_SORT_ORDER);
                    if(query != null){
                        while(query.moveToNext()){
                            Conversation conversation = null;
                            Sms sms;
                            int numOfConversations = conversations.size();
                            int threadId = query.getInt(query.getColumnIndex(projection[5]));
                            if(numOfConversations > 0){
                                for(int i = 0; i < numOfConversations; i++){
                                    Conversation compare = conversations.get(i);
                                    if(compare.getThreadId() == threadId){
                                        conversation = compare;
                                        break;
                                    }
                                }
                            }
                            sms = getSMS(query);
                            if(conversation == null){
                                conversation = new Conversation();
                                conversation.setThreadId(threadId);
                                conversation.setContact(getContact(sms));
                            }
                            conversation.getMessages().add(sms);
                            conversations.add(conversation);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mConversationsFragment.addConversations(conversations);
                            }
                        });
                        query.close();
                    }
                }
            });

            // TODO: Read all the conversations on a separate thread and post it on EventBus
        }
    }

    private Sms getSMS(Cursor cursor){
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

    private Contact getContact(Sms sms){
        Contact contact = new Contact();
        String address = sms.getAddress();
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.NORMALIZED_NUMBER,
                ContactsContract.PhoneLookup.PHOTO_URI, ContactsContract.PhoneLookup._ID};
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
        Cursor query = getContentResolver().query(uri, projection, null, null, null);
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

    @Override
    public void setDefault() {
        String pack = getPackageName();
        if(Telephony.Sms.getDefaultSmsPackage(this).equals(pack)){
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
            startActivityForResult(intent, CHANGE_DEFAULT_REQUEST);
        }
    }

    /**
     * Class Methods
     */
    private void initUIWidgets(){
        Toolbar toolbar = findViewById(R.id.am_toolbar);
        setSupportActionBar(toolbar);
    }

    private void initFragments(){
        mConversationsFragment = (ConversationsFragment)getSupportFragmentManager()
                .findFragmentByTag(ConversationsFragment.TAG);
        if(mConversationsFragment == null){
            mConversationsFragment = new ConversationsFragment();
        }
    }

    private List<String> getNeededPermissions(){
        List<String> permissions = new ArrayList<>();
        for(String permission: PERMISSIONS){
            if(checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
                permissions.add(permission);
            }
        }
        return permissions;
    }

    private void requestPermissions(final int requestCode, List<String> permissions) {
        Dexter.withActivity(this)
                .withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if(report.areAllPermissionsGranted()){
                            switch(requestCode){
                                case READ_CONTACTS_REQUEST:
                                    readContacts();
                                    break;
                            }
                        } else {
                            showSnackbar(R.string.permissions_rationale,
                                    R.string.action_settings, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts
                                                    ("package", BuildConfig.APPLICATION_ID, null);
                                            intent.setData(uri);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                   final PermissionToken token) {
                        new AlertDialog.Builder(MainActivity.this).setTitle
                                (R.string.permissions_rationale_title)
                                .setMessage(R.string.permissions_rationale)
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        token.cancelPermissionRequest();
                                    }
                                })
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        token.continuePermissionRequest();
                                    }
                                })
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override public void onDismiss(DialogInterface dialog) {
                                        token.cancelPermissionRequest();
                                    }
                                })
                                .show();
                    }
                }).check();
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(R.id.main_activity),
                getString(mainTextStringId),
                Snackbar.LENGTH_LONG)
                .setAction(getString(actionStringId), listener).show();
    }

}
