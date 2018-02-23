package com.atar.mysms.ui;

/*
 * Created by Atar on 23-Feb-18.
 */

import android.net.Uri;
import android.provider.Telephony;
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

    public MessagingAdapter(Conversation conversation){
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
    public void onBindViewHolder(SmsHolder holder, int position) {
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
            holder.mDate.setText(s);
            patternBottom = "HH:mm";
        } else if(currentTimeMillis - lastMessageTime > MINUTE){
            long minutesAfter = (currentTimeMillis - lastMessageTime) / MINUTE;
            String s = minutesAfter + " " + holder.mDate.getResources().getString(R.string.minutes);
            holder.mDate.setText(s);
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
        private ImageView mPhoto;
        private TextView mBody, mDateTop, mDate;

        /**
         * Constructor
         */
        public SmsHolder(View itemView, int type) {
            super(itemView);
            if(type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX){
                mPhoto = itemView.findViewById(R.id.im_photo);
                mBody = itemView.findViewById(R.id.im_body);
                mDate = itemView.findViewById(R.id.im_date);
                mDateTop = itemView.findViewById(R.id.im_date_top);
            } else {
                mBody = itemView.findViewById(R.id.om_body);
                mDate = itemView.findViewById(R.id.om_date);
                mDateTop = itemView.findViewById(R.id.om_date_top);
            }
        }
    }

}
