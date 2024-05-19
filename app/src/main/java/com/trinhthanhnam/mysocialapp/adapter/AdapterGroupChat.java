package com.trinhthanhnam.mysocialapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.trinhthanhnam.mysocialapp.R;
import com.trinhthanhnam.mysocialapp.model.GroupChat;

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
