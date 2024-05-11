package com.example.final_version;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();

    private FirebaseAuth mAuth;
    private Intent feed_intent;

    private boolean buttonBlocker;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        feed_intent = new Intent(this, FeedActivity.class);
        mAuth = FirebaseAuth.getInstance();
        buttonBlocker = false;
        db = FirebaseFirestore.getInstance();
    }

    public void login(View view) {
        EditText email = findViewById(R.id.login_email);
        EditText password = findViewById(R.id.login_password);

        String email_str = email.getText().toString();
        String password_str = password.getText().toString();

        if(!email_str.isEmpty() && !password_str.isEmpty() && email_str.contains("@")) {
            mAuth.signInWithEmailAndPassword(email_str, password_str).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {

                        Log.d(LOG_TAG, "User login successfully");
                        startActivity(feed_intent);
                    } else {
                        Log.d(LOG_TAG, "User wasn't logged in" + task.getException().getMessage());
                        findViewById(R.id.warning_login).setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            findViewById(R.id.warning_login).setVisibility(View.VISIBLE);
        }
    }

    public void login_animation(View view) {
        if (!buttonBlocker) {
            buttonBlocker = true;

            // 1. Position animation top of the wave
            RelativeLayout wave = findViewById(R.id.wave_top);
            ObjectAnimator animation = ObjectAnimator.ofFloat(wave, "translationY", -1300f);
            animation.setDuration(1000);

            ObjectAnimator animation2 = ObjectAnimator.ofFloat(wave, "translationY", 0);
            animation2.setDuration(1000);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(animation, animation2);
            animatorSet.start();

            // 2. Stretch animation bottom of the wave
            RelativeLayout wave_rect = findViewById(R.id.wave_bottom);
            wave_rect.animate()
                    .scaleY(18f)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(900);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.login_email).setVisibility(View.VISIBLE);
                    findViewById(R.id.login_password).setVisibility(View.VISIBLE);
                    findViewById(R.id.sing_in_button).setVisibility(View.VISIBLE);
                    findViewById(R.id.register_animation).setVisibility(View.VISIBLE);
                    findViewById(R.id.or).setVisibility(View.VISIBLE);
                    findViewById(R.id.sub_text1).setVisibility(View.VISIBLE);

                    findViewById(R.id.register_user_name).setVisibility(View.GONE);
                    findViewById(R.id.register_email).setVisibility(View.GONE);
                    findViewById(R.id.register_password).setVisibility(View.GONE);
                    findViewById(R.id.register_button).setVisibility(View.GONE);
                    findViewById(R.id.sign_in_animation).setVisibility(View.GONE);
                    findViewById(R.id.sub_text2).setVisibility(View.GONE);
                    findViewById(R.id.warning_login).setVisibility(View.GONE);

                    wave_rect.animate()
                            .scaleY(0)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .setDuration(1000);

                    buttonBlocker = false;
                }
            }, 1000);
        }
    }

    public void register(View view) {
        EditText email = findViewById(R.id.register_email);
        EditText user_name = findViewById(R.id.register_user_name);
        EditText password = findViewById(R.id.register_password);

        String user_name_str = user_name.getText().toString();
        String password_str = password.getText().toString();
        String email_str = email.getText().toString();

        if(!email_str.isEmpty() && !password_str.isEmpty() && email_str.contains("@")) {
            mAuth.createUserWithEmailAndPassword(email_str, password_str).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        // storing data in firestore db
                        userID = mAuth.getCurrentUser().getUid();
                        DocumentReference documentReference = db.collection("users").document(userID);
                        Map<String, Object> user_reg = new HashMap<>();
                        user_reg.put("userName", user_name_str);
                        user_reg.put("gmail", email_str);
                        user_reg.put("followersNum", 0);
                        user_reg.put("followedNum", 0);
                        documentReference.set(user_reg).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(LOG_TAG, "User was uploaded to database: " + userID);
                            }
                        });

                        // Updating display name in firebase register
                        user = FirebaseAuth.getInstance().getCurrentUser();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(user_name_str)
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(LOG_TAG, "User profile updated.");
                                        }
                                    }
                                });

                        Log.d(LOG_TAG, "User created successfully");
                        startActivity(feed_intent);
                    } else {
                        Log.d(LOG_TAG, "User wasn't created succecfully" + task.getException().getMessage());
                        findViewById(R.id.warning_register).setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            findViewById(R.id.warning_register).setVisibility(View.VISIBLE);
        }
    }

    public void register_animation(View view) {
        if(!buttonBlocker) {
            buttonBlocker = true;

            RelativeLayout wave = findViewById(R.id.wave_top);
            ObjectAnimator animation = ObjectAnimator.ofFloat(wave, "translationY", -1300f);
            animation.setDuration(1000);

            ObjectAnimator animation2 = ObjectAnimator.ofFloat(wave, "translationY", 0);
            animation2.setDuration(1000);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(animation, animation2);
            animatorSet.start();

            RelativeLayout wave_rect = findViewById(R.id.wave_bottom);
            wave_rect.animate()
                    .scaleY(18f)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(900);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.register_user_name).setVisibility(View.VISIBLE);
                    findViewById(R.id.register_email).setVisibility(View.VISIBLE);
                    findViewById(R.id.register_password).setVisibility(View.VISIBLE);
                    findViewById(R.id.register_button).setVisibility(View.VISIBLE);
                    findViewById(R.id.sign_in_animation).setVisibility(View.VISIBLE);
                    findViewById(R.id.sub_text2).setVisibility(View.VISIBLE);

                    findViewById(R.id.login_email).setVisibility(View.GONE);
                    findViewById(R.id.login_password).setVisibility(View.GONE);
                    findViewById(R.id.sing_in_button).setVisibility(View.GONE);
                    findViewById(R.id.register_animation).setVisibility(View.GONE);
                    findViewById(R.id.or).setVisibility(View.GONE);
                    findViewById(R.id.sub_text1).setVisibility(View.GONE);
                    findViewById(R.id.warning_login).setVisibility(View.GONE);

                    wave_rect.animate()
                            .scaleY(0)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .setDuration(1000);

                    buttonBlocker = false;
                }
            }, 1000);
        }
    }


}