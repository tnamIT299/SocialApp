package com.trinhthanhnam.mysocialapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.trinhthanhnam.mysocialapp.adapter.AdapterPost;
import com.trinhthanhnam.mysocialapp.model.Post;

import java.util.ArrayList;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerViewPost;

    ImageView avatarIv,coverIv;
    TextView txt_name, txt_email, txt_phone;
    EditText searchEdt;
    List<Post> postList;
    AdapterPost adapterPost;
    String uid;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        recyclerViewPost = findViewById(R.id.recyclerViewPost);
        searchEdt = findViewById(R.id.searchEdt);
        firebaseAuth = FirebaseAuth.getInstance();
        avatarIv = findViewById(R.id.imvAvatar);
        coverIv = findViewById(R.id.coverIv);
        txt_name = findViewById(R.id.txtName);
        txt_email = findViewById(R.id.txtEmail);
        txt_phone = findViewById(R.id.txtPhone);

        //get Uid
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

//        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    //get data
//                    String name =  "" + ds.child("name").getValue();
//                    String email = "" + ds.child("email").getValue();
//                    String phone = "" + ds.child("phone").getValue();
//                    String image = "" + ds.child("image").getValue();
//                    String cover = "" + ds.child("cover").getValue();
//
//                    //set data
//                    txt_name.setText(name);
//                    txt_email.setText(email);
//                    txt_phone.setText(phone);
//                    try {
//                        Picasso.get().load(image).into(avatarIv);
//                    }catch (Exception e) {
//                        //load default image
//                        Picasso.get().load(R.drawable.logo).into(avatarIv);
//                    }
//
//
//                    try {
//                        Picasso.get().load(cover).into(coverIv);
//                    }catch (Exception e) {
//                        //load default image
//
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        postList = new ArrayList<>();


        checkUserstatus();
        loadProfileInfo();
        loadHisPost();

        searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString())) {
                    searchHisPost(s.toString());
                } else {
                    loadHisPost();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    private void loadProfileInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Query ref = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get data
                            String name =  "" + ds.child("name").getValue();
                            String email = "" + ds.child("email").getValue();
                            String phone = "" + ds.child("phone").getValue();
                            String image = "" + ds.child("image").getValue();
                            String cover = "" + ds.child("cover").getValue();

                            //set data
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txt_name.setText(name);
                                    txt_email.setText(email);
                                    txt_phone.setText(phone);

                                    try {
                                        Glide.with(ThereProfileActivity.this)
                                                .load(image)
                                                .into(avatarIv);
                                    } catch (Exception e) {
                                        //load default image
                                        Glide.with(ThereProfileActivity.this)
                                                .load(R.drawable.logo)
                                                .into(avatarIv);
                                    }

                                    try {
                                        Glide.with(ThereProfileActivity.this)
                                                .load(cover)
                                                .into(coverIv);
                                    } catch (Exception e) {
                                        //load default image
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ThereProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private void loadHisPost() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LinearLayoutManager layoutManager = new LinearLayoutManager(ThereProfileActivity.this);
                layoutManager.setStackFromEnd(true);
                layoutManager.setReverseLayout(true);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerViewPost.setLayoutManager(layoutManager);
                    }
                });

                //init post list
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                //query to load posts
                Query query = ref.orderByChild("uid").equalTo(uid);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            Post myPost = ds.getValue(Post.class);
                            postList.add(myPost);

                            //adapter
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapterPost = new AdapterPost(ThereProfileActivity.this, postList);
                                    //set adapter to recyclerview
                                    recyclerViewPost.setAdapter(adapterPost);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ThereProfileActivity.this, "Error Load post..", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private void searchHisPost(String search) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(ThereProfileActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerViewPost.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query to load posts
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Post myPost = ds.getValue(Post.class);
                    if(myPost.getpTitle().toLowerCase().contains(search.toLowerCase()) ||
                            myPost.getpDescr().toLowerCase().contains(search.toLowerCase())) {
                        postList.add(myPost);
                    }

                    //adapter
                    adapterPost = new AdapterPost(ThereProfileActivity.this, postList);
                    //set adapter to recyclerview
                    recyclerViewPost.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, "Not found post..", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    private void checkUserstatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){
            //set email of logged in user
            // txt_proFile.setText(user.getEmail());
        }else{
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

}