package com.trinhthanhnam.mysocialapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.trinhthanhnam.mysocialapp.R;
import com.trinhthanhnam.mysocialapp.model.User;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterParticipantAdd extends RecyclerView.Adapter<AdapterParticipantAdd.HolderParticipantAdd> {

    private Context context;
    private ArrayList<User> userList;
    private String groupId,myGroupRole;

    public AdapterParticipantAdd(Context context, ArrayList<User> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_participant_add,parent,false);
        return new HolderParticipantAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantAdd holder, int position) {
        User user = userList.get(position);
        String name = user.getName();
        String email = user.getEmail();
        String image = user.getImage();
        String uid = user.getUid();

        holder.nameTv.setText(name);
        holder.emailTv.setText(email);
        holder.statusTv.setText("");
        try {
            Picasso.get().load(image).placeholder(R.drawable.baseline_account_circle_24).into(holder.imvAvatar);
        } catch (Exception e) {
            holder.imvAvatar.setImageResource(R.drawable.baseline_account_circle_24);
        }
        checkIfAlreadyExists(user, holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Thêm log để kiểm tra
                Log.d("AdapterParticipantAdd", "User clicked: " + name + " (UID: " + uid + ")");

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("Participants").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String hisPreviousRole = "" + snapshot.child("role").getValue();
                                    Log.d("AdapterParticipantAdd", "User role: " + hisPreviousRole);
                                    String[] options;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Choose Option");
                                    if (myGroupRole.equals("creator")) {
                                        if (hisPreviousRole.equals("admin")) {
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which == 0) {
                                                        removeAdmin(user);
                                                    } else {
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();
                                        } else if (hisPreviousRole.equals("participant")) {
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which == 0) {
                                                        makeAdmin(user);
                                                    } else {
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();
                                        }
                                    } else if (myGroupRole.equals("admin")) {
                                        if (hisPreviousRole.equals("creator")) {
                                            Toast.makeText(context, "Creator of Group...", Toast.LENGTH_SHORT).show();
                                        } else if (hisPreviousRole.equals("admin")) {
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which == 0) {
                                                        removeAdmin(user);
                                                    } else {
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();
                                        } else if (hisPreviousRole.equals("participant")) {
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which == 0) {
                                                        makeAdmin(user);
                                                    } else {
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Add Participant")
                                            .setMessage("Add this user in this group?")
                                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    addParticipant(user);
                                                }
                                            })
                                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("AdapterParticipantAdd", "Error: " + error.getMessage());
                            }
                        });
            }
        });
    }


    private void addParticipant(User user) {
        String timeStamp = ""+System.currentTimeMillis();
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("uid",user.getUid());
        hashMap.put("role","participant");
        hashMap.put("timeStamp",""+timeStamp);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(user.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Added successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void makeAdmin(User user) {
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("role","admin");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(user.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "The user is now admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        
    }

    private void removeParticipant(User user) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(user.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeAdmin(User user) {
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("role","participant");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(user.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "The user is no longer admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfAlreadyExists(User user, HolderParticipantAdd holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String hisRole = ""+snapshot.child("role").getValue();
                            holder.statusTv.setText(hisRole);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        holder.statusTv.setText("");
                    }
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class HolderParticipantAdd extends RecyclerView.ViewHolder{
        private ImageView imvAvatar;
        private TextView nameTv,emailTv,statusTv;
        public HolderParticipantAdd(@NonNull View itemView) {
            super(itemView);
            imvAvatar = itemView.findViewById(R.id.imvAvatar);
            nameTv = itemView.findViewById(R.id.nameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            statusTv = itemView.findViewById(R.id.statusTv);

        }
    }
}
