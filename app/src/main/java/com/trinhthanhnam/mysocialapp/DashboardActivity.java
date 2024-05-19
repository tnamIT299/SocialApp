package com.trinhthanhnam.mysocialapp;

import static com.google.android.gms.auth.zzl.getToken;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.trinhthanhnam.mysocialapp.notifications.Token;

public class DashboardActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    TextView txt_proFile;
    String mUID;
    private AlertDialog alertDialog;
    BottomNavigationView navigationView;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //init
       // txt_proFile=findViewById(R.id.txt_profile);
        firebaseAuth = FirebaseAuth.getInstance();

         navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.container, homeFragment,"");
        ft1.commit();

        checkUserstatus();

        //update token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                return;
            }
            // Get new FCM registration token
            String token = task.getResult();
            // Pass the token to updateToken function
            updateToken(token);
        });

    }

    @Override
    protected void onResume() {
        checkUserstatus();
        super.onResume();
    }

    private void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);

//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
//        // Lưu token với key là 'fcmToken'
//        ref.child("fcmToken").setValue(token);

//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (firebaseUser != null) {
//            String userId = firebaseUser.getUid();
//            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Tokens").child(userId);
//            databaseRef.child("fcmToken").setValue(token)
//                    .addOnSuccessListener(aVoid -> Log.d("FCM", "Token successfully updated."))
//                    .addOnFailureListener(e -> Log.e("FCM", "Failed to update token", e));
//        } else {
//            Log.d("FCM", "No user signed in, cannot update token.");
//        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener=
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            //handle item click
           if( menuItem.getItemId() == R.id.nav_home){
               HomeFragment homeFragment = new HomeFragment();
               FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
               ft1.replace(R.id.container, homeFragment,"");
               ft1.commit();
               return true;
           } else if (menuItem.getItemId() == R.id.nav_profie) {
               ProfileFragment profileFragment = new ProfileFragment();
               FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
               ft2.replace(R.id.container, profileFragment,"");
               ft2.commit();
               return true;
           } else if (menuItem.getItemId() == R.id.nav_chat) {
               ChatFragment chatFragment = new ChatFragment();
               FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
               ft5.replace(R.id.container, chatFragment,"");
               ft5.commit();
               return true;
           }
           else if (menuItem.getItemId() == R.id.nav_user){
               UsersFragment usersFragment = new UsersFragment();
               FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
               ft3.replace(R.id.container, usersFragment,"");
               ft3.commit();
               return true;
           }
           else if (menuItem.getItemId() == R.id.nav_mutilChoice){
               showMoreOptions();
           }
            return false;
        }
    };

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, navigationView , Gravity.END);
        popupMenu.getMenu().add(Menu.NONE,0,0,"Notifications").setIcon(R.drawable.baseline_notifications_24);
        popupMenu.getMenu().add(Menu.NONE,1,0,"Group Chats").setIcon(R.drawable.baseline_groups_24);
        popupMenu.getMenu().add(Menu.NONE,2,0,"Settings").setIcon(R.drawable.baseline_manage_accounts_24);

        //click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == 0) {
                    //Notifications
                    NotificationFragment notiFragment = new NotificationFragment();
                   FragmentTransaction ft6 = getSupportFragmentManager().beginTransaction();
                   ft6.replace(R.id.container, notiFragment,"");
                   ft6.commit();
                } else if(id == 1){
                    //Group Chats
                    GroupChatFragment groupChatFragment = new GroupChatFragment();
                    FragmentTransaction ft7 = getSupportFragmentManager().beginTransaction();
                    ft7.replace(R.id.container, groupChatFragment,"");
                    ft7.commit();
                }
                else if(id == 2){
                    //Settings
                    OptionFragment optionFragment = new OptionFragment();
                    FragmentTransaction ft8 = getSupportFragmentManager().beginTransaction();
                    ft8.replace(R.id.container, optionFragment,"");
                    ft8.commit();
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void checkUserstatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){
            //set email of logged in user
           // txt_proFile.setText(user.getEmail());
            mUID = user.getUid();
            SharedPreferences sp = getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID",mUID);
            editor.apply();
        }else{
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        alertDialog =  new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Exist App")
                .setMessage("Are you sure you want to close this app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onStart() {
        checkUserstatus();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return super.onCreateOptionsMenu(menu);
    }
    //handle menu

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if(id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserstatus();
        }
        return super.onOptionsItemSelected(item);
    }


}