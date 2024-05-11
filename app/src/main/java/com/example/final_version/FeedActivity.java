package com.example.final_version;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeedActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private Intent user_intent;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private String userID;
    private LinearLayout scrollview_content;
    StorageReference storageReference;
    int likes_count;
    String likesClick;
    int likes_countClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feed);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user_intent = new Intent(this, UserTabActivity.class);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();
        db = FirebaseFirestore.getInstance();

        scrollview_content = findViewById(R.id.scrollview_content);
        loadFeed();
    }

    public void loadFeed() {
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot user_document : task.getResult()) {

                                // Getting the posts
                                db.collection("users").document(user_document.getId()).collection("posts")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {

                                                        for (QueryDocumentSnapshot post_document : task.getResult()) {

                                                            Log.d(LOG_TAG, post_document.getId() + " => " + post_document);

                                                            LinearLayout rowLinearLayout = new LinearLayout(FeedActivity.this);
                                                            rowLinearLayout.setOrientation(LinearLayout.VERTICAL);

                                                            // User of the post
                                                            TextView userNameTextView = new TextView(FeedActivity.this);
                                                            userNameTextView.setText(user_document.getString("userName"));
                                                            userNameTextView.setLayoutParams(new ViewGroup.LayoutParams(
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                                            ));
                                                            userNameTextView.setTextSize(25);

                                                            // Post date and time
                                                            TextView timeTextView = new TextView(FeedActivity.this);
                                                            String postTime = post_document.getId().substring(0, 19);
                                                            timeTextView.setText(postTime);
                                                            timeTextView.setLayoutParams(new ViewGroup.LayoutParams(
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                                            ));

                                                            // Image of the post
                                                            ImageView post_img = new ImageView(FeedActivity.this);
                                                            storageReference = FirebaseStorage.getInstance().getReference();
                                                            StorageReference photoReference = storageReference.child(post_document.getString("pic"));
                                                            Log.d(LOG_TAG, photoReference.toString());
                                                            final long ONE_MEGABYTE = 1024 * 1024;
                                                            photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                                @Override
                                                                public void onSuccess(byte[] bytes) {
                                                                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                                    post_img.setImageBitmap(bmp);
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception exception) {
                                                                    Toast.makeText(getApplicationContext(), "No Such file or Path found!!", Toast.LENGTH_LONG).show();
                                                                }
                                                            });
                                                            post_img.setLayoutParams(new ViewGroup.LayoutParams(
                                                                    1000,
                                                                    1000
                                                            ));

                                                            // Post title
                                                            TextView titleTextView = new TextView(FeedActivity.this);
                                                            titleTextView.setText(post_document.getString("title"));
                                                            titleTextView.setLayoutParams(new ViewGroup.LayoutParams(
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                                            ));
                                                            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);

                                                            // Description of the post
                                                            TextView descriptionTextView = new TextView(FeedActivity.this);
                                                            descriptionTextView.setText(post_document.getString("description"));
                                                            descriptionTextView.setLayoutParams(new ViewGroup.LayoutParams(
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                                            ));

                                                            // Like Button
                                                            Button like_button = new Button(FeedActivity.this);
                                                            like_button.setText("Like");
                                                            like_button.setLayoutParams(new ViewGroup.LayoutParams(
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                                            ));
                                                            // Likes on this post
                                                            TextView likesTextView = new TextView(FeedActivity.this);
                                                            likes_count = 0;
                                                            String likes = post_document.get("likes").toString();
                                                            for (int i = 0; i < likes.length(); i++) {
                                                                if (likes.charAt(i) == ',') {
                                                                    likes_count++;
                                                                    /*Log.d(LOG_TAG, "likes: " + likes_count);*/
                                                                }
                                                            }
                                                            likesTextView.setText("Likes: " + likes_count);

                                                            LinearLayout likeRowLinearLayout = new LinearLayout(FeedActivity.this);
                                                            likeRowLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                                                            likeRowLinearLayout.addView(like_button);
                                                            likeRowLinearLayout.addView(likesTextView);
                                                            ViewGroup.MarginLayoutParams paramslikeRow = new ViewGroup.MarginLayoutParams(
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                                            );
                                                            paramslikeRow.setMargins(0,0,30,0);
                                                            likeRowLinearLayout.setLayoutParams(paramslikeRow);


                                                            // Like or Liked state
                                                            DocumentReference likes_db = db.collection("users").document(user_document.getId())
                                                                    .collection("posts").document(post_document.getId());

                                                            likes_db.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                    String likes = documentSnapshot.get("likes").toString();
                                                                    if (userID.equals(user_document.getId())) {
                                                                        like_button.setText("Own Post");
                                                                        like_button.setTextColor(Color.parseColor("#808080"));
                                                                    } else if (likes.contains(userID)) {
                                                                        like_button.setText("Liked");
                                                                        like_button.setTextColor(Color.parseColor("#008000"));
                                                                    } else {
                                                                        like_button.setText("Like");
                                                                        like_button.setTextColor(Color.parseColor("#808080"));
                                                                    }
                                                                }
                                                            });

                                                            like_button.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    likes_db.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                            String likes = documentSnapshot.get("likes").toString();
                                                                            if (!userID.equals(user_document.getId())) {
                                                                                if(!likes.contains(userID)) {
                                                                                    likes_db.update("likes", FieldValue.arrayUnion(userID)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void unused) {
                                                                                            likes_countClick = 0;
                                                                                            likes_db.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                                @Override
                                                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                                    likesClick = documentSnapshot.get("likes").toString();

                                                                                                    for (int i = 0; i < likesClick.length(); i++) {
                                                                                                        if (likesClick.charAt(i) == ',') {
                                                                                                            likes_countClick++;
                                                                                                            Log.d(LOG_TAG, "liked: " + likes_countClick);
                                                                                                        }
                                                                                                    }
                                                                                                    likesTextView.setText("Likes: " + (likes_countClick));
                                                                                                    like_button.setText("Liked");
                                                                                                    like_button.setTextColor(Color.parseColor("#008000"));
                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    });

                                                                                } else {
                                                                                    likes_db.update("likes", FieldValue.arrayRemove(userID)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void unused) {
                                                                                            likes_countClick = 0;
                                                                                            likes_db.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                                @Override
                                                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                                    likesClick = documentSnapshot.get("likes").toString();
                                                                                                    Log.d(LOG_TAG, likesClick);
                                                                                                    for (int i = 0; i < likesClick.length(); i++) {
                                                                                                        if (likesClick.charAt(i) == ',') {
                                                                                                            likes_countClick++;
                                                                                                            Log.d(LOG_TAG, "liked: " + likes_countClick);
                                                                                                        }
                                                                                                    }
                                                                                                    likesTextView.setText("Likes: " + (likes_countClick));
                                                                                                    like_button.setText("Like");
                                                                                                    like_button.setTextColor(Color.parseColor("#808080"));
                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            });

                                                            // Adding the text
                                                            rowLinearLayout.addView(userNameTextView);
                                                            rowLinearLayout.addView(timeTextView);
                                                            rowLinearLayout.addView(post_img);
                                                            rowLinearLayout.addView(titleTextView);
                                                            rowLinearLayout.addView(descriptionTextView);
                                                            rowLinearLayout.addView(likeRowLinearLayout);
                                                            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                                            );
                                                            params.setMargins(0,30,0,30);
                                                            rowLinearLayout.setLayoutParams(params);

                                                            scrollview_content.addView(rowLinearLayout);
                                                        }


                                                } else {
                                                    Log.d(LOG_TAG, "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.d(LOG_TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void openUserTab(View view) {
        startActivity(user_intent);
    }
}