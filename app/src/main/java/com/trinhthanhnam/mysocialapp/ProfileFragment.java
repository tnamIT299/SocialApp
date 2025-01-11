package com.trinhthanhnam.mysocialapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.trinhthanhnam.mysocialapp.adapter.AdapterPost;
import com.trinhthanhnam.mysocialapp.model.Post;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    //FireBase
    FirebaseAuth auth;
    FirebaseUser user;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    StorageReference storageReference;
    //path where images of user profile and cover will be stored
    String storagePath = "Users_Profile_Cover_Imgs/";

    ImageView avatarIv,coverIv;
    TextView txt_name, txt_email, txt_phone;
    FloatingActionButton addFab;
    Button btnAddFriend;
    RecyclerView recyclerViewPost;

    EditText searchEdt;

    ProgressDialog progressDialog;
    private  static final int CAMERA_REQUEST_CODE = 100;
    private  static final int STORAGE_REQUEST_CODE = 200;
    private  static final int IMAGE_PICK_GALLERY_CODE = 300;
    private  static final int IMAGE_PICK_CAMERA_CODE = 400;

    String cameraPermission[];
    String storagePermission[];

    List<Post> postList;
    AdapterPost adapterPost;
    String uid;

    Uri image_uri;

    //for checking profile or cover photo
    String profileOrCoverPhoto;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        //init FireBase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        //init array of permissions
        cameraPermission = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //Ánh xạ
        avatarIv = view.findViewById(R.id.imvAvatar);
        coverIv = view.findViewById(R.id.coverIv);
        txt_name = view.findViewById(R.id.txtName);
        txt_email = view.findViewById(R.id.txtEmail);
        txt_phone = view.findViewById(R.id.txtPhone);
        addFab = view.findViewById(R.id.add_fab);
        searchEdt = view.findViewById(R.id.searchEdt);
        recyclerViewPost = view.findViewById(R.id.recyclerViewPost);
        btnAddFriend = view.findViewById(R.id.btnAddFriend);
        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getActivity());
        checkUserstatus();
        loadProfileInfo();
        loadMyPosts();

        postList = new ArrayList<>();

        //addFirend
        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAddFriend.setText("Cancel");
                btnAddFriend.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_person_add_disabled_24,0,0,0);
            }
        });


        //fab_button
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showEditDialog();
            }
        });

        searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString())) {
                    searchMyPost(s.toString());
                } else {
                    loadMyPosts();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;

    }

    private void loadProfileInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Query ref = databaseReference.orderByChild("email").equalTo(user.getEmail());
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
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txt_name.setText(name);
                                    txt_email.setText(email);
                                    txt_phone.setText(phone);
                                    if(image != null && !image.isEmpty()) {
                                        Picasso.get().load(image).into(avatarIv);
                                    } else {
                                        //load default image
                                        Picasso.get().load(R.drawable.logo).into(avatarIv);
                                    }

                                    if(cover != null && !cover.isEmpty()) {
                                        Picasso.get().load(cover).into(coverIv);
                                    } else {
                                        //load default image
                                        Picasso.get().load(R.drawable.logo).into(coverIv);
                                    }


                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private void loadMyPosts() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                layoutManager.setStackFromEnd(true);
                layoutManager.setReverseLayout(true);
                getActivity().runOnUiThread(new Runnable() {
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
                            if(getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapterPost = new AdapterPost(getActivity(), postList);
                                        //set adapter to recyclerview
                                        recyclerViewPost.setAdapter(adapterPost);
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Error Load post..", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private void searchMyPost(String search){
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    adapterPost = new AdapterPost(getActivity(), postList);
                    //set adapter to recyclerview
                    recyclerViewPost.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Not found post..", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
       requestPermissions(storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        requestPermissions( cameraPermission, CAMERA_REQUEST_CODE);
    }

    private void showEditDialog() {
        String options [] = {"Edit Profile Picture", "Edit Cover Photo", "Edit Name", "Edit Phone", "Change Password"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set Title
        builder.setTitle("Choose Action");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog items click
                if (which == 0){
                    //Edit Profile Picture
                    progressDialog.setMessage("Uploading Profile Picture...");
                    profileOrCoverPhoto = "image";
                    showImageDialog();
                }else if (which == 1){
                    //Edit Cover Photo
                    progressDialog.setMessage("Uploading Cover Photo...");
                    profileOrCoverPhoto = "cover";
                    showImageDialog();
                }else if (which == 2){
                    //Edit Name
                    progressDialog.setMessage("Uploading Name...");
                    showNamePhoneUpdateDialog("name");
                }else if (which == 3){
                    //Edit Phone
                    progressDialog.setMessage("Uploading Phone...");
                    showNamePhoneUpdateDialog("phone");
                }else if (which == 4 ){
                    //Edit Phone
                    progressDialog.setMessage("Changing password...");
                    showChangePasswordDialog();
                }
            }
        });
        builder.create().show();
    }

    private void showChangePasswordDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_change_pass, null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        EditText oldPassEdt = view.findViewById(R.id.edt_oldPass);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        EditText newPassEdt = view.findViewById(R.id.edt_newPass);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button btnUpdate = view.findViewById(R.id.btn_update);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
       AlertDialog dialog = builder.create();
         dialog.show();
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPass = oldPassEdt.getText().toString().trim();
                String newPass = newPassEdt.getText().toString().trim();
                if(TextUtils.isEmpty(oldPass)){
                    Toast.makeText(getActivity(), "Enter your current password...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(newPass.length() < 6 ){
                    Toast.makeText(getActivity(), "Password length must be at least 6 characters...", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updatePassword(oldPass, newPass);
            }
        });

    }

    private void updatePassword(String oldPass, String newPass) {
        progressDialog.show();
        FirebaseUser user = auth.getCurrentUser();
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);
        user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                user.updatePassword(newPass).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Password Updated...", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        getActivity().finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showNamePhoneUpdateDialog(String key) {
        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + key);
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add edit text
        EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + key);
        linearLayout.addView(editText);
        builder.setView(linearLayout);

        //add buttons in dialog
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = editText.getText().toString().trim();
                if(!TextUtils.isEmpty(value)){
                    progressDialog.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);
                    databaseReference.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    if(key.equals("name")) {

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String child = ds.getKey();
                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        // update name in current users comments on posts
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds : snapshot.getChildren()){
                                    String child = ds.getKey();
                                    if(snapshot.child(child).hasChild("Comments")){
                                        String child1 = ""+snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for(DataSnapshot ds : snapshot.getChildren()){
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

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
                }else{
                    Toast.makeText(getActivity(), "Please enter " + key, Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void showImageDialog() {

        String options [] = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set Title
        builder.setTitle("Pick Image From");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog items click
                if (which == 0){
                    //Camera
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    }else{
                        pickFromCamera();
                    }

                }else if (which == 1){
                    //Gallery
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    }else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //permission allowed
                        pickFromCamera();
                    } else {
                        //permission denied
                        Toast.makeText(getActivity(), "Bật camera và quyền truy cập bộ nhớ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //permission allowed
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(getActivity(), "Bật camera và quyền truy cập bộ nhớ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == getActivity().RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //image is picked from gallery, get uri of image
                image_uri = data.getData();
                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //image is picked from camera, get uri of image
                uploadProfileCoverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri imageUri) {
        //show progress
        progressDialog.show();
        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + user.getUid();
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image uploaded to storage, now get it's url
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                Uri downloadUri = uriTask.getResult();

                //check if image is uploaded or not and url is received
                if(uriTask.isSuccessful()) {
                    HashMap<String, Object> results = new HashMap<>();
                    results.put(profileOrCoverPhoto, downloadUri.toString());
                    databaseReference.child(user.getUid()).updateChildren(results)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Error Updating Image...", Toast.LENGTH_SHORT).show();
                                }
                            });

                    if(profileOrCoverPhoto.equals("image")) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String child = ds.getKey();
                                    snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        // update name in current users comments on posts
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds : snapshot.getChildren()){
                                    String child = ds.getKey();
                                    if(snapshot.child(child).hasChild("Comments")){
                                        String child1 = ""+snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for(DataSnapshot ds : snapshot.getChildren()){
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

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
                }else{
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        //put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to open image
        Intent CamIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        CamIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(CamIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        //intent to pick image from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void checkUserstatus(){
        //get current user
        FirebaseUser user = auth.getCurrentUser();
        if(user!=null){
            //set email of logged in user
            // txt_proFile.setText(user.getEmail());
            uid = user.getUid();
        }else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}