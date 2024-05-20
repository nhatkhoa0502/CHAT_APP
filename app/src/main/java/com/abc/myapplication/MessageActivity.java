package com.abc.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Adapter.MessageAdapter;
import com.example.chatapp.Model.Chat;
import com.example.chatapp.Model.User;
import com.example.chatapp.Notification.APIService;
import com.example.chatapp.Notification.Client;
import com.example.chatapp.Notification.Data;
import com.example.chatapp.Notification.MyResponse;
import com.example.chatapp.Notification.Sender;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MessageActivity extends AppCompatActivity {
    Toolbar toolbar;
    CircleImageView profileImage;
    TextView username;
    ImageButton btn_send, btn_mic;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    DatabaseReference tokenRef;
    Intent intent;
    EditText txt_message;
    MessageAdapter messageAdapter;
    List<Chat> listChat;
    RecyclerView recyclerView;
    String userid;
    private ValueEventListener valueEventListener1, seenListener;
    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_arrow_back_24_white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        btn_mic = findViewById(R.id.btn_mic);
        txt_message = findViewById(R.id.txt_message);
        recyclerView = findViewById(R.id.recycler_view_message);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        tokenRef = FirebaseDatabase.getInstance().getReference("tokens");

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = txt_message.getText().toString();
                if (!message.equals("")) {
                    message = message.trim(); // Loại bỏ khoảng trắng ở đầu và cuối chuỗi
                    sendMessage(firebaseUser.getUid(), userid, message, new Date().getTime());
                    sendNotification(firebaseUser.getDisplayName(), message);
                } else {
                    Toast.makeText(MessageActivity.this, "Enter The Message First", Toast.LENGTH_SHORT).show();
                }
                txt_message.setText("");
            }
        });

        btn_mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessageActivity.this, MicRecorderActivity.class);
                intent.putExtra("senderId", firebaseUser.getUid());
                intent.putExtra("receiverId", userid);
                startActivity(intent);
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getName());
                Glide.with(MessageActivity.this).load(user.getProfile()).into(profileImage);
                readMessage(firebaseUser.getUid(), userid, user.getProfile());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        seenMessage(userid);
    }

    private void sendNotification(String senderName, String message) {
        tokenRef.child(userid).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String token = dataSnapshot.getValue(String.class);
                Data data = new Data(senderName, message);
                Sender sender = new Sender(data, token);
                apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                        if (response.code() == 200) {
                            if (response.body().success != 1) {
                                Toast.makeText(MessageActivity.this, "Failed ", Toast.LENGTH_LONG);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenMessage(final String userid) {
        databaseReference = FirebaseDatabase.getInstance().getReference("chats");
        seenListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot sn : snapshot.getChildren()) {
                    Chat chat = sn.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        sn.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String sender, String receiver, String message, long time) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("duration", 0);
        hashMap.put("time", time);
        hashMap.put("isseen", false);
        reference.child("chats").push().setValue(hashMap);
    }

    private void readMessage(final String senderUserId, final String receiverUserId, final String imageUrl) {
        listChat = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("chats");
        valueEventListener1 = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listChat.clear();
                for (DataSnapshot sn : snapshot.getChildren()) {
                    Chat chat = sn.getValue(Chat.class);
                    if (chat.getReceiver().equals(senderUserId) && chat.getSender().equals(receiverUserId) ||
                            chat.getReceiver().equals(receiverUserId) && chat.getSender().equals(senderUserId)) {
                        listChat.add(chat);
                    }
                }
                //test khác video
                messageAdapter = new MessageAdapter(MessageActivity.this, listChat, imageUrl);
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.call_video) {
            Intent callingIntent = new Intent(MessageActivity.this, CallingActivity.class);
            callingIntent.putExtra("userid", userid);
            startActivity(callingIntent);
//            finish();
            return true;

        }
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Gỡ bỏ tất cả các ValueEventListener khi activity kết thúc
        databaseReference = FirebaseDatabase.getInstance().getReference("chats");
        databaseReference.removeEventListener(seenListener);
        databaseReference.removeEventListener(valueEventListener1);
    }

    private void status(String status) {
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        databaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference = FirebaseDatabase.getInstance().getReference("chats");
        databaseReference.removeEventListener(seenListener);

        status("offline");
    }
}