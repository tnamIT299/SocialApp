package com.trinhthanhnam.mysocialapp.adapter;

import android.annotation.SuppressLint;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.trinhthanhnam.mysocialapp.ChatActivity;
import com.trinhthanhnam.mysocialapp.R;
import com.trinhthanhnam.mysocialapp.ThereProfileActivity;
import com.trinhthanhnam.mysocialapp.model.User;

import java.util.HashMap;
import java.util.List;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder> {
    Context mContext;
    List<User> userList;
    FirebaseAuth firebaseAuth;
    String myUid;
    public AdapterUser(Context mContext, List<User> userList) {
        this.mContext = mContext;
        this.userList = userList;
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_user,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {
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
        holder.blockIv.setImageResource(R.drawable.ic_unblocked);
        checkIsBlocked(hisUID,holder,position);

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
                       imBlockedORNot(hisUID );
                    }
                });
                builder.create().show();

            }
        });
        // click block
        holder.blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userList.get(position).isBlocked()){
                    unBlockUser(hisUID);
                }else{
                    blockUser(hisUID);
                }
            }
        });
    }
    private void imBlockedORNot(String hisUID){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUID ).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if(ds.exists()){
                                Toast.makeText(mContext, "You're blocked by that user,can't send message", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        Intent intent2 = new Intent(mContext, ChatActivity.class);
                        intent2.putExtra("hisUID", hisUID);
                        mContext.startActivity(intent2);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsBlocked(String hisUID, MyHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if(ds.exists()){
                                holder.blockIv.setImageResource(R.drawable.ic_blocked);
                                userList.get(position).setBlocked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser(String hisUID) {
        //block the user , by addding uid to current user's "BlockedUsers" node

        //put values in hashmap to put in db
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid",hisUID);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(mContext, "Block Successfully...", Toast.LENGTH_SHORT).show();     
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        
    }

    private void unBlockUser(String hisUID) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if(ds.exists()){
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(mContext, "Unblocked Successfully...", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(mContext, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
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
        return userList.size();
    }

    //View holder class
    class MyHolder extends RecyclerView.ViewHolder {
        ImageView avatarIv,blockIv;
        TextView nameTv, emailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //init views
            avatarIv = itemView.findViewById(R.id.imvAvatar);
            nameTv = itemView.findViewById(R.id.tvName);
            emailTv = itemView.findViewById(R.id.tvEmail);
            blockIv = itemView.findViewById(R.id.blockIv);
        }
    }
}
