package com.trinhthanhnam.mysocialapp.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trinhthanhnam.mysocialapp.R;
import com.trinhthanhnam.mysocialapp.model.Chat;
import com.trinhthanhnam.mysocialapp.model.User;

import java.util.HashMap;
import java.util.List;

public class AdapterSearchResults extends RecyclerView.Adapter<AdapterSearchResults.ViewHolder> {

    private List<Chat> searchResults;
    private Context context;

    public AdapterSearchResults(List<Chat> searchResults, Context context) {
        this.searchResults = searchResults;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_search_results, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = searchResults.get(position);

        String senderUid = chat.getSender();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(senderUid);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Lấy thông tin của người dùng từ dataSnapshot
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        // Nếu thông tin tồn tại, hiển thị userName
                        holder.senderTextView.setText(user.getName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
        long senderTime = Long.parseLong(chat.getTimestamp());
        String dateTime = (String) DateUtils.getRelativeTimeSpanString(senderTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
        holder.messageTextView.setText(chat.getMessage());
        holder.timeTextView.setText(dateTime);
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView senderTextView;
        private TextView messageTextView;
        private TextView timeTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.senderTv);
            messageTextView = itemView.findViewById(R.id.messageTv);
            timeTextView = itemView.findViewById(R.id.timeTv);
        }
    }
}
