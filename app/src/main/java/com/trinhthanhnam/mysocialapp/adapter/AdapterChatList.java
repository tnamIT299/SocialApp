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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trinhthanhnam.mysocialapp.ChatActivity;
import com.trinhthanhnam.mysocialapp.R;
import com.trinhthanhnam.mysocialapp.model.ChatList;
import com.trinhthanhnam.mysocialapp.model.User;

import java.util.HashMap;
import java.util.List;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder>{
    Context context;
    List<User> userList;
    FirebaseAuth firebaseAuth;

    String myUid;
    private HashMap<String, String> lastMessageMap;

    public AdapterChatList(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
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
                // Kiểm tra xem người dùng hiện tại có bị chặn bởi người dùng khác hay không
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.child(hisUid).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    // Người dùng hiện tại bị chặn, hiển thị thông báo
                                    Toast.makeText(context, "You're blocked by that user,can't send message", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Người dùng hiện tại không bị chặn, mở ChatActivity
                                    Intent intent = new Intent(context, ChatActivity.class);
                                    intent.putExtra("hisUID", hisUid);
                                    context.startActivity(intent);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }
    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId, lastMessage);
    }


//    private void checkIsBlocked(String hisUID, AdapterUser.MyHolder holder, int position) {
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
//        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        for (DataSnapshot ds : snapshot.getChildren()){
//                            if(ds.exists()){
//                                holder.blockIv.setImageResource(R.drawable.ic_blocked);
//                                userList.get(position).setBlocked(true);
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//    }

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
