package com.trinhthanhnam.mysocialapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.trinhthanhnam.mysocialapp.adapter.AdapterGroupSearchResults;
import com.trinhthanhnam.mysocialapp.adapter.AdapterParticipantAdd;
import com.trinhthanhnam.mysocialapp.adapter.AdapterSearchResults;
import com.trinhthanhnam.mysocialapp.model.Chat;
import com.trinhthanhnam.mysocialapp.model.Group_Chat;
import com.trinhthanhnam.mysocialapp.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {
    private String groupId;
    private String myGroupRole = "";
    private FirebaseAuth firebaseAuth;
    private ArrayList<User> userList;
    private AdapterParticipantAdd adapterParticipantAdd;
    private ActionBar actionBar;
    private ImageView groupIconIv;
    private TextView descriptionTv,createByTv,editGroupTv,addParticipantTv,leaveGroupTv,participantsTv,searchMessageTv;
    private RecyclerView participantsRv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Group Info");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        } else {
            Log.e("GroupInfoActivity", "ActionBar is null in onCreate");
        }
        groupIconIv = findViewById(R.id.groupIconIv);
        descriptionTv = findViewById(R.id.descriptionTv);
        createByTv = findViewById(R.id.createByTv);
        editGroupTv = findViewById(R.id.editGroupTv);
        addParticipantTv = findViewById(R.id.addParticipantTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
        participantsTv = findViewById(R.id.participantsTv);
        participantsRv = findViewById(R.id.participantsRv);
        searchMessageTv = findViewById(R.id.searchMessageTv);

        groupId = getIntent().getStringExtra("groupId");
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyGroupRole();
        loadGroupInfo();
        addParticipantTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this,GroupParticipantAddActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });
        leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dialogTitle="";
                String dialogDescription="";
                String positiveButtonTitle="";
                if(myGroupRole.equals("creator")){
                    dialogTitle="Delete Group";
                    dialogDescription="Are you sure you want to Delete group permanently?";
                    positiveButtonTitle = "DELETE";
                }
                else{
                    dialogTitle="Leave Group";
                    dialogDescription="Are you sure you want to Leave group permanently?";
                    positiveButtonTitle = "LEAVE";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(myGroupRole.equals("creator")){
                                    deleteGroup();
                                }
                                else{
                                    leaveGroup();
                                }
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this,GroupEditActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });
        searchMessageTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(GroupInfoActivity.this);
                builder.setTitle("Search Messages");

                // Thiết lập EditText để nhập từ khóa
                final EditText input = new EditText(GroupInfoActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);

                // Thiết lập các thông số cho EditText (nếu cần)
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                input.setLayoutParams(lp);
                builder.setView(input);

                // Thiết lập nút "Tìm kiếm"
                builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String keyword = input.getText().toString().trim();
                        if (!keyword.isEmpty()) {
                            searchMessagesInFirebase(keyword);
                        } else {
                            Toast.makeText(GroupInfoActivity.this, "Please enter a keyword to search", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // Thiết lập nút "Hủy"
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    private void searchMessagesInFirebase(String keyword) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        String keywordLower = keyword.toLowerCase();
        ref.child(groupId).child("Messages")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Group_Chat> searchResults = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Lấy tin nhắn từ dataSnapshot
                            Group_Chat chat = snapshot.getValue(Group_Chat.class);
                            // Thêm tin nhắn vào danh sách kết quả nếu tin nhắn không null
                            if (chat != null && chat.getType().equals("text") && !chat.getMessage().equals("This message was deleted...")) {
                                // Chuyển đổi nội dung tin nhắn sang chữ thường để so sánh không phân biệt chữ hoa chữ thường
                                String messageLower = chat.getMessage().toLowerCase();
                                if (messageLower.contains(keywordLower)) {
                                    searchResults.add(chat);
                                }
                            }
                        }
                        if (searchResults.isEmpty()) {
                            Toast.makeText(GroupInfoActivity.this, "No messages found", Toast.LENGTH_SHORT).show();
                        } else {
                            // Hiển thị kết quả tìm kiếm
                            showSearchResults(searchResults);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(GroupInfoActivity.this, "Search failed: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showSearchResults(List<Group_Chat> searchResults) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Search Results");

        // Tạo một layout XML tùy chỉnh cho nội dung của Dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_search_results_list, null);
        builder.setView(dialogView);

        // Ánh xạ RecyclerView trong layout của Dialog
        RecyclerView recyclerView = dialogView.findViewById(R.id.searchResultsRecyclerView);

        // Thiết lập LayoutManager cho RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Tạo Adapter và thiết lập cho RecyclerView
        AdapterGroupSearchResults adapter = new AdapterGroupSearchResults(searchResults, this);
        recyclerView.setAdapter(adapter);
        // Hiển thị Dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Thiết lập sự kiện click cho nút "OK"
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Đóng Dialog khi người dùng nhấn "OK"
            }
        });
    }

    private void leaveGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(firebaseAuth.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(GroupInfoActivity.this, "Group left successfully...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this,DashboardActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();     
                    }
                });
    }

    private void deleteGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(GroupInfoActivity.this, "Group successfully deleted ...", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(GroupInfoActivity.this,DashboardActivity.class);
                        intent.putExtra("showGroupFragment", true);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid")
                .equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        actionBar.setSubtitle("Group Info");
                        for(DataSnapshot ds : snapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                            actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail() + "(" + myGroupRole + ")");
                            if (myGroupRole.equals("participant")){
                                editGroupTv.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.GONE);
                                leaveGroupTv.setText("Leave Group");
                            }
                            else if (myGroupRole.equals("admin")){
                                editGroupTv.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Leave Group");
                            }
                            else if(myGroupRole.equals("creator")){
                                editGroupTv.setVisibility(View.VISIBLE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Delete Group");
                                Drawable img = ContextCompat.getDrawable(getApplicationContext(), R.drawable.baseline_delete_24);
                                leaveGroupTv.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                            }
                        }
                        loadParticipants();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadParticipants() {
        userList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");

        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    String uid = ""+ds.child("uid").getValue();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ds : snapshot.getChildren()){
                                User user = ds.getValue(User.class);
                                userList.add(user);
                            }
                            adapterParticipantAdd = new AdapterParticipantAdd(GroupInfoActivity.this,userList,""+groupId,""+myGroupRole);
                            participantsTv.setText("Participants (" + userList.size() +")");
                            participantsRv.setAdapter(adapterParticipantAdd);
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

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String groupId = ""+ds.child("groupId").getValue();
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String groupDescription = ""+ds.child("groupDescription").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String createdBy = ""+ds.child("createBy").getValue();
                    String timeStamp = ""+ds.child("timestamp").getValue();

//                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
//                    cal.setTimeInMillis(Long.parseLong(timeStamp));
//                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

                    long senderTime = Long.parseLong(timeStamp);
                    String dateTime = (String) DateUtils.getRelativeTimeSpanString(senderTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
                    loadCreator(dateTime,createdBy);

                    actionBar.setTitle(groupTitle);
                    descriptionTv.setText(groupDescription);
                    try{
                        Picasso.get().load(groupIcon).placeholder(R.drawable.baseline_groups_24).into(groupIconIv);
                    }catch (Exception e){
                        groupIconIv.setImageResource(R.drawable.baseline_groups_24);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCreator(String dateTime, String createdBy) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    createByTv.setText("Created by "+name + " on " +dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}