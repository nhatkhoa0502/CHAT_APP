package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Model.Chat;
import com.example.chatapp.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import java.util.ArrayList;
import java.util.List;

public class CallingActivity extends AppCompatActivity {
    private String TAG = CallingActivity.this.getClass().getSimpleName();
    private ImageView cancelCallBtn, acceptCallBtn;
    private String receiverUserId = "";
    private String senderUserId = "";
    private String callingID = "";
    private String ringingID = "";
    private String checker = "";
    private TextView txt_username;
    private CircleImageView civ_profile_image;
    private DatabaseReference userRef;
    private MediaPlayer mediaPlayer;
    private ValueEventListener valueEventListener1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        receiverUserId = getIntent().getExtras().get("userid").toString();
        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mediaPlayer = MediaPlayer.create(this, R.raw.iphone);

        txt_username = findViewById(R.id.username);
        civ_profile_image = findViewById(R.id.profile_image);
        cancelCallBtn = findViewById(R.id.cancel_call);
        acceptCallBtn = findViewById(R.id.make_call);

        cancelCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                checker = "clicked";
                final HashMap<String, Object> cancel = new HashMap<>();
                cancel.put("cancel", "cancel");
                userRef.child(senderUserId).updateChildren(cancel);
                userRef.child(receiverUserId).updateChildren(cancel);
                cancelCallingUser();
            }
        });

        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();

                final HashMap<String, Object> callingPickUpMap = new HashMap<>();
                callingPickUpMap.put("picked", "picked");
                userRef.child(senderUserId).child("Ringing")
                        .updateChildren(callingPickUpMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isComplete()) {
                                    Intent intent = new Intent(CallingActivity.this, VideoChatActivity.class);
                                    intent.putExtra("senderUserId", senderUserId);
                                    intent.putExtra("receiverUserId", receiverUserId);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mediaPlayer.start();

        userRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!checker.equals("clicked") && !dataSnapshot.hasChild("Calling") && !dataSnapshot.hasChild("Ringing")) {

                    final HashMap<String, Object> callingInfo = new HashMap<>();
                    callingInfo.put("calling", receiverUserId);

                    userRef.child(senderUserId).child("Calling").updateChildren(callingInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                final HashMap<String, Object> ringingInfo = new HashMap<>();
                                ringingInfo.put("ringing", senderUserId);

                                userRef.child(receiverUserId).child("Ringing").updateChildren(ringingInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // load in4 người nhận
                                        userRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DataSnapshot dataSnapshot = task.getResult();
                                                    if (dataSnapshot.child(senderUserId).hasChild("Calling") && !dataSnapshot.child(senderUserId).hasChild("Ringing")) {
                                                        User user = dataSnapshot.child(receiverUserId).getValue(User.class);
                                                        txt_username.setText(user.getName());
                                                        Glide.with(CallingActivity.this).load(user.getProfile()).into(civ_profile_image);
                                                    }
                                                } else {
                                                    Log.e(TAG, "Lỗi load thông tin người nhận lên calling activity!!");
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        valueEventListener1 = userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(senderUserId).hasChild("Ringing") && !dataSnapshot.child(senderUserId).hasChild("Calling")) {
                    acceptCallBtn.setVisibility(View.VISIBLE);

                    // load in4 người gọi
                    String userid = dataSnapshot.child(senderUserId).child("Ringing").child("ringing").getValue().toString();
                    String username = dataSnapshot.child(userid).child("name").getValue(String.class);
                    String profile_image = dataSnapshot.child(userid).child("profile").getValue(String.class);
                    txt_username.setText(username);
                    Glide.with(CallingActivity.this).load(profile_image).into(civ_profile_image);
                }

                if (dataSnapshot.child(receiverUserId).child("Ringing").hasChild("picked")) {
                    mediaPlayer.stop();
                    Intent intent = new Intent(CallingActivity.this, VideoChatActivity.class);
                    intent.putExtra("senderUserId", senderUserId);
                    intent.putExtra("receiverUserId", receiverUserId);
                    startActivity(intent);
                    finish();
                }

                if (dataSnapshot.child(receiverUserId).hasChild("cancel")) {
                    userRef.child(receiverUserId).child("cancel").removeValue();
                    mediaPlayer.stop();
                    finish();
                }

                if (dataSnapshot.child(senderUserId).hasChild("cancel")) {
                    userRef.child(senderUserId).child("cancel").removeValue();
                    mediaPlayer.stop();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void cancelCallingUser() {
        // from sender side
        userRef.child(senderUserId).child("Calling").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("calling")) {
                    callingID = dataSnapshot.child("calling").getValue().toString();

                    userRef.child(callingID).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                userRef.child(senderUserId).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // from receiver side
        userRef.child(senderUserId).child("Ringing").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("ringing")) {

                    ringingID = dataSnapshot.child("ringing").getValue().toString();

                    userRef.child(ringingID).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                userRef.child(senderUserId).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Gỡ bỏ tất cả các ValueEventListener khi activity kết thúc
        userRef.removeEventListener(valueEventListener1);
    }
}