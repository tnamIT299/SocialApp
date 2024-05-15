package com.trinhthanhnam.mysocialapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.trinhthanhnam.mysocialapp.ChatActivity;
import com.trinhthanhnam.mysocialapp.R;
import com.trinhthanhnam.mysocialapp.model.ChatList;
import com.trinhthanhnam.mysocialapp.model.User;

import java.util.HashMap;
import java.util.List;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder>{
    Context context;
    List<User> userList;
    private HashMap<String, String> lastMessageMap;

    public AdapterChatList(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String hisUid = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String lastMessage = lastMessageMap.get(hisUid);

        holder.nameTv.setText(userName);
        if (lastMessage == null || lastMessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);
        }else {
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }
        try{
            //set user image
            Glide.with(context)
                    .load(userImage)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .into(holder.profileIv);
        }catch (Exception e){
            Glide.with(context).load(R.drawable.baseline_account_circle_24).into(holder.profileIv);
        }
        if(userList.get(position).getOnlineStatus().equals("online")){
           Glide.with(context).load(R.drawable.circle_online).into(holder.onlineStatus);
        }else {
            Glide.with(context).load(R.drawable.circle_offline).into(holder.onlineStatus);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUID", hisUid);
                context.startActivity(intent);
            }
        });

    }
    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        //views of row_chatlist.xml
        ImageView profileIv, blockIv, onlineStatus;
        TextView nameTv, lastMessageTv, timeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatus = itemView.findViewById(R.id.onlineStatus);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastmsgTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
