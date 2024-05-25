package com.abc.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chatapp.Model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileUserViewActivity extends AppCompatActivity {
    private Intent intent;
    private String userid;
    CircleImageView profile_image;
    TextView username;
    RelativeLayout send_friend_request, cancel_friend_request;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user_view);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        send_friend_request = findViewById(R.id.send_friend_request);
        cancel_friend_request = findViewById(R.id.cancel_friend_request);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        intent = getIntent();
        userid = intent.getStringExtra("userid");

        loadProfile();

        send_friend_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_friend_request.setVisibility(View.GONE);
                cancel_friend_request.setVisibility(View.VISIBLE);
                sendFriendRequest();
            }
        });
        cancel_friend_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_friend_request.setVisibility(View.VISIBLE);
                cancel_friend_request.setVisibility(View.GONE);
                cancelFriendRequest();
            }
        });
    }

    private void cancelFriendRequest() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("requests").child(userid).child(firebaseUser.getUid()).removeValue();
    }

    private void sendFriendRequest() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", "pending");
        reference.child("requests").child(userid).child(firebaseUser.getUid()).setValue(hashMap);
    }

    private void loadProfile() {

        FirebaseDatabase.getInstance().getReference("users").child(userid).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    username.setText(user.getName());
                    Glide.with(ProfileUserViewActivity.this).load(user.getProfile()).into(profile_image);
                }
                FirebaseDatabase.getInstance().getReference("requests").child(userid).child(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            send_friend_request.setVisibility(View.GONE);
                            cancel_friend_request.setVisibility(View.VISIBLE);
                        } else {
                            send_friend_request.setVisibility(View.VISIBLE);
                            cancel_friend_request.setVisibility(View.GONE);
                        }
                        FirebaseDatabase.getInstance().getReference("friends").child(firebaseUser.getUid()).child(userid).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    send_friend_request.setVisibility(View.GONE);
                                    cancel_friend_request.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                });
            }
        });
    }
}