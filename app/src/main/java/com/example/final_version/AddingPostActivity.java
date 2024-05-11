package com.example.final_version;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AddingPostActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    String userID;
    Intent feed_intent;
    Intent userTab_intent;
    StorageReference storageReference;
    Uri image;
    MaterialButton uploadImage, selectImage;
    ImageView imageView;
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    uploadImage.setEnabled(true);
                    image = result.getData().getData();
                    float alpha = 1;
                    imageView.setAlpha(alpha);
                    Glide.with(getApplicationContext()).load(image).into(imageView);
                }
            } else {
                Toast.makeText(AddingPostActivity.this, "Image or Title or Description missing", Toast.LENGTH_SHORT).show();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_post);
        EdgeToEdge.enable(this);
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

        user = FirebaseAuth.getInstance().getCurrentUser();

        userTab_intent = new Intent(this, UserTabActivity.class);

        FirebaseApp.initializeApp(AddingPostActivity.this);
        storageReference = FirebaseStorage.getInstance().getReference();

        imageView = findViewById(R.id.upload_imageView);
        selectImage = findViewById(R.id.upload_selectImage);
        uploadImage = findViewById(R.id.upload_materialbutton);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publish_post(image);
            }
        });
    }

    public void publish_post(Uri file) {
        LocalDateTime now = LocalDateTime.now();

        // Uploading img to storage
        StorageReference ref = storageReference.child("posts_images/" + user.getUid() + "/" + now.toString().replace("T", "_"));

        ref.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AddingPostActivity.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();

                // Uploading to Firestore
                EditText postTitle = findViewById(R.id.postTitle);
                EditText postDescription = findViewById(R.id.postDescription);

                String postTitle_str = postTitle.getText().toString();
                String postDescription_str = postDescription.getText().toString();

                DocumentReference postReference = db.collection("users").document(userID).collection("posts").document(now.toString().replace("T", " "));
                Map<String, Object> post_reg = new HashMap<>();
                post_reg.put("pic", "posts_images/" + user.getUid() + "/" + now.toString().replace("T", "_"));
                post_reg.put("title", postTitle_str);
                post_reg.put("description", postDescription_str);
                post_reg.put("likes", Arrays.asList(user.getUid()));


                postReference.set(post_reg).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(LOG_TAG, "User was uploaded to database: " + userID);
                        startActivity(feed_intent);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddingPostActivity.this, "Failed!" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void back(View view) {
        startActivity(userTab_intent);
    }
}