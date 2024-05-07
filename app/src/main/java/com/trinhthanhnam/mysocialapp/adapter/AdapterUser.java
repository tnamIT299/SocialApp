package com.trinhthanhnam.mysocialapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.trinhthanhnam.mysocialapp.ChatActivity;
import com.trinhthanhnam.mysocialapp.R;
import com.trinhthanhnam.mysocialapp.ThereProfileActivity;
import com.trinhthanhnam.mysocialapp.model.User;

import java.util.List;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder> {
    Context mContext;
    List<User> userList;

    public AdapterUser(Context mContext, List<User> userList) {
        this.mContext = mContext;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_user,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
            //get data
        String hisUID = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String name = userList.get(position).getName();
        String email = userList.get(position).getEmail();

        //set data
        holder.nameTv.setText(name);
        holder.emailTv.setText(email);
        try{
            Glide.with(mContext)
                    .load(userImage)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .into(holder.avatarIv);
        }catch (Exception e){
            holder.avatarIv.setImageResource(R.drawable.baseline_account_circle_24);
        }

       // handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //alertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setItems(new String[] {"Profile", "Chat"}, (dialog, which) -> {
                    if (which == 0){
                        //profile clicked
                        Intent intent1 = new Intent(mContext, ThereProfileActivity.class);
                        intent1.putExtra("uid", hisUID);
                        mContext.startActivity(intent1);
                    }
                    if (which == 1){
                        //chat clicked
                        Intent intent2 = new Intent(mContext, ChatActivity.class);
                        intent2.putExtra("hisUID", hisUID);
                        mContext.startActivity(intent2);
                    }
                });
                builder.create().show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //View holder class
    class MyHolder extends RecyclerView.ViewHolder {
        ImageView avatarIv;
        TextView nameTv, emailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //init views
            avatarIv = itemView.findViewById(R.id.imvAvatar);
            nameTv = itemView.findViewById(R.id.tvName);
            emailTv = itemView.findViewById(R.id.tvEmail);
        }
    }
}
