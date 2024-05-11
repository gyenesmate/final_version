package com.example.final_version;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class UserTabActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();

    private Intent feed_intent;
    private Intent login_intent;
    private Intent changeProfilePic_intent;
    private Intent addPost_intent;
    private Intent deletingPost_intent;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private String userID;
    StorageReference storageReference;
    private int likes_count;
    private int posts_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_tab);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        login_intent = new Intent(this, MainActivity.class);
        feed_intent = new Intent(this, FeedActivity.class);
        changeProfilePic_intent = new Intent(this, PofilePicChangeActivity.class);
        addPost_intent = new Intent(this, AddingPostActivity.class);
        deletingPost_intent = new Intent(this, DeletingPostActivity.class);
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userID = user.getUid();


        loadUserInfo();
    }

    public void loadUserInfo() {
        // Loading Profile picture
        ImageView profile_pic = findViewById(R.id.profile_pic);
        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference photoReference = storageReference.child("profile_pic_images/" + userID);
        final long ONE_MEGABYTE = 1024 * 1024;
        photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                float alpha = 1;
                profile_pic.setAlpha(alpha);
                profile_pic.setImageBitmap(bmp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Click Porfile Picture to add one!", Toast.LENGTH_LONG).show();
            }
        });

        // Loading user data
        TextView userName = findViewById(R.id.userName);
        TextView gmail = findViewById(R.id.gmail);
        db.collection("users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(LOG_TAG, "DocumentSnapshot data: " + document.getData().get("userName"));
                        userName.setText(document.getData().get("userName").toString());
                        gmail.setText(document.getData().get("gmail").toString());
                    } else {
                        Log.d(LOG_TAG, "No such document");
                    }
                } else {
                    Log.d(LOG_TAG, "get failed with ", task.getException());

                }
            }
        });

        TextView followers_num = findViewById(R.id.followers_number);
        TextView followed_num = findViewById(R.id.followed_number);
        likes_count = 0;
        posts_count = 0;
        db.collection("users").document(userID).collection("posts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot post_document : task.getResult()) {
                                String likes = post_document.get("likes").toString();
                                for (int i = 0; i < likes.length(); i++) {
                                    if (likes.charAt(i) == ',') {
                                        likes_count++;
                                        /*Log.d(LOG_TAG, "likes: " + likes_count);*/
                                    }
                                }
                                posts_count++;
                                /*Log.d(LOG_TAG, "posts:" + posts_count);*/
                            }
                            followers_num.setText(String.valueOf(posts_count));
                            followed_num.setText(String.valueOf(likes_count));
                        } else {
                            Log.d(LOG_TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void openFeed(View view) {
        startActivity(feed_intent);
    }

    public void changeUserPic(View view) {
        startActivity(changeProfilePic_intent);
    }

    public void addPost(View view) {
        startActivity(addPost_intent);
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(login_intent);
    }

    public void deletingPost(View view) {
        startActivity(deletingPost_intent);
    }
}