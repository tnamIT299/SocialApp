package com.trinhthanhnam.mysocialapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.trinhthanhnam.mysocialapp.adapter.AdapterComment;
import com.trinhthanhnam.mysocialapp.model.Comment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {
    String myUid, myEmail, myName, myDp,postId,pLikes,hisDp,hisName,hisUid,pImage;
    ImageView postImageIv,postUImg,AvatarImg;
    TextView postUName,postTime,postTitleTv,postDescriptionTv,postLikeTv,postCommentTv;
    ImageButton btnMore,btnSend;
    Button btnLike,btnShare;
    EditText edtComment;
    boolean mProcessComment = false;
    boolean mProcessLike = false;
    ProgressDialog pd;
    RecyclerView commentRecyclerView;
    List<Comment> commentList;
    AdapterComment adapterComment;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        postUImg = findViewById(R.id.postUImg);
        postImageIv = findViewById(R.id.postImageIv);
        AvatarImg = findViewById(R.id.AvatarImg);
        postUName = findViewById(R.id.postUName);
        postTime = findViewById(R.id.postTime);
        postTitleTv = findViewById(R.id.postTitleTv);
        postDescriptionTv = findViewById(R.id.postDescriptionTv);
        postLikeTv = findViewById(R.id.postLikeTv);
        postCommentTv = findViewById(R.id.postCommentTv);
        btnMore = findViewById(R.id.btnMore);
        btnSend = findViewById(R.id.btnSend);
        btnLike = findViewById(R.id.btnLike);
        btnShare = findViewById(R.id.btnShare);
        commentRecyclerView = findViewById(R.id.commentRecyclerView);
        edtComment = findViewById(R.id.edtComment);

        loadPostInfo();
        checkUserStatus();
        loadUserInfo();
        setLikes();
        loadComments();


        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
                addToHisNotifications(""+hisUid,""+postId,"Liked your post");
            }
        });
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pTitle = postTitleTv.getText().toString().trim();
                String pDescr = postDescriptionTv.getText().toString().trim();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) postImageIv.getDrawable();
                if (bitmapDrawable == null) {
                    //post without image
                    shareTextOnly(pTitle, pDescr);
                } else {
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageandText(pTitle, pDescr, bitmap);
                }
            }
        });

    }


    private void shareImageandText(String pTitle, String pDescr, Bitmap bitmap) {
        String shareBody = pTitle + "\n" + pDescr;
        //first we will save this image in cache, get the saved image uri
        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        intent.setType("image/png");
        startActivity(Intent.createChooser(intent, "Share Via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream fileInputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileInputStream);
            fileInputStream.flush();
            fileInputStream.close();
            uri = FileProvider.getUriForFile(this, "com.trinhthanhnam.mysocialapp.fileprovider", file);

        }catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void shareTextOnly(String pTitle, String pDescr) {
        String shareBody = pTitle + "\n" + pDescr;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(intent, "Share Via"));
    }

    private void addToHisNotifications(String hisUid, String pId, String notification){
        String timestamp = ""+System.currentTimeMillis();
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        commentRecyclerView.setLayoutManager(layoutManager);
        commentList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    Comment comment = ds.getValue(Comment.class);
                    commentList.add(comment);
                    //setup adapter
                    adapterComment = new AdapterComment(getApplicationContext(),commentList,myUid,postId);
                    //set adapter
                    commentRecyclerView.setAdapter(adapterComment);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, btnMore, Gravity.END);
        if(hisUid.equals(myUid)){
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }//item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == 0){
                    beginDelete();
                } else if(id == 1){
                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete() {
        if(pImage.equals("noImage")){
            deleteWithoutImage();
        } else {
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                fquery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(PostDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");
        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(PostDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes() {
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postId).hasChild(myUid)){
                    btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                    btnLike.setText("Liked");
                }else{
                    btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like,0,0,0);
                    btnLike.setText("Liked");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void likePost() {
        // Lưu vị trí cuộn hiện tại
        int scrollPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

        mProcessLike = true;
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessLike){
                    if(snapshot.child(postId).hasChild(myUid)){
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;
                    }else {
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likesRef.child(postId).child(myUid).setValue("Liked");
                        mProcessLike = false;

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Đặt lại vị trí cuộn
        recyclerView.scrollToPosition(scrollPosition);
    }

    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("Adding comment...");
        //get data from comment edit text
        String comment = edtComment.getText().toString().trim();
        //validate
        if(TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Comment is empty...", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("cid",timeStamp);
        hashMap.put("comment",comment);
        hashMap.put("timeStamp",timeStamp);
        hashMap.put("uid",myUid);
        hashMap.put("uEmail",myEmail);
        hashMap.put("uDp",myDp);
        hashMap.put("uName",myName);
        // put this data in db
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, "Comment Added...", Toast.LENGTH_SHORT).show();
                        addToHisNotifications(""+ hisUid,""+ postId,"Commented on your post");
                        edtComment.setText("");
                        updateCommentCount();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void updateCommentCount() {
        mProcessComment = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessComment){
                    String comments= ""+snapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments)+1;
                    ref.child("pComments").setValue(""+newCommentVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUserInfo() {
        // get user info
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    myName = ""+ds.child("name").getValue();
                    myDp = ""+ds.child("image").getValue();
                    //set data
                    try{
                        Picasso.get().load(myDp).placeholder(R.drawable.baseline_account_circle_24).into(AvatarImg);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.baseline_account_circle_24).into(AvatarImg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    String pTitle = ""+ds.child("pTitle").getValue();
                    String pDescr = ""+ds.child("pDescr").getValue();
                    pLikes = ""+ds.child("pLikes").getValue();
                    String pTimeStamp = ""+ds.child("pTime").getValue();
                    pImage = ""+ds.child("pImage").getValue();
                    hisDp = ""+ds.child("uDp").getValue();
                    String hisUid = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    hisName = ""+ds.child("uName").getValue();
                    String commentCount = ""+ds.child("pComments").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    //send cmt
                    btnSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            postComment();
                            addToHisNotifications(""+hisUid,""+postId,"comment on your post");;
                        }
                    });

                    //set data
                    postTitleTv.setText(pTitle);
                    postDescriptionTv.setText(pDescr);
                    postLikeTv.setText(pLikes + " Likes");
                    postTime.setText(pTime);
                    postUName.setText(hisName);
                    postCommentTv.setText(commentCount+" Comments");

                    if(pImage.equals("noImage")){
                        postImageIv.setVisibility(View.GONE);
                    } else {
                        postImageIv.setVisibility(View.VISIBLE);
                        try{
                            Glide.with(PostDetailActivity.this)
                                    .load(pImage)
                                    .into(postImageIv);

                        }catch (Exception e){

                        }
                    }
                    // set user image in comment part
                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.baseline_account_circle_24).into(postUImg);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.baseline_account_circle_24).into(postUImg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!= null){
            myEmail = user.getEmail();
            myUid = user.getUid();
        }else{
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}