package com.trinhthanhnam.mysocialapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.trinhthanhnam.mysocialapp.NotificationFragment;
import com.trinhthanhnam.mysocialapp.PostDetailActivity;
import com.trinhthanhnam.mysocialapp.R;
import com.trinhthanhnam.mysocialapp.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.HolderNotification> {

    private Context context;
    private ArrayList<Notification> notificationList;
    private FirebaseAuth firebaseAuth;

    public AdapterNotification(Context context, ArrayList<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderNotification onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_notification, parent, false);

        return new HolderNotification(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderNotification holder, @SuppressLint("RecyclerView") int position) {
        //Notification notification = notificationList.get(position);
        String name = notificationList.get(position).getsName();
        String notificationStr = notificationList.get(position).getNotification();
        String image = notificationList.get(position).getsImage();
        String timestamp = notificationList.get(position).getTimestamp();
        String senderUid = notificationList.get(position).getsUid();
        String pId = notificationList.get(position).getpId();

        long onlineStatusTime = Long.parseLong(timestamp);
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(onlineStatusTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(senderUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String image = "" + ds.child("image").getValue();

                    //add to model
                    notificationList.get(position).setsName(name);
                    notificationList.get(position).setsImage(image);
                    //set to views
                    holder.nameTv.setText(name);
                    if(image != null && !image.isEmpty()) {
                        Picasso.get().load(image).into(holder.avatarIv);
                    } else {
                        //load default image
                        Picasso.get().load(R.drawable.logo).into(holder.avatarIv);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.notificationTv.setText(notificationStr);
        holder.timeTv.setText(timeAgo);
        try {
            Glide.with(context).load(image).into(holder.avatarIv);
        } catch (Exception e) {
            holder.avatarIv.setImageResource(R.drawable.baseline_account_circle_24);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //delete
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete this notification?");
                builder.setPositiveButton("Delete", (dialog, which) -> {
                    // Remove the item from the list and notify the adapter
                    Notification notification = notificationList.remove(position);
                    notifyItemRemoved(position);

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.child(firebaseAuth.getUid()).child("Notifications").child(notification.getTimestamp()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // If there's an error, add the item back to the list and notify the adapter
                            notificationList.add(position, notification);
                            notifyItemInserted(position);
                            Toast.makeText(context, "Error"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });
                builder.create().show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    class HolderNotification extends RecyclerView.ViewHolder {
        ImageView avatarIv;
        TextView nameTv, notificationTv, timeTv;

        public HolderNotification(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            notificationTv = itemView.findViewById(R.id.notificationTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
