package com.trinhthanhnam.mysocialapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trinhthanhnam.mysocialapp.GroupChatActivity;
import com.trinhthanhnam.mysocialapp.R;
import com.trinhthanhnam.mysocialapp.model.GroupChat;
import com.trinhthanhnam.mysocialapp.notifications.Data;

import java.util.ArrayList;
import java.util.List;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.MyHolder> {
    private Context context;
    private ArrayList<GroupChat> groupChatList;

    public AdapterGroupChat(Context context, ArrayList<GroupChat> groupChatList) {
        this.context = context;
        this.groupChatList = groupChatList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.row_groupchat, null);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        GroupChat groupChat = groupChatList.get(position);
        String groupId = groupChat.getGroupId();
        String groupTitle = groupChat.getGroupTitle();
        String groupIcon = groupChat.getGroupIcon();
        String time = groupChat.getTimestamp();

        holder.senderNameTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");
        loadLastMessage(groupChat,holder);
        holder.groupNameTv.setText(groupTitle);
        //holder.timeTv.setText();
        try{
            Glide.with(context).load(groupIcon).placeholder(R.drawable.baseline_groups_24).into(holder.groupIconIv);
        }catch (Exception e){
            holder.groupIconIv.setImageResource(R.drawable.baseline_groups_24);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open group chat
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId",groupId);
                context.startActivity(intent);
            }
        });
    }

    private void loadLastMessage(GroupChat groupChat, MyHolder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupChat.getGroupId()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            String message = ""+ds.child("message").getValue();
                            String timeStamp = ""+ds.child("timeStamp").getValue();
                            String sender = ""+ds.child("sender").getValue();

                            long senderTime = Long.parseLong(timeStamp);
                            String dateTime = (String) DateUtils.getRelativeTimeSpanString(senderTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
                            holder.messageTv.setText(message);
                            holder.timeTv.setText(dateTime);
                            //get info of last sender
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()){
                                                String name = ""+ds.child("name").getValue();
                                                holder.senderNameTv.setText(name);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatList.size();
    }


    class MyHolder extends RecyclerView.ViewHolder {
        ImageView groupIconIv;
        TextView groupNameTv, senderNameTv, messageTv, timeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            groupIconIv = itemView.findViewById(R.id.groupIconIv);
            groupNameTv = itemView.findViewById(R.id.groupNameTv);
            senderNameTv = itemView.findViewById(R.id.senderNameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);

        }
    }
}
