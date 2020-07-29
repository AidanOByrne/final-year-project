package com.example.monitorme.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.monitorme.Activity.FindUserActivity;
import com.example.monitorme.Object.UserObject;
import com.example.monitorme.R;

import java.util.ArrayList;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {

    Context context;
    private ArrayList<UserObject> userList;
    private boolean selectingMultipleUsers = false;


    public UserListAdapter(Context context, ArrayList<UserObject> userList){
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        UserListViewHolder rcv = new UserListViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final UserListViewHolder holder, final int position) {
        holder.mName.setText(userList.get(position).getName());
        holder.mDescription.setText(userList.get(position).getStatus());
        if( context != null ) {
            if (userList.get(holder.getAdapterPosition()).getImage().equals("")) {
                holder.mImage.setBackground(context.getResources().getDrawable(R.drawable.ic_user));
            } else {
                Glide.with(context)
                        .load(userList.get(holder.getLayoutPosition()).getImage())
                        .apply(RequestOptions.circleCropTransform().circleCrop())
                        .into(holder.mImage);
            }
        }
        holder.mLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                updateUserClick(holder);
                return true;
            }
        });
        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectingMultipleUsers)
                    updateUserClick(holder);
                else{
                    userList.get(position).setSelected(true);
                    ((FindUserActivity)context).createChat();
                }

            }
        });
    }

    private void updateUserClick(UserListViewHolder holder){
        int position = holder.getAdapterPosition();

        userList.get(position).setSelected(!userList.get(position).getSelected());

        if(userList.get(position).getSelected())
            holder.mSelected.setBackground(context.getResources().getDrawable(R.drawable.ic_check));
        else
            holder.mSelected.setBackground(null);

        updateSelectingMultipleUsers();
    }

    private void updateSelectingMultipleUsers() {
        for(UserObject mUser : userList){
            if(mUser.getSelected()){
                selectingMultipleUsers = true;
                return;
            }
        }
        selectingMultipleUsers = false;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }



    class UserListViewHolder extends RecyclerView.ViewHolder{
        TextView mName, mDescription;
        ImageView mImage, mSelected;
        LinearLayout mLayout;
        UserListViewHolder(View view){
            super(view);
            mName = view.findViewById(R.id.name);
            mDescription = view.findViewById(R.id.description);
            mImage = view.findViewById(R.id.image);
            mSelected = view.findViewById(R.id.selected);
            mLayout = view.findViewById(R.id.layout);
        }
    }
}
