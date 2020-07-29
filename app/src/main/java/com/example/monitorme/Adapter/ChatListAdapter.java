package com.example.monitorme.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.monitorme.Activity.ChatActivity;
import com.example.monitorme.Activity.MainActivity;
import com.example.monitorme.Object.ChatObject;
import com.example.monitorme.Object.UserObject;
import com.example.monitorme.R;
import com.example.monitorme.Utils.SendMessage;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    ArrayList<ChatObject> chatList;
    ArrayList<UserObject> userList;
    Context context;
    boolean mainTab;

    public ChatListAdapter(Context context, ArrayList<ChatObject> chatList,ArrayList<UserObject> userList, boolean mainTab){
        this.chatList = chatList;
        this.userList = userList;
        this.mainTab = mainTab;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        ChatListViewHolder rcv = new ChatListViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatListViewHolder holder, final int position) {
        Collections.sort(chatList);
        if(chatList.get(position).getName()==null)
            return;

        if(!chatList.get(position).getName().equals(""))
            holder.mName.setText(chatList.get(position).getName());
        else
            holder.mName.setText(chatList.get(position).getNameByUsers());

        holder.mMessage.setText(chatList.get(position).getLastMessage());
        holder.mTimestamp.setText(chatList.get(position).getTimestampStr());

        if( context != null ) {
            if (chatList.get(holder.getAdapterPosition()).getImage().equals("")) {
                Glide.with(context)
                        .load(context.getResources().getDrawable(R.drawable.ic_user))
                        .apply(RequestOptions.circleCropTransform().circleCrop())
                        .into(holder.mImage);
                if (chatList.get(holder.getAdapterPosition()).getUserObjectArrayList().size() == 2) {
                    for (UserObject mUser :  chatList.get(position).getUserObjectArrayList()){
                        if (!mUser.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                            Glide.with(context)
                                    .load(mUser.getImage())
                                    .apply(RequestOptions.circleCropTransform().circleCrop())
                                    .into(holder.mImage);
                        }
                    }
                }
            }else {
                Glide.with(context)
                        .load(chatList.get(holder.getLayoutPosition()).getImage())
                        .apply(RequestOptions.circleCropTransform().circleCrop())
                        .into(holder.mImage);
            }
        }

        if(mainTab){
            holder.mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                    intent.putExtra("chatObject", chatList.get(holder.getAdapterPosition()));
                    v.getContext().startActivity(intent);
                }
            });
        }else{
            holder.mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SendMessage(new ChatObject(chatList.get(holder.getAdapterPosition()).getChatId()), false, null,((MainActivity)context).getBitmapToSend(), "");
                    ((MainActivity)context).clearBackStack();
                }
            });
            holder.mTimestamp.setVisibility(View.GONE);
            holder.mMessage.setVisibility(View.INVISIBLE);
            holder.mName.setPadding(0,30,0,0);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }



    class ChatListViewHolder extends RecyclerView.ViewHolder{
        TextView    mName,
                mMessage,
                mTimestamp;
        ImageView mImage;
        RelativeLayout mLayout;
        ChatListViewHolder(View view){
            super(view);

            mName = view.findViewById(R.id.name);
            mMessage = view.findViewById(R.id.message);
            mTimestamp = view.findViewById(R.id.timestamp);
            mImage = view.findViewById(R.id.image);
            mLayout = view.findViewById(R.id.layout);
        }
    }
}
