package com.trinhthanhnam.mysocialapp.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.trinhthanhnam.mysocialapp.ChatActivity;
import com.trinhthanhnam.mysocialapp.R;
import com.trinhthanhnam.mysocialapp.model.Chat;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder>{
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<Chat> chatList;
    String imageUrl;
    FirebaseUser fUser;

    public AdapterChat(Context context, List<Chat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
        this.fUser = fUser;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") final int position) {
        //get data
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();
        String type = chatList.get(position).getType();
        //convert time stamp to dd/mm/yyyy hh:mm am/pm

        long onlineStatusTime = Long.parseLong(timeStamp);
        String dateTime = (String) DateUtils.getRelativeTimeSpanString(onlineStatusTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);

        //set data
         if(type.equals("text")) {
             //text message
             holder.messageTv.setVisibility(View.VISIBLE);
             holder.messageIv.setVisibility(View.GONE);
             holder.messageTv.setText(message);
         }else{
                //image message
                holder.messageTv.setVisibility(View.GONE);
                holder.messageIv.setVisibility(View.VISIBLE);
                Glide.with(context).load(message).into(holder.messageIv);
         }

        holder.timeTv.setText(dateTime);
        try{
            Picasso.get().load(imageUrl).into(holder.profileIv);
        }catch (Exception e){
            //there is exception getting picture
        }
        //click to show delete dialog
        holder.messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss dialog
                        dialog.dismiss();
                    }
                });
                //create and show dialog
                builder.create().show();
                return true;
            }
        });
        holder.messageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image in dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                //set image in dialog
                ImageView imageView = new ImageView(context);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(1200, 1200);
                imageView.setLayoutParams(layoutParams);
                Glide.with(context).load(message).into(imageView);
                builder.setView(imageView);
                //set cancel button
                builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                //show dialog
                builder.create().show();
            }
        });

        //set seen/delivered status of message
        if(position == chatList.size()-1) {
            if (chatList.get(position).isSeen()) {
                if (chatList.get(position).isSeen()){
                    holder.isSeenTv.setText("Seen");
                } else{
                    holder.isSeenTv.setText("Delivered");
                }
            } else {
                holder.isSeenTv.setVisibility(View.GONE);
            }
        }
    }

    private void deleteMessage(int position) {
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String messageTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timeStamp").equalTo(messageTimeStamp);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    if(ds.child("sender").getValue().equals(myUID)){
                        //set message "This message was deleted..."
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was deleted...");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "Message deleted...", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(context, "You can delete only your messages...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{
        //views
        ImageView profileIv, messageIv;
        TextView messageTv, timeTv, isSeenTv;
        RelativeLayout messageLayout; //for click listener to show delete

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            profileIv = itemView.findViewById(R.id.profileIv);
            messageIv = itemView.findViewById(R.id.messageIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv= itemView.findViewById(R.id.seenTv);
            messageLayout = itemView.findViewById(R.id.messageLayout);
        }
    }
}
