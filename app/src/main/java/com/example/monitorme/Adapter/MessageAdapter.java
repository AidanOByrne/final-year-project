package com.example.monitorme.Adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.monitorme.Object.ChatObject;
import com.example.monitorme.Object.MessageObject;
import com.example.monitorme.Object.UserObject;
import com.example.monitorme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    // decalre variables
    ArrayList<MessageObject> messageList;
    ChatObject chatObject;
    Context context;

    // initialise message adpater
    public MessageAdapter(Context context, ChatObject chatObject, ArrayList<MessageObject> messageList){
        this.messageList = messageList;
        this.chatObject = chatObject;
        this.context = context;
    }


    @NonNull
    @Override
    // creates a new view holder
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflates the layout of item message to the view point of the user
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, null, false);
        // assign layout params for the view
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // set params to view
        layoutView.setLayoutParams(lp);
        // assing it to the rcv
        MessageViewHolder rcv = new MessageViewHolder(layoutView);
        // return
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        // settting text of view holder
        holder.mMessage.setText(messageList.get(position).getMessage());
        // for each user in the chat object
        for (UserObject mUser : chatObject.getUserObjectArrayList()) {
            if (mUser.getUid().equals(messageList.get(position).getSenderId())) {
                String name = mUser.getName();
                holder.mSender.setText(name);
            }
        }
        holder.mDate.setText(messageList.get(position).getTimestampStr());

        // new params for the messages
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.mLayout.getLayoutParams();
        // assign which position the message goes
        // this is receviing messages
        if(messageList.get(position).getSenderId().equals(FirebaseAuth.getInstance().getUid())){
            params.leftMargin = 100;
            params.rightMargin = 0;
            holder.mLayout.setLayoutParams(params);
            holder.mLayout.setGravity(Gravity.END);
        }else{
            // this is sending messages
            params.rightMargin = 100;
            params.leftMargin = 0;
            holder.mLayout.setLayoutParams(params);
            holder.mLayout.setGravity(Gravity.START);
        }

        holder.mSender.setVisibility(View.VISIBLE);
        if (position > 0) {
            // setting most recent message viewable
            if (messageList.get(position - 1).getSenderId().equals(messageList.get(position).getSenderId())) {
                holder.mSender.setVisibility(View.GONE);
            }
        }

        // if media list emprty view gone
        if(messageList.get(holder.getAdapterPosition()).getMediaUrlList().isEmpty()){
            holder.mMediaLayout.setVisibility(View.GONE);
        }
        else{

            holder.mMediaLayout.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(messageList.get(position).getMediaUrlList().get(0))
                    .into(holder.mMedia);
            holder.mMediaAmount.setText(String.valueOf(messageList.get(position).getMediaUrlList().size()));
        }
        // set on click listener for media layout
        holder.mMediaLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get media
                new ImageViewer.Builder(v.getContext(), messageList.get(holder.getAdapterPosition()).getMediaUrlList())
                        .setStartPosition(0)
                        .show();
            }
        });

    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }


    class MessageViewHolder extends RecyclerView.ViewHolder{
        // delacre variables
        TextView    mMessage,
                mSender,
                mDate,
                mMediaAmount;
        ImageView mMedia;
        FrameLayout mMediaLayout;
        LinearLayout mLayout;
        CardView mCard;
        MessageViewHolder(View view){
            // initialise the variables with the correspnding values based on id
            super(view);
            mLayout = view.findViewById(R.id.layout);

            mMessage = view.findViewById(R.id.message);
            mSender = view.findViewById(R.id.sender);
            mDate = view.findViewById(R.id.date);
            mMediaAmount = view.findViewById(R.id.mediaAmount);

            mMediaLayout = view.findViewById(R.id.mediaLayout);
            mMedia = view.findViewById(R.id.media);

            mCard = view.findViewById(R.id.card);

        }
    }
}
