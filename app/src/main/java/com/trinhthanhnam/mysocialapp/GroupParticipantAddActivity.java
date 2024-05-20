package com.trinhthanhnam.mysocialapp;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trinhthanhnam.mysocialapp.adapter.AdapterParticipantAdd;
import com.trinhthanhnam.mysocialapp.model.User;
import androidx.appcompat.widget.Toolbar;
import com.trinhthanhnam.mysocialapp.notifications.Data;

import java.util.ArrayList;

public class GroupParticipantAddActivity extends AppCompatActivity {
    private RecyclerView usersRv;
    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;
    private String groupId,myGroupRole;
    private ArrayList<User> userList;
    private AdapterParticipantAdd adapterParticipantAdd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participant_add);
        Toolbar toolbar = findViewById(R.id.toolBar);
        if (toolbar == null) {
            Log.e("GroupParticipantAddActivity", "Toolbar is null");
        } else {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Add Participants");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            } else {
                Log.e("GroupParticipantAddActivity", "ActionBar is null");
            }
        }
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Add Participants");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        usersRv = findViewById(R.id.usersRv);
        groupId = getIntent().getStringExtra("groupId");
        loadGroupInfo();

    }

    private void getAllUsers() {
        userList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    if(!firebaseAuth.getUid().equals(user.getUid())){
                        userList.add(user);
                    }
                }
                adapterParticipantAdd = new AdapterParticipantAdd(GroupParticipantAddActivity.this,userList,""+groupId,""+myGroupRole);
                usersRv.setAdapter(adapterParticipantAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        final DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String groupId = ""+ds.child("groupId").getValue();
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String groupDescription = ""+ds.child("groupDescription").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();
                    actionBar.setTitle("Add Participants");
                    ref1.child(groupId).child("Participants").child(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        myGroupRole = ""+snapshot.child("role").getValue();
                                        actionBar.setTitle(groupTitle + "("+myGroupRole+")");
                                        getAllUsers();
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}