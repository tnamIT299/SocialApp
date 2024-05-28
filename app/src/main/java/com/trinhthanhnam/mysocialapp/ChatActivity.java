package com.trinhthanhnam.mysocialapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StringeeConnectionListener;
import com.trinhthanhnam.mysocialapp.adapter.AdapterChat;
import com.trinhthanhnam.mysocialapp.adapter.AdapterSearchResults;
import com.trinhthanhnam.mysocialapp.adapter.AdapterUser;
import com.trinhthanhnam.mysocialapp.calling.TokenGenerator;
import com.trinhthanhnam.mysocialapp.model.Chat;
import com.trinhthanhnam.mysocialapp.model.User;
import com.trinhthanhnam.mysocialapp.notifications.APIService;
import com.trinhthanhnam.mysocialapp.notifications.Client;
import com.trinhthanhnam.mysocialapp.notifications.Data;
import com.trinhthanhnam.mysocialapp.notifications.Response;
import com.trinhthanhnam.mysocialapp.notifications.Sender;
import com.trinhthanhnam.mysocialapp.notifications.Token;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {
    TokenGenerator tokenGenerator = new TokenGenerator();
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageButton btn_send, attachBtn, moreIv, duoIv, callIv;
    ImageView profileIv,blockIv;
    TextView nameTv , userStatusTv;
    EditText messageEt;

    LinearLayout typingLayout;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;
    List<Chat> chatList;
    AdapterChat adapterChat;

    private Handler handler ;
    private Runnable runnableCode;


    String hisuid;
    String myUid;
    String hisImage;
    boolean isBlocked = false;
    Uri uriImage = null;

    APIService apiService;
    boolean notify = false;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    //permisson array
    String [] cameraPermission;
    String [] storagePermission;

    public static StringeeClient client;
    static Map<String, StringeeCall> callMap = new HashMap<>();
    static Map<String, StringeeCall2> call2Map = new HashMap<>();



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //init
        toolbar = findViewById(R.id.toolBar);
        toolbar.setTitle("");
        profileIv = findViewById(R.id.profileIv);
        recyclerView= findViewById(R.id.chatRcv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        btn_send = findViewById(R.id.sendBtn);
        attachBtn = findViewById(R.id.attachBtn);
        blockIv = findViewById(R.id.blockIv);
        moreIv = findViewById(R.id.moreIv);
        duoIv = findViewById(R.id.duoIv);
        callIv = findViewById(R.id.callIv);
        typingLayout = findViewById(R.id.typingLayout);

        //init permisson array
        cameraPermission = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //layout (linear layout) for recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //recycler view properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //create api service
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        Intent intent = getIntent();
        hisuid = intent.getStringExtra("hisUID");
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("Users");

        //refesh token after 1hour
        handler = new Handler();
        runnableCode = new Runnable() {
            @Override
            public void run() {
                String newToken = tokenGenerator.genAccessToken(TokenGenerator.SID_KEY, TokenGenerator.SECRET_KEY, 3600, myUid);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child(myUid).child("accessTokenCall").setValue(newToken);
                handler.postDelayed(this, 3600 * 1000);
            }
        };
        handler.post(runnableCode);

        Query userQuery = usersDbRef.orderByChild("uid").equalTo(hisuid);
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    String name = ""+ds.child("name").getValue();
                    hisImage = ""  +ds.child("image").getValue();
                    String typingStatus = ""  +ds.child("typingTo").getValue();
                    //check typing status
                    if(typingStatus.equals(myUid)) {
                        userStatusTv.setText("Typing...");
                    }else{
                        String onlineStatus = ""+ds.child("onlineStatus").getValue();
                        //set data
                        nameTv.setText(name);
                        if(onlineStatus.equals("online")) {
                            userStatusTv.setText(onlineStatus);
                        }else{
                            //convert time stamp to dd/mm/yyyy hh:mm am/pm
                            long onlineStatusTime = Long.parseLong(onlineStatus);
                            String timeAgo = (String) DateUtils.getRelativeTimeSpanString(onlineStatusTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
                            userStatusTv.setText("Last seen: " + timeAgo);
                        }
                    }

                    try{
                        Picasso.get().load(hisImage).into(profileIv);
                    }catch (Exception e){
                        profileIv.setImageResource(R.drawable.baseline_account_circle_24);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //click buttom send
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                //get text from edit text
                String message = messageEt.getText().toString().trim();
                if(TextUtils.isEmpty(message)){
                    //empty, don't send
                    Toast.makeText(ChatActivity.this, "Empty message", Toast.LENGTH_SHORT).show();
                }else{
                    sendMessage(message);
                }
                //reset edit text after sending message
                messageEt.setText("");
            }
        });

        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0){
                    checkTyping("noOne");
                }else{
                    checkTyping(hisuid);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        moreIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popup = new PopupMenu(ChatActivity.this, moreIv);
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
                MenuItem blockItem = popup.getMenu().findItem(R.id.blockIv);
                blockItem.setTitle(isBlocked ? "Unblock" : "Block");
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.blockIv) {
                            // Handle option 1 click
                            if (isBlocked){
                                unBlockUser();
                                typingLayout.setVisibility(View.VISIBLE);
                            }else{
                                blockUser();
                                typingLayout.setVisibility(View.GONE);
                            }
                            // Toggle the block state
                            isBlocked = !isBlocked;
                            return true;
                        } else if (item.getItemId() == R.id.searchMessagesIv) {
                            // Xử lý khi nhấp vào tùy chọn tìm kiếm tin nhắn
                            showSearchDialog(); // Gọi hàm để hiển thị Dialog tìm kiếm
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

                popup.show(); // Showing popup menu
            }
        });

        duoIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hisuid == null || hisuid.isEmpty()) {
                    Toast.makeText(ChatActivity.this, "You can't make a call", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(ChatActivity.this, VideoCallActivity.class);
                intent.putExtra("to", hisuid);
                intent.putExtra("isIncomingCall", false);
                startActivity(intent);
            }
        });

        callIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hisuid == null || hisuid.isEmpty()) {
                    Toast.makeText(ChatActivity.this, "You can't make a call", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(ChatActivity.this, CallingActivity.class);
                intent.putExtra("to", hisuid);
                intent.putExtra("isIncomingCall", false);
                intent.putExtra("nameTo", nameTv.getText().toString().trim());
                startActivity(intent);
            }
        });

        initStringeeConnection();
        readMessage();
        checkIsBlocked();
        seenMessage();



    }

    private void showSearchDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ChatActivity.this);
        builder.setTitle("Search Messages");

        // Thiết lập EditText để nhập từ khóa
        final EditText input = new EditText(ChatActivity.this);
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
                    Toast.makeText(ChatActivity.this, "Please enter a keyword to search", Toast.LENGTH_SHORT).show();
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

    private void searchMessagesInFirebase(String keyword) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        // Chuyển đổi keyword sang chữ thường để tìm kiếm không phân biệt chữ hoa chữ thường
        String keywordLower = keyword.toLowerCase();
        // Tạo truy vấn để tìm kiếm các tin nhắn chứa keyword trong trường "message"
        ref.orderByChild("message")
            .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Chat> searchResults = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Lấy tin nhắn từ dataSnapshot
                    Chat chat = snapshot.getValue(Chat.class);
                    // Thêm tin nhắn vào danh sách kết quả nếu tin nhắn không null
                    if (chat != null && chat.getType().equals("text") && !chat.getMessage().equals("This message was deleted...") && ((chat.getSender().equals(myUid) && chat.getReceiver().equals(hisuid)) || (chat.getSender().equals(hisuid) && chat.getReceiver().equals(myUid)))) {
                        // Chuyển đổi nội dung tin nhắn sang chữ thường để so sánh không phân biệt chữ hoa chữ thường
                        String messageLower = chat.getMessage().toLowerCase();
                        if (messageLower.contains(keywordLower)) {
                            searchResults.add(chat);
                        }
                    }
                }

                if (searchResults.isEmpty()) {
                    Toast.makeText(ChatActivity.this, "No messages found", Toast.LENGTH_SHORT).show();
                } else {
                    // Hiển thị kết quả tìm kiếm
                    showSearchResults(searchResults);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Search failed: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void showSearchResults(List<Chat> searchResults) {
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
        AdapterSearchResults adapter = new AdapterSearchResults(searchResults, this);
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



    private void initStringeeConnection() {
        client = new StringeeClient(this);
        client.setConnectionListener(new StringeeConnectionListener() {
            @Override
            public void onConnectionConnected(StringeeClient stringeeClient, boolean b) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Stringee", "Connected as " + stringeeClient.getUserId() + " successfully!");
                    }
                });
            }

            @Override
            public void onConnectionDisconnected(StringeeClient stringeeClient, boolean b) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChatActivity.this, "Disconnect!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onIncomingCall(StringeeCall stringeeCall) {
                runOnUiThread(()->{
                    callMap.put(stringeeCall.getCallId(), stringeeCall);
                    Intent intent = new Intent(ChatActivity.this, CallingActivity.class);
                    intent.putExtra("callId", stringeeCall.getCallId());
                    intent.putExtra("isIncomingCall", true);
                    intent.putExtra("nameTo", nameTv.getText().toString().trim());
                    startActivity(intent);
                });
            }

            @Override
            public void onIncomingCall2(StringeeCall2 stringeeCall2) {
                runOnUiThread(() ->{
                    call2Map.put(stringeeCall2.getCallId(), stringeeCall2);
                    Intent intent = new Intent(ChatActivity.this, VideoCallActivity.class);
                    intent.putExtra("callId", stringeeCall2.getCallId());
                    intent.putExtra("isIncomingCall", true);
                    startActivity(intent);
                });
            }

            @Override
            public void onConnectionError(StringeeClient stringeeClient, StringeeError stringeeError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Error: " + stringeeError.getMessage());
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRequestNewToken(StringeeClient stringeeClient) {

            }

            @Override
            public void onCustomMessage(String s, JSONObject jsonObject) {

            }

            @Override
            public void onTopicMessage(String s, JSONObject jsonObject) {

            }
        });

        usersDbRef.child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String accessToken = dataSnapshot.child("accessTokenCall").getValue(String.class);
                    client.connect(accessToken.trim().toString());
                    System.out.println(accessToken);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi
            }
        });

    }

    private void checkIsBlocked() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(hisuid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if(ds.exists()){
                                isBlocked = true;
                                typingLayout.setVisibility(View.GONE); // Hide the typingLayout
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser() {
        //block the user , by addding uid to current user's "BlockedUsers" node
        //put values in hashmap to put in db
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid",hisuid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisuid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ChatActivity.this, "Block Successfully...", Toast.LENGTH_SHORT).show();
                        typingLayout.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void unBlockUser() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisuid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if(ds.exists()){
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(ChatActivity.this, "Unblocked Successfully...", Toast.LENGTH_SHORT).show();
                                                typingLayout.setVisibility(View.VISIBLE);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ChatActivity.this, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
//                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisuid)){
//                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
//                        hasSeenHashMap.put("isSeen", true);
//                        ds.getRef().updateChildren(hasSeenHashMap);
//                    }

                    if (chat != null && myUid != null && hisuid != null) {
                        if ((chat.getReceiver() != null && chat.getReceiver().equals(myUid)) && (chat.getSender() != null && chat.getSender().equals(hisuid))) {
                            HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                            hasSeenHashMap.put("isSeen", true);
                            ds.getRef().updateChildren(hasSeenHashMap);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                chatList = new ArrayList<>();
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            Chat chat = ds.getValue(Chat.class);
                            if (chat != null && myUid != null && hisuid != null) {
                                if ((chat.getReceiver() != null && chat.getReceiver().equals(myUid) && chat.getSender() != null && chat.getSender().equals(hisuid)) ||
                                        (chat.getReceiver() != null && chat.getReceiver().equals(hisuid) && chat.getSender() != null && chat.getSender().equals(myUid))) {
                                    chatList.add(chat);
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                                    adapterChat.notifyDataSetChanged();
                                    recyclerView.setAdapter(adapterChat);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).start();
    }

    private void sendMessage(String message) {
        //create chat list
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String timeStamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisuid);
        hashMap.put("message", message);
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("isSeen", false);
        hashMap.put("type", "text");
        databaseReference.child("Chats").push().setValue(hashMap);

        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if(notify){
                    sendNotification(hisuid, user.getName(),message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(myUid)
                .child(hisuid);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef1.child("id").setValue(hisuid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(hisuid)
                .child(myUid);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void sendImageMessage(Uri uriImage) throws IOException {
        notify = true;

        String timeStamp = ""+System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/" + "post_" + timeStamp;
        //getBitmap from image uri
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriImage);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //get url of uploaded image
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String downloadUri = uriTask.getResult().toString();
                if(uriTask.isSuccessful()){
                    //add image
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", myUid);
                    hashMap.put("receiver", hisuid);
                    hashMap.put("message", downloadUri);
                    hashMap.put("timeStamp", timeStamp);
                    hashMap.put("type", "image");
                    hashMap.put("isSeen", false);
                    databaseReference.child("Chats").push().setValue(hashMap);
                    //send notification
                    DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                    database.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if(notify){
                                sendNotification(hisuid, user.getName(),"Sent you a photo");
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList")
                            .child(myUid)
                            .child(hisuid);
                    chatRef1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                chatRef1.child("id").setValue(hisuid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                            .child(hisuid)
                            .child(myUid);
                    chatRef2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                chatRef2.child("id").setValue(myUid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this, "Fail to send image", Toast.LENGTH_SHORT).show();
            }
        });


    }


    private void sendNotification(final String hisuid, final String name, final String message) {
        DatabaseReference tokensRef = FirebaseDatabase.getInstance().getReference("Tokens").child(hisuid).child("token");;
        tokensRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String token = snapshot.getValue(String.class);
                    System.out.println(token);
                    if (token != null && !token.isEmpty()) {
                        sendNotificationToToken(hisuid, name, message, token);
                        System.out.println("Notification sent to token: " + token);
                    } else {
                        Log.e("Notification", "Token is null or empty for user: " + hisuid);
                    }
                } else {
                    Log.e("Notification", "No token record for user: " + hisuid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Notification", "Failed to get token", error.toException());
            }
        });
    }

    private void sendNotificationToToken(String hisuid, String name, String message, String token) {
        Data data = new Data(myUid, R.drawable.baseline_account_circle_24, name + ": " + message, "New Message", hisuid);
        Sender sender = new Sender(data, token);
        apiService.sendNotification(sender).enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                Toast.makeText(ChatActivity.this, "Notification sent: " + response.message(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.e("Notification Error", "Failed to send notification: " + t.getMessage());
            }
        });

    }




    private void checkUserstatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){
            //set email of logged in user
            // txt_proFile.setText(user.getEmail());
            myUid = user.getUid();
        }else{
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //update value of online status of current user
        dbRef.updateChildren(hashMap);
    }

    private void checkTyping(String typing){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        //update value of online status of current user
        dbRef.updateChildren(hashMap);
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
                    try {
                        sendImageMessage(uriImage);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(this, "Không thể lấy ảnh từ thư viện", Toast.LENGTH_SHORT).show();
                }
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                try {
                    sendImageMessage(uriImage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            Toast.makeText(this, "Hủy chọn ảnh", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onStart() {
        checkUserstatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //set offline
        String timeStamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timeStamp);
        checkTyping("noOne");
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }
}