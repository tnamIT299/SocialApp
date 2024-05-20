package com.trinhthanhnam.mysocialapp.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trinhthanhnam.mysocialapp.R;
import com.trinhthanhnam.mysocialapp.model.GroupChat;
import com.trinhthanhnam.mysocialapp.model.Group_Chat;
import com.trinhthanhnam.mysocialapp.notifications.Data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroup_Chat extends RecyclerView.Adapter<AdapterGroup_Chat.HolderGroup_Chat> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private ArrayList<Group_Chat> Group_ChatList;
    private FirebaseAuth firebaseAuth;

    public AdapterGroup_Chat(Context context, ArrayList<Group_Chat> group_ChatList) {
        this.context = context;
        Group_ChatList = group_ChatList;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderGroup_Chat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right,parent,false);
            return new HolderGroup_Chat(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left,parent,false);
            return new HolderGroup_Chat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroup_Chat holder, int position) {
        Group_Chat model = Group_ChatList.get(position);
        String message = model.getMessage();
        String timeStamp = model.getTimeStamp();
        String senderUid = model.getSender();

        long onlineStatusTime = Long.parseLong(timeStamp);
        String dateTime = (String) DateUtils.getRelativeTimeSpanString(onlineStatusTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
        holder.messageTv.setText(message);
        holder.timeTv.setText(dateTime);
        setUserName(model,holder);

    }

    private void setUserName(Group_Chat model, HolderGroup_Chat holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            holder.nameTv.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return Group_ChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(Group_ChatList.get(position).getSender().equals(firebaseAuth.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }
    }

    class HolderGroup_Chat extends RecyclerView.ViewHolder{
        private TextView nameTv,messageTv,timeTv;
        public HolderGroup_Chat(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
