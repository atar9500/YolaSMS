package com.atar.mysms.ui;


import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.atar.mysms.structure.Contact;
import com.atar.mysms.structure.Conversation;
import com.atar.mysms.R;
import com.atar.mysms.structure.Sms;
import com.atar.mysms.structure.SmsReceivedEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessagingFragment extends Fragment {

    /**
     * Constants
     */
    public static final String TAG = MessagingFragment.class.getSimpleName();

    /**
     * Constructor
     */
    public MessagingFragment() {}

    /**
     * Data
     */
    private MessagingCallback mCallback;
    private Conversation mConversation;
    private MessagingAdapter mAdapter;

    /**
     * UI Widgets
     */
    private View mView, mEmpty;
    private RecyclerView mList;
    private EditText mField;
    private FloatingActionButton mSend;

    /**
     * Fragment Methods
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (MessagingCallback)context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setRetainInstance(true);

        if(savedInstanceState == null){
            mConversation = mCallback.getConversation();
        }

        mView = inflater.inflate(R.layout.fragment_messaging, container, false);

        initUIWidgets();

        return mView;
    }

    /**
     * Class Methods
     */
    private void initUIWidgets(){
        mAdapter = new MessagingAdapter(mConversation);
        mList = mView.findViewById(R.id.fm_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        mList.setLayoutManager(layoutManager);
        mList.setAdapter(mAdapter);
        mList.setHasFixedSize(true);

        mField = mView.findViewById(R.id.fm_field);
        mField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if(getContext() != null) {
                    mSend.setBackgroundTintList(ColorStateList.valueOf
                            (ContextCompat.getColor(getContext(), editable.length() > 0 ?
                            R.color.colorAccent : R.color.colorDeactive)));
                }
            }
        });

        mSend = mView.findViewById(R.id.fm_send);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mField.setText(null);
            }
        });

        mEmpty = mView.findViewById(R.id.fm_empty);
        mEmpty.setVisibility(mConversation.getMessages().isEmpty() ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * EventBus Methods
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSmsEvent(SmsReceivedEvent smsReceivedEvent){
        Sms sms = smsReceivedEvent.getSms();
        Contact contact = smsReceivedEvent.getContact();
        if(contact.equals(mConversation.getContact())){
            List<Sms> messages = mConversation.getMessages();
            messages.add(0, sms);
            mAdapter.notifyItemInserted(0);
            mEmpty.setVisibility(View.INVISIBLE);
            if(messages.size() > 1){
                mAdapter.notifyItemChanged(1);
            }
            mList.smoothScrollToPosition(0);
        }
    }

    /**
     * Inner Interfaces
     */
    interface MessagingCallback{
        Conversation getConversation();
    }

}
