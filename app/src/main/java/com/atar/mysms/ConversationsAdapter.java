package com.atar.mysms;

import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/*
 * Created by Atar on 04-Feb-18.
 */

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationHolder> {

    /**
     * Constants
     */
    private static final int SECOND = 1000;
    private static final int MINUTE = SECOND * 60;
    private static final int HOUR = MINUTE * 60;

    /**
     * Data
     */
    private List<Conversation> mConversations;

    /**
     * Constructor
     */
    public ConversationsAdapter(List<Conversation> conversations){
        mConversations = conversations;
    }

    /**
     * Adapter Methods
     */
    @Override
    public ConversationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ConversationHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.conversation, parent, false));
    }

    @Override
    public void onBindViewHolder(ConversationHolder holder, int position) {
        Conversation conversation = mConversations.get(position);
        Contact contact = conversation.getContact();
        Sms sms = conversation.getMessages().get(0);

        holder.mName.setText(contact.getName() == null ? contact.getDisplayedPhoneNumber() : contact.getName());

        holder.mBody.setText(sms.getBody());

        int unread = conversation.getUnread();
        boolean isItUnread = unread > 0;
        holder.mBadge.setVisibility(isItUnread ? View.VISIBLE : View.GONE);
        String unreadString = unread + "";
        holder.mUnread.setText(unreadString);

        String imageUri = contact.getImageUri();
        String name = contact.getName();
        boolean canShowLetter = imageUri == null && name != null && !name.isEmpty();
        holder.mLetter.setVisibility(canShowLetter ? View.VISIBLE : View.INVISIBLE);

        if(canShowLetter){
            if(contact.getId() == -1){
                holder.mLetter.setVisibility(View.INVISIBLE);
                holder.mPhoto.setImageResource(R.drawable.non_contact);
            } else {
                holder.mLetter.setVisibility(View.VISIBLE);
                holder.mLetter.setText(name.substring(0, 1));
                holder.mPhoto.setImageResource(R.color.colorAccent);
            }
        } else if(imageUri == null){
            holder.mPhoto.setImageResource(R.drawable.def_account);
        } else {
            Glide.with(holder.itemView).load(Uri.parse(imageUri)).into(holder.mPhoto);
        }

        long lastMessageTime = sms.getTimestamp();
        long currentTimeMillis = System.currentTimeMillis();
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(currentTimeMillis);
        Calendar conversationTime = Calendar.getInstance();
        conversationTime.setTimeInMillis(lastMessageTime);
        String pattern = null;
        if(currentTime.get(Calendar.YEAR) != conversationTime.get(Calendar.YEAR)){
            pattern = "dd/MM/yy";
        } else if(currentTime.get(Calendar.MONTH) != conversationTime.get(Calendar.MONTH)){
            pattern = "MMM dd";
        } else if(currentTime.get(Calendar.WEEK_OF_YEAR) != conversationTime.get(Calendar.WEEK_OF_YEAR)){
            pattern = "MMM dd";
        } else if(currentTime.get(Calendar.DAY_OF_WEEK) != conversationTime.get(Calendar.DAY_OF_WEEK)){
            pattern = "EEE";
        } else if(currentTimeMillis - HOUR > lastMessageTime){
            long hoursAfter = (currentTimeMillis - lastMessageTime) / HOUR;
            String s = hoursAfter + " " + holder.mDate.getResources().getString(R.string.hours);
            holder.mDate.setText(s);
        } else if(currentTimeMillis - lastMessageTime > MINUTE){
            long minutesAfter = (currentTimeMillis - lastMessageTime) / MINUTE;
            String s = minutesAfter + " " + holder.mDate.getResources().getString(R.string.minutes);
            holder.mDate.setText(s);
        }
        if(pattern != null){
            holder.mDate.setText(parseDate(lastMessageTime, pattern));
        }

        holder.mBody.setTypeface(null, isItUnread ? Typeface.BOLD : Typeface.NORMAL);
        holder.mName.setTypeface(null, isItUnread ? Typeface.BOLD : Typeface.NORMAL);
    }

    @Override
    public int getItemCount() {
        return mConversations.size();
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
    class ConversationHolder extends RecyclerView.ViewHolder{

        /**
         * UI Widgets
         */
        private TextView mUnread, mLetter, mName, mBody, mDate;
        private ImageView mPhoto;
        private View mBadge;

        /**
         * Constructor
         */
        ConversationHolder(View itemView) {
            super(itemView);
            mUnread = itemView.findViewById(R.id.cv_unread);
            mLetter = itemView.findViewById(R.id.cv_letter);
            mName = itemView.findViewById(R.id.cv_name);
            mBody = itemView.findViewById(R.id.cv_text);
            mDate = itemView.findViewById(R.id.cv_date);
            mPhoto = itemView.findViewById(R.id.cv_pic);
            mBadge = itemView.findViewById(R.id.cv_badge);
        }
    }

}
