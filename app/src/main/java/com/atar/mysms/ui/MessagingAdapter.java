package com.atar.mysms.ui;

/*
 * Created by Atar on 23-Feb-18.
 */

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.atar.mysms.structure.Contact;
import com.atar.mysms.structure.Conversation;
import com.atar.mysms.R;
import com.atar.mysms.structure.Sms;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessagingAdapter extends RecyclerView.Adapter<MessagingAdapter.SmsHolder> {

    /**
     * Constants
     */
    private static final int SECOND = 1000;
    private static final int MINUTE = SECOND * 60;
    private static final int HOUR = MINUTE * 60;

    /**
     * Data
     */
    private List<Sms> mSmsMessages;
    private Contact mContact;

    MessagingAdapter(Conversation conversation){
        mSmsMessages = conversation.getMessages();
        mContact = conversation.getContact();
    }

    /**
     * RecyclerView.Adapter Methods
     */
    @Override
    public SmsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        boolean isSent = viewType != Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX;
        return new SmsHolder(LayoutInflater.from(parent.getContext())
                .inflate(isSent ? R.layout.outgoing_message : R.layout.incoming_message,
                parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(final SmsHolder holder, int position) {
        Sms sms = mSmsMessages.get(position);

        holder.mBody.setText(sms.getBody());

        long lastMessageTime = sms.getTimestamp();
        long currentTimeMillis = System.currentTimeMillis();
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(currentTimeMillis);
        Calendar conversationTime = Calendar.getInstance();
        conversationTime.setTimeInMillis(lastMessageTime);
        String patternTop = null;
        String patternBottom = null;
        if(currentTime.get(Calendar.YEAR) != conversationTime.get(Calendar.YEAR)){
            patternTop = "E, MMM d yyyy · HH:mm";
            patternBottom = "MMM d yyyy, HH:mm";
        } else if(currentTime.get(Calendar.MONTH) != conversationTime.get(Calendar.MONTH)){
            patternTop = "MMM dd · HH:mm";
            patternBottom = "MMM d, HH:mm";
        } else if(currentTime.get(Calendar.WEEK_OF_YEAR) != conversationTime.get(Calendar.WEEK_OF_YEAR)){
            patternTop = "MMM dd · HH:mm";
            patternBottom = "MMM d, HH:mm";
        } else if(currentTime.get(Calendar.DAY_OF_WEEK) != conversationTime.get(Calendar.DAY_OF_WEEK)){
            patternTop = "EEE · HH:mm";
            patternBottom = "EEE, HH:mm";
        } else if(currentTimeMillis - HOUR > lastMessageTime){
            long hoursAfter = (currentTimeMillis - lastMessageTime) / HOUR;
            String s = hoursAfter + " " + holder.mDate.getResources().getString(R.string.hours);
            holder.mDateTop.setText(s);
            patternBottom = "HH:mm";
        } else if(currentTimeMillis - lastMessageTime > MINUTE){
            long minutesAfter = (currentTimeMillis - lastMessageTime) / MINUTE;
            String s = minutesAfter + " " + holder.mDate.getResources().getString(R.string.minutes);
            holder.mDateTop.setText(s);
            patternBottom = "HH:mm";
        }
        if(patternTop != null){
            holder.mDateTop.setText(parseDate(lastMessageTime, patternTop));
        }
        holder.mDate.setText(parseDate(lastMessageTime, patternBottom));

        holder.mDateTop.setVisibility(position == mSmsMessages.size() - 1 ||
                sms.getTimestamp() - mSmsMessages.get(position + 1)
                .getTimestamp() > 10 * MINUTE ? View.VISIBLE : View.GONE);

        holder.mDate.setVisibility(position == 0 || sms.isSentSMS() != mSmsMessages
                .get(position - 1).isSentSMS() ? View.VISIBLE : View.GONE);

        boolean isBubble = position == mSmsMessages.size() - 1 ||
                sms.getType() != mSmsMessages.get(position + 1).getType() ||
                sms.getTimestamp() - mSmsMessages.get(position + 1)
                        .getTimestamp() > 10 * MINUTE;
        if(sms.isSentSMS()){
            holder.mBody.setBackgroundResource(isBubble ? R.drawable.outgoing_bubble : R.drawable.outgoing_msg);
        } else {
            holder.mBody.setBackgroundResource(isBubble ? R.drawable.incoming_bubble : R.drawable.incoming_msg);
        }

        if(!sms.isSentSMS()){
            String imageUri = mContact.getImageUri();
            if(imageUri == null){
                holder.mPhoto.setImageResource(R.drawable.non_contact);
            } else {
                Glide.with(holder.itemView).load(Uri.parse(imageUri)).into(holder.mPhoto);
            }
            holder.mPhoto.setVisibility(isBubble ? View.VISIBLE : View.INVISIBLE);
        }

        holder.mCard.setVisibility((sms.isMms() ? View.VISIBLE : View.GONE));
        if(sms.isMms()){
            holder.mBody.setVisibility(sms.getBody() == null || sms.getBody().isEmpty() ? View.GONE : View.VISIBLE);
            String imageUri = sms.getImageUri();
            boolean isThereImageUri = imageUri != null && !imageUri.isEmpty();
            holder.mCard.setVisibility(isThereImageUri ? View.VISIBLE : View.GONE);
            if(isThereImageUri){
                holder.mLoadingMms.setVisibility(View.VISIBLE);
                Glide.with(holder.itemView)
                        .load(imageUri)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e,
                                    Object model, Target<Drawable> target, boolean isFirstResource) {
                                holder.mLoadingMms.setVisibility(View.GONE);
                                holder.mError.setVisibility(View.VISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model,
                                    Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.mLoadingMms.setVisibility(View.GONE);
                                holder.mError.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(holder.mMms);
            } else {
                Glide.with(holder.itemView).clear(holder.mMms);
            }
        }

    }

    @Override
    public int getItemCount() {
        return mSmsMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mSmsMessages.get(position).getType();
    }

    /**
     * Class Methods
     */
    private String parseDate(long timestamp, String pattern){
        try{
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
            Date netDate = (new Date(timestamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }

    /**
     * Inner Classes
     */
    class SmsHolder extends RecyclerView.ViewHolder{

        /**
         * UI Widgets
         */
        private ImageView mPhoto, mMms;
        private TextView mBody, mDateTop, mDate;
        private View mCard, mLoadingMms, mError;

        /**
         * Constructor
         */
        SmsHolder(View itemView, int type) {
            super(itemView);
            if(type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX){
                mPhoto = itemView.findViewById(R.id.im_photo);
                mBody = itemView.findViewById(R.id.im_body);
                mDate = itemView.findViewById(R.id.im_date);
                mDateTop = itemView.findViewById(R.id.im_date_top);
                mMms = itemView.findViewById(R.id.im_mms);
                mCard = itemView.findViewById(R.id.im_card);
                mLoadingMms = itemView.findViewById(R.id.im_loading);
                mError = itemView.findViewById(R.id.im_error_mms);
            } else {
                mBody = itemView.findViewById(R.id.om_body);
                mDate = itemView.findViewById(R.id.om_date);
                mDateTop = itemView.findViewById(R.id.om_date_top);
                mMms = itemView.findViewById(R.id.om_mms);
                mCard = itemView.findViewById(R.id.om_card);
                mLoadingMms = itemView.findViewById(R.id.om_loading);
                mError = itemView.findViewById(R.id.om_error_mms);
            }
        }
    }

}
