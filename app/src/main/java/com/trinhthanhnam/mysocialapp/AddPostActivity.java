package com.trinhthanhnam.mysocialapp;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    //permisson array
    String [] cameraPermission;
    String [] storagePermission;
    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    EditText titleEt, descriptionEt;
    ImageView imageIv;
    Button uploadBtn;
    //User Infor
    String name, email, uid, dp;
    Uri uriImage = null;
    //progress bar
    ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        titleEt = findViewById(R.id.edtTitle);
        descriptionEt = findViewById(R.id.edtDescription);
        imageIv = findViewById(R.id.imageIv);
        uploadBtn = findViewById(R.id.btnUpload);

        //init permisson array
        cameraPermission = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressBar= new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserstatus();
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    name = ""+ds.child("name").getValue();
                    email = ""+ds.child("email").getValue();
                    dp = ""+ds.child("image").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //get image from gallery/camera on click
        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();
                if(TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this, "Enter title...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this, "Enter description...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (uriImage == null) {
                    uploadData(title, description, "noImage");
                }else {
                    uploadData(title, description, String.valueOf(uriImage));
                }
            }
        });


    }

    private void uploadData(String title, String description, String uri) {
        progressBar.setMessage("Publishing post...");
        progressBar.show();
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timeStamp;
        if(!uri.equals("noImage")) {
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putFile(Uri.parse(uri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    String downloadUri = uriTask.getResult().toString();
                    if(uriTask.isSuccessful()){
                        //create post
                        HashMap<Object, String> hashMap = new HashMap<>();
                        hashMap.put("uid", uid);
                        hashMap.put("uName", name);
                        hashMap.put("uEmail", email);
                        hashMap.put("uDp", dp);
                        hashMap.put("pId", timeStamp);
                        hashMap.put("pTitle", title);
                        hashMap.put("pDescr", description);
                        hashMap.put("pImage", downloadUri);
                        hashMap.put("pTime", timeStamp);

                        //path to store post data
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        //put data in this ref
                        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressBar.dismiss();
                                Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                //reset views
                                titleEt.setText("");
                                descriptionEt.setText("");
                                imageIv.setImageURI(null);
                                uriImage = null;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.dismiss();
                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            //post without image
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);
            hashMap.put("uDp", dp);
            hashMap.put("pId", timeStamp);
            hashMap.put("pTitle", title);
            hashMap.put("pDescr", description);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timeStamp);

            //path to store post data
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            //put data in this ref
            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressBar.dismiss();
                    Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                    //reset views
                    titleEt.setText("");
                    descriptionEt.setText("");
                    imageIv.setImageURI(null);
                    uriImage = null;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void showImagePickDialog() {
        String []options= {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");
        //set options to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //item click handle
                if (which == 0) {
                    //camera clicked
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                }

                else if (which == 1) {
                    //gallery clicked
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        //create and show dialog
        builder.create().show();

    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        uriImage = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImage);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }


    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserstatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserstatus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


    private void checkUserstatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){
            //set email of logged in user
            // txt_proFile.setText(user.getEmail());
            email = user.getEmail();
            uid = user.getUid();
        }else{
            startActivity(new Intent(this, MainActivity.class));
           finish();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }else {
                        //permission denied
                        Toast.makeText(this, "Camera & Storage both permissions are necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length > 0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        pickFromGallery();
                    }else {
                        //permission denied
                        Toast.makeText(this, "Storage permissions necessary...", Toast.LENGTH_SHORT).show();
                    }
                }else {

                }
            }
            break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                if (data != null) {
                    uriImage = data.getData();
                    imageIv.setImageURI(uriImage);
                } else {
                    Toast.makeText(this, "Không thể lấy ảnh từ thư viện", Toast.LENGTH_SHORT).show();
                }
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                imageIv.setImageURI(uriImage);
            }
        } else {
            Toast.makeText(this, "Hủy chọn ảnh", Toast.LENGTH_SHORT).show();
        }
    }
}