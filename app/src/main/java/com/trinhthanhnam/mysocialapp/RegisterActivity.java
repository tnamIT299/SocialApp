package com.trinhthanhnam.mysocialapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.trinhthanhnam.mysocialapp.calling.TokenGenerator;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    TokenGenerator tokenGenerator = new TokenGenerator();
    EditText edt_email, edt_pass, edt_cfpass;
    TextView txt_haveAcc;
    Button btn_register;

    ProgressDialog progressDialog;
    private FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        edt_email=findViewById(R.id.edt_email);
        edt_pass=findViewById(R.id.edt_pass);
        edt_cfpass=findViewById(R.id.edt_cfpass);
        btn_register=findViewById(R.id.btn_register);
        txt_haveAcc=findViewById(R.id.txt_have_account);
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đang đăng ký.....");


        txt_haveAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });


        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = edt_email.getText().toString().trim();
                String pass = edt_pass.getText().toString().trim();
                String cfpass = edt_cfpass.getText().toString().trim();
                //validate
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    edt_email.setError("Email không hợp lệ");
                    edt_email.setFocusable(true);
                }else if(pass.length()<6){
                    edt_pass.setError("Mật khẩu tối thiểu từ 6 ký tự trở lên");
                    edt_pass.setFocusable(true);
                }else if (!(cfpass.equals(pass))){
                    System.out.println(cfpass);
                    System.out.println(pass);
                    edt_cfpass.setError("Mật khẩu không khớp");

                }else{
                    registerUser(email , pass);
                }
            }
        });

    }

    private void registerUser(String email, String pass) {
        progressDialog.show(); // Ensure progressDialog is not null and properly initialized

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Dismiss progressDialog
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();

                            FirebaseUser user = auth.getCurrentUser();
                            //get user email and uid from auth
                            String email = user.getEmail();
                            String uid = user.getUid();

                            //using Hashmap
                            HashMap<Object, String> hashMap = new HashMap<>();

                            //put into in hashmap
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", "");
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("typingTo", "noOne");
                            hashMap.put("phone", "");
                            hashMap.put("image", "");
                            hashMap.put("cover", "");
                            hashMap.put("accessTokenCall", tokenGenerator.genAccessToken(TokenGenerator.SID_KEY, TokenGenerator.SECRET_KEY, 3600, uid));

                            //Firebase data instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //path to store data named "Users"
                            DatabaseReference reference = database.getReference("Users");

                            //put data within hashmap in database
                            reference.child(uid).setValue(hashMap);
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        } else {
                            Toast.makeText(RegisterActivity.this, "Xác thực thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss(); // Dismiss progressDialog
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}