package com.trinhthanhnam.mysocialapp.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.trinhthanhnam.mysocialapp.R;
import com.trinhthanhnam.mysocialapp.model.Group_Chat;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterGroup_Chat extends RecyclerView.Adapter<AdapterGroup_Chat.HolderGroup_Chat> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private String groupId;
    private ArrayList<Group_Chat> Group_ChatList;
    private FirebaseAuth firebaseAuth;

    public AdapterGroup_Chat(Context context, String groupId, ArrayList<Group_Chat> group_ChatList) {
        this.context = context;
        this.groupId = groupId;
        Group_ChatList = group_ChatList;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderGroup_Chat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right, parent, false);
            return new HolderGroup_Chat(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left, parent, false);
            return new HolderGroup_Chat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroup_Chat holder, @SuppressLint("RecyclerView") int position) {
        Group_Chat model = Group_ChatList.get(position);
        String message = model.getMessage();
        String timeStamp = model.getTimeStamp();
        String senderUid = model.getSender();
        String type = model.getType();

        long senderTime = Long.parseLong(timeStamp);
        String dateTime = (String) DateUtils.getRelativeTimeSpanString(senderTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);

        if (type.equals("text")) {
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setText(message);
        } else {
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);
            Glide.with(context).load(message).into(holder.messageIv);
        }

        holder.timeTv.setText(dateTime);
        holder.messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDeleteDialog(position, senderUid);
                return true;
            }
        });

        setUserName(model, holder);
    }

    private void showDeleteDialog(int position, String senderUid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete");
        builder.setMessage("Are you sure you want to delete this message?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (firebaseAuth.getCurrentUser() != null && senderUid.equals(firebaseAuth.getUid())) {
                    deleteMessage(position);
                } else {
                    Toast.makeText(context, "You can delete only your messages...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Đóng dialog
                dialog.dismiss();
            }
        });

        // Tạo và hiển thị dialog
        builder.create().show();
    }

    private void deleteMessage(int position) {
        String messageTimeStamp = Group_ChatList.get(position).getTimeStamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Groups");
        DatabaseReference messageRef = dbRef.child(groupId).child("Messages");
        Query query = messageRef.orderByChild("timeStamp").equalTo(messageTimeStamp);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().removeValue();
                    Toast.makeText(context, "This message was deleted...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUserName(Group_Chat model, HolderGroup_Chat holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();
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
        if (Group_ChatList.get(position).getSender().equals(firebaseAuth.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class HolderGroup_Chat extends RecyclerView.ViewHolder {
        private TextView nameTv, messageTv, timeTv;
        private ImageView messageIv;
        RelativeLayout messageLayout;

        public HolderGroup_Chat(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            messageIv = itemView.findViewById(R.id.messageIv);
            messageLayout = itemView.findViewById(R.id.messageLayout);
        }
    }
}
