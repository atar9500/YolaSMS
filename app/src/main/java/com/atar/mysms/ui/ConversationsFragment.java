package com.atar.mysms.ui;

import android.content.Context;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.atar.mysms.structure.Conversation;
import com.atar.mysms.R;
import com.labo.kaji.fragmentanimations.MoveAnimation;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ConversationsFragment extends Fragment {

    /**
     * Tag
     */
    public static final String TAG = ConversationsFragment.class.getSimpleName();

    /**
     * Constructor
     */
    public ConversationsFragment() {}

    /**
     * Data
     */
    private List<Conversation> mConversations;
    private Conversation mClickedConversation;
    private ConversationsCallback mCallback;
    boolean mIsAttaching;

    /**
     * UI Widgets
     */
    private View mView, mEmpty, mSetDefault;
    private ConversationsAdapter mAdapter;

    /**
     * Fragment Methods
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof ConversationsCallback){
            mCallback = (ConversationsCallback)context;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setRetainInstance(true);

        if(mConversations == null){
            mConversations = new ArrayList<>();
            mCallback.readConversations();
        }

        mView = inflater.inflate(R.layout.fragment_conversations, container, false);

        initUIWidgets();

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mIsAttaching = true;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if(mIsAttaching){
            mIsAttaching = false;
            return super.onCreateAnimation(transit, enter, nextAnim);
        } else {
            return MoveAnimation.create(enter ? MoveAnimation.RIGHT : MoveAnimation.LEFT, enter, 350);
        }
    }

    /**
     * Class Methods
     */
    private void initUIWidgets(){

        if(mAdapter == null){
            mAdapter = new ConversationsAdapter(mConversations, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Object object = view.getTag();
                    if(object instanceof Conversation){
                        mClickedConversation = (Conversation)object;
                        mCallback.onConversationClick();
                    }
                }
            });
        }

        RecyclerView list = mView.findViewById(R.id.fc_list);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        list.setAdapter(mAdapter);
        list.setHasFixedSize(true);

        FloatingActionButton compose = mView.findViewById(R.id.fc_compose);
        compose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mEmpty = mView.findViewById(R.id.fc_empty);
        mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);

        mSetDefault = mView.findViewById(R.id.fc_set_default);
        mSetDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCallback != null){
                    mCallback.setDefault();
                }
            }
        });
        updateSetDefaultView();

    }

    private void updateSetDefaultView(){
        if(getContext() != null){
            boolean isDefault = Telephony.Sms.getDefaultSmsPackage(getContext()).equals(getContext().getPackageName());
            if(isDefault && mSetDefault.isShown()){
                mSetDefault.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
            } else if(!isDefault && !mSetDefault.isShown()){
                mSetDefault.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
            }
            mSetDefault.setVisibility(!isDefault ? View.VISIBLE : View.GONE);
            mSetDefault.setEnabled(!isDefault);
        }
    }

    public void addConversations(List<Conversation> conversations){
        int previousSize = mConversations.size();
        mConversations.clear();
        mAdapter.notifyItemRangeRemoved(0, previousSize);
        mConversations.addAll(conversations);
        mAdapter.notifyItemRangeInserted(0, mConversations.size());
        if(mAdapter.getItemCount() > 0){
            if(!mEmpty.isShown()){
                mEmpty.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
            }
            mEmpty.setVisibility(View.INVISIBLE);
        } else {
            if(mEmpty.isShown()){
                mEmpty.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
            }
            mEmpty.setVisibility(View.VISIBLE);
        }
    }

    public Conversation getClickedConversation(){
        return mClickedConversation;
    }

    /**
     * EventBus Methods
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConversationEvent(Conversation conversation) {
        mConversations.add(conversation);
        mAdapter.notifyItemInserted(0);
        if(mEmpty.isShown()){
            mEmpty.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
        }
        mEmpty.setVisibility(View.VISIBLE);
    }

    /**
     * Inner Interfaces
     */
    public interface ConversationsCallback{
        void readConversations();
        void setDefault();
        void onConversationClick();
    }

}
