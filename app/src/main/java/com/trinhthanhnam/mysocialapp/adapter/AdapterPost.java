package com.trinhthanhnam.mysocialapp.adapter;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.trinhthanhnam.mysocialapp.AddPostActivity;
import com.trinhthanhnam.mysocialapp.PostDetailActivity;
import com.trinhthanhnam.mysocialapp.PostLikedByActivity;
import com.trinhthanhnam.mysocialapp.R;
import com.trinhthanhnam.mysocialapp.ThereProfileActivity;
import com.trinhthanhnam.mysocialapp.model.Post;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyHolder> {
    Context context;
    List<Post> postList;
    String myUid;
    private DatabaseReference likesRef;
    private DatabaseReference postsRef;
    ExoPlayer player;

    boolean mProcessLike = false;

    public AdapterPost(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_post, parent, false);
        return new MyHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String uid = postList.get(position).getUid();
        String uEmail = postList.get(position).getuEmail();
        String uName = postList.get(position).getuName();
        String uDp = postList.get(position).getuDp();
        String pId = postList.get(position).getpId();
        String pTitle = postList.get(position).getpTitle();
        String pDescr = postList.get(position).getpDescr();
        String pImage = postList.get(position).getpImage();
        String pVideo = postList.get(position).getpVideo();
        String pTimeStamp = postList.get(position).getpTime();
        String pLikes= postList.get(position).getpLikes();
        String pComments= postList.get(position).getpComments();

        System.out.println("pTimeStamp: " + pTimeStamp);
        String pTime= null;
        long onlineStatusTime =0;
        if (pTimeStamp != null && !pTimeStamp.isEmpty()) {
            onlineStatusTime = Long.parseLong(pTimeStamp);
            pTime = (String) DateUtils.getRelativeTimeSpanString(onlineStatusTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
        }else{
            onlineStatusTime = 0;
        }

        //set data
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescr);
        holder.pLikesTv.setText(pLikes+" Likes");
        holder.pCommentsTv.setText(pComments+" Comments");
        setLikes(holder,pId);

        //set user
        Glide.with(context)
                .load(uDp)
                .placeholder(R.drawable.baseline_account_circle_24)
                .into(holder.uPictureIv);


        if (pVideo != null && !pVideo.isEmpty() && pImage == null) {
            player = new ExoPlayer.Builder(context).build();
            holder.postVideo.setPlayer(player);
            // Set the media item to be played
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(pVideo));
            System.out.println("sRCVideo: " + pVideo);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
            holder.postVideo.setVisibility(View.VISIBLE);
            holder.pImageIv.setVisibility(View.GONE);
        }

        // Use Glide to load the post image if it exists
        if( pImage == null && pVideo == null){
            holder.pImageIv.setVisibility(View.GONE);
            holder.postVideo.setVisibility(View.GONE);
        } else if(pImage != null && !pImage.isEmpty() && pVideo == null) {
            holder.pImageIv.setVisibility(View.VISIBLE);
            holder.postVideo.setVisibility(View.GONE);
            try{
                Glide.with(context)
                        .load(pImage)
                        .into(holder.pImageIv);

            }catch (Exception e){
            }
            holder.pImageIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId);
                    startActivity(context, intent, null);
                }
            });
        }


        //handle item click
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, uid, myUid, pId, pImage,pVideo);
            }
        });

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                final int pLikes = Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike = true;
                final String postIde = postList.get(position).getpId();
                final String postUid = postList.get(position).getUid();
                likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(mProcessLike){
                            if(snapshot.child(postIde).hasChild(myUid)){
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLike = false;
                            }else {
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                likesRef.child(postIde).child(myUid).setValue("Liked");
                                mProcessLike = false;
                                if (!myUid.equals(postUid)) {
                                    addToHisNotifications(""+uid, ""+pId, "liked your post");
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });


        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });
        holder.pDescriptionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });

        holder.pTitleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });

        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.pImageIv.getDrawable();
                if (bitmapDrawable == null) {
                    //post without image
                    shareTextOnly(pTitle, pDescr);
                } else {
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageandText(pTitle, pDescr, bitmap);
                }
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });

        holder.pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostLikedByActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
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
        context.startActivity(Intent.createChooser(intent, "Share Via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream fileInputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileInputStream);
            fileInputStream.flush();
            fileInputStream.close();
            uri = FileProvider.getUriForFile(context, "com.trinhthanhnam.mysocialapp.fileprovider", file);

        }catch (Exception e) {
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
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

    private void shareTextOnly(String pTitle, String pDescr) {
        String shareBody = pTitle + "\n" + pDescr;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(intent, "Share Via"));
    }

    private void setLikes(final MyHolder holder,final String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postKey).hasChild(myUid)){
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like,0,0,0);
                    holder.likeBtn.setText("Liked");
                }else{
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.notlike,0,0,0);
                    holder.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage, String pVideo) {
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);
        if(uid.equals(myUid)){
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Detail");
        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == 0){
                    beginDelete(pId, pImage, pVideo);
                } else if(id == 1){
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                } else if (id == 2) {
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage, String pVideo) {
        if(pImage == null && pVideo == null){
            deleteWithoutImage(pId);
        }
        if(pImage != null && pVideo == null) {
            deleteWithImage(pId, pImage);
        }
        if(pImage == null && pVideo != null) {
            deleteWithVideo(pId, pVideo);
        }
    }

    private void deleteWithVideo(String pId, String pVideo) {

        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pVideo);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                fquery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithImage(String pId, String pImage) {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                fquery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteWithoutImage(String pId) {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");
        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder
    class MyHolder extends RecyclerView.ViewHolder{
        ImageView uPictureIv, pImageIv;
        PlayerView postVideo;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv,pCommentsTv;
        ImageButton moreBtn;
        Button likeBtn, commentBtn, shareBtn;

        LinearLayout profileLayout;
        @SuppressLint("WrongViewCast")
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //init views
            uPictureIv = itemView.findViewById(R.id.postUImg);
            pImageIv = itemView.findViewById(R.id.postImageIv);
            postVideo = itemView.findViewById(R.id.postVideo);
            uNameTv = itemView.findViewById(R.id.postUName);
            pTimeTv = itemView.findViewById(R.id.postTime);
            pTitleTv = itemView.findViewById(R.id.postTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.postDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.postLikeTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.btnLike);
            commentBtn = itemView.findViewById(R.id.btnComment);
            shareBtn = itemView.findViewById(R.id.btnShare);
            profileLayout = itemView.findViewById(R.id.profileLayout);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);

            itemView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(@NonNull View v) {
                }
                @Override
                public void onViewDetachedFromWindow(@NonNull View v) {
                    if (player != null) {
                        player.release();
                        player = null;
                    }
                }
            });

        }
    }
}
