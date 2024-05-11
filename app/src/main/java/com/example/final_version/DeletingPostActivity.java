package com.example.final_version;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class DeletingPostActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    String userID;
    Intent feed_intent;
    Intent user_intent;
    LinearLayout deletingContent_view;
    CheckBox checkBox;
    int counter;
    ArrayList<String> postTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_deleting_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        feed_intent = new Intent(this, FeedActivity.class);
        user_intent = new Intent(this, UserTabActivity.class);

        deletingContent_view = findViewById(R.id.deletingContent_view);

        postTimes = new ArrayList<String>();
        loadPosts();
    }

    public void loadPosts() {
        counter = 0;
        db.collection("users").document(userID).collection("posts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot post_document : task.getResult()) {


                            LinearLayout rowLinearLayout = new LinearLayout(DeletingPostActivity.this);
                            rowLinearLayout.setOrientation(LinearLayout.VERTICAL);

                            TextView timeTextView = new TextView(DeletingPostActivity.this);
                            String postTime = post_document.getId().substring(0, 19);
                            timeTextView.setText(postTime);
                            timeTextView.setLayoutParams(new ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            ));

                            TextView titleTextView = new TextView(DeletingPostActivity.this);
                            titleTextView.setText(post_document.getString("title"));
                            titleTextView.setLayoutParams(new ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            ));

                            checkBox = new CheckBox(DeletingPostActivity.this);
                            checkBox.setId(counter);
                            checkBox.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(!postTimes.contains(post_document.getId())) {
                                        postTimes.add(post_document.getId());
                                        Log.d(LOG_TAG, "Selected: " + post_document.getId());
                                    } else {
                                        postTimes.remove(post_document.getId());
                                        Log.d(LOG_TAG, "Deselected: " + post_document.getId());
                                    }
                                }
                            });

                            rowLinearLayout.addView(timeTextView);
                            rowLinearLayout.addView(titleTextView);
                            rowLinearLayout.addView(checkBox);

                            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(0,20,0,20);
                            rowLinearLayout.setLayoutParams(params);

                            deletingContent_view.addView(rowLinearLayout);

                            counter = counter + 1;
                        }
                    }
                });
    }


    public void deletePosts(View view) {
        for (String deletingTimes : postTimes) {
            db.collection("users").document(userID).collection("posts").document(deletingTimes)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(LOG_TAG, "DocumentSnapshot successfully deleted! ->" + deletingTimes);
                        }
                    });
        }
        startActivity(feed_intent);
    }

    public void back(View view) {
        startActivity(user_intent);
    }
}