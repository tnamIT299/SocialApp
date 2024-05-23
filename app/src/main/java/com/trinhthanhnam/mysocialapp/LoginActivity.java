package com.trinhthanhnam.mysocialapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    ImageButton btn_ggSign;

    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;
    EditText edt_email, edt_pass;
    TextView txt_nothave_acc, txt_fogotPass;
    Button btn_login;
    private FirebaseAuth auth;
    ProgressDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edt_email=findViewById(R.id.edt_email);
        edt_pass=findViewById(R.id.edt_pass);
        btn_login=findViewById(R.id.btn_login);
        txt_nothave_acc=findViewById(R.id.txt_nothave_account);
        txt_fogotPass=findViewById(R.id.txt_fogotPass);
        btn_ggSign=findViewById(R.id.btn_ggSign);

        gso= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc= GoogleSignIn.getClient(this, gso);

        //before auth
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("36445327318-f1cgjppsg4qmgsg1pmhsiguidvf7imu0.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        auth = FirebaseAuth.getInstance();


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input
                String email = edt_email.getText().toString();
                String pass = edt_pass.getText().toString().trim();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //invalid
                    edt_email.setError("Email không hợp lệ");
                    edt_email.setFocusable(true);
                }else{
                    ///valid email
                    loginUser(email , pass);
                }
            }
        });

//        btn_ggSign.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//                startActivityForResult(signInIntent, RC_SIGN_IN);
//            }
//        });

        btn_ggSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        txt_nothave_acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        txt_fogotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverDialog();
            }
        });

        progressDialog= new ProgressDialog(this);
        progressDialog.setTitle("Logging....");
    }

    private void signIn() {
        Intent signIntent = gsc.getSignInIntent();
        startActivityForResult(signIntent, 1000);
    }

    private void showRecoverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");
        //set layout linear layout
        LinearLayout linearLayout = new LinearLayout(this);
        EditText email = new EditText(this);
        email.setHint("Enter your email to recover ....");
        String emailGet = email.getText().toString().trim();
        email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        email.setMinEms(16);
        linearLayout.addView(email);
        linearLayout.setPadding(10, 10 , 10 , 10);

        builder.setView(linearLayout);
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                beginRecovery(email.getText().toString().trim());
            }
        });

        //btn_cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void beginRecovery(String email) {
        progressDialog.setTitle("Sending email...");
        progressDialog.show();
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Send email to recover successfully", Toast.LENGTH_SHORT).show();
                }else{
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Fail", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String pass) {
        progressDialog.setTitle("Logging....");
        progressDialog.show();
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    FirebaseUser user = auth.getCurrentUser();


                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                }else{
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error, get and show
                Toast.makeText(LoginActivity.this, "" +e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1000){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                task.getResult(ApiException.class);
            } catch (ApiException e) {
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void firebaseAuthwithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = auth.getCurrentUser();

                    //get user email and uid from auth
                    String email = user.getEmail();
                    String uid = user.getUid();

                    //using Háhmap
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


                    //Firebase data instance
                    FirebaseDatabase database = FirebaseDatabase.getInstance("https://my-socialapp-6aaa9-default-rtdb.firebaseio.com/");
                    //path to store data named "Users"
                    DatabaseReference reference = database.getReference("Users");

                    //put data within hashmap in database
                    reference.child(uid).setValue(hashMap);
                    Toast.makeText(LoginActivity.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                }else{
                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}