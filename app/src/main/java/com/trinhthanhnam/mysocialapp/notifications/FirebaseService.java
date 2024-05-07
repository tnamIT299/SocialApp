package com.trinhthanhnam.mysocialapp.notifications;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;

public class FirebaseService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (firebaseUser != null) {
//            updateTokenInFirebaseDatabase(token);
//        }

        if (firebaseUser != null) {
            // Update token in the database
            updateTokenInFirebaseDatabase(firebaseUser.getUid(), token);
        }
    }

    private void updateTokenInFirebaseDatabase(String userId,String newToken) {
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
//        Token token = new Token(newToken);
//        reference.child(firebaseUser.getUid()).setValue(token);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Tokens").child(userId);

        // Assuming you're storing the token under a child named 'fcmToken'
        database.child("token").setValue(newToken)
                .addOnSuccessListener(aVoid -> Log.d("FCM", "Token updated successfully"))
                .addOnFailureListener(e -> Log.e("FCM", "Failed to update token", e));
    }
}
