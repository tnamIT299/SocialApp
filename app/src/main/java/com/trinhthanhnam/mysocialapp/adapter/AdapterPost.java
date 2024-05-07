package com.trinhthanhnam.mysocialapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.trinhthanhnam.mysocialapp.R;
import com.trinhthanhnam.mysocialapp.ThereProfileActivity;
import com.trinhthanhnam.mysocialapp.model.Post;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyHolder> {
    Context context;
    List<Post> postList;

    public AdapterPost(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_post, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
            String uid = postList.get(position).getUid();
            String uEmail = postList.get(position).getuEmail();
            String uName = postList.get(position).getuName();
            String uDp = postList.get(position).getuDp();
            String pId = postList.get(position).getpId();
            String pTitle = postList.get(position).getpTitle();
            String pDescr = postList.get(position).getpDescr();
            String pImage = postList.get(position).getpImage();
            String pTimeStamp= postList.get(position).getpTime();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //set data
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescr);


        //set user
        Glide.with(context)
                .load(uDp)
                .placeholder(R.drawable.baseline_account_circle_24)
                .into(holder.uPictureIv);

        // Use Glide to load the post image if it exists
        if(!pImage.equals("noImage")){
            Glide.with(context)
                    .load(pImage)
                    .into(holder.pImageIv);
        } else {
            holder.pImageIv.setVisibility(View.GONE);
        }


        //handle item click
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder
    class MyHolder extends RecyclerView.ViewHolder{
        ImageView uPictureIv, pImageIv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv;
        ImageButton moreBtn;
        Button likeBtn, commentBtn, shareBtn;

        LinearLayout profileLayout;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //init views
            uPictureIv = itemView.findViewById(R.id.postUImg);
            pImageIv = itemView.findViewById(R.id.postImageIv);
            uNameTv = itemView.findViewById(R.id.postUName);
            pTimeTv = itemView.findViewById(R.id.postTime);
            pTitleTv = itemView.findViewById(R.id.postTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.postDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.postLikeTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.btnLike);
            commentBtn = itemView.findViewById(R.id.btnComment);
            shareBtn = itemView.findViewById(R.id.btnShare);
            profileLayout = itemView.findViewById(R.id.profileLayout);


        }
    }
}
