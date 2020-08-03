package com.example.monitorme.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.monitorme.R;

import java.util.ArrayList;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder>{

    // variables
    ArrayList<String> mediaList;
    Context context;

    // adapter
    public MediaAdapter(Context context, ArrayList<String> mediaList){
        this.context = context;
        this.mediaList = mediaList;
    }

    @NonNull
    @Override
    // inflater for view
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media,null, false);
        MediaViewHolder mediaViewHolder = new MediaViewHolder(layoutView);

        return mediaViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MediaViewHolder holder, int position) {
        Glide.with(context).load(Uri.parse(mediaList.get(position))).into(holder.mMedia);

        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaList.isEmpty())
                    return;
                mediaList.remove(holder.getAdapterPosition());
                notifyDataSetChanged();
            }
        });
    }

    @Override
    // get count of media list
    public int getItemCount() {
        return mediaList.size();
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {
        // declare variables
        ImageView mMedia;
        FrameLayout mLayout;

        public MediaViewHolder(View itemView) {
            super(itemView);
            // initialise variables depending on xml file
            mMedia = itemView.findViewById(R.id.media);
            mLayout = itemView.findViewById(R.id.layout);
        }
    }
}
