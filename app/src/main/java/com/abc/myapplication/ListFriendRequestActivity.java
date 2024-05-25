package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp.Adapter.ListFriendRequestAdapter;
import com.example.chatapp.Adapter.UserAdapter;
import com.example.chatapp.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListFriendRequestActivity extends AppCompatActivity {
    private String TAG = ListFriendRequestActivity.this.getClass().getSimpleName();
    private RecyclerView recyclerView;
    private ListFriendRequestAdapter adapter;
    private List<User> mUsers;
    private ValueEventListener valueEventListener;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friend_request);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        mUsers = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        readListFriendRequest();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseDatabase.getInstance().getReference("requests").child(firebaseUser.getUid()).removeEventListener(valueEventListener);
    }

    private void readListFriendRequest() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("requests").child(firebaseUser.getUid());
        valueEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                if (!snapshot.exists()) {
                    adapter = new ListFriendRequestAdapter(ListFriendRequestActivity.this, mUsers);
                    recyclerView.setAdapter(adapter);
                    return;
                }
                for (DataSnapshot sn : snapshot.getChildren()) {
                    String userRequestId = sn.getKey();

                    FirebaseDatabase.getInstance().getReference("users").child(userRequestId)
                            .get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                @Override
                                public void onSuccess(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        User user = dataSnapshot.getValue(User.class);
                                        mUsers.add(user);
                                        adapter = new ListFriendRequestAdapter(ListFriendRequestActivity.this, mUsers);
                                        recyclerView.setAdapter(adapter);
                                        adapter.setOnItemClickListener(new ListFriendRequestAdapter.OnItemClickListener() {
                                            @Override
                                            public void onAcceptClick(int position) {
                                                // Xử lý sự kiện khi click nút accept
                                                User user = mUsers.get(position);
                                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                                                HashMap<String, Object> hashMap = new HashMap<>();
                                                hashMap.put("isFriend", true);
                                                reference.child("friends").child(firebaseUser.getUid()).child(user.getId()).setValue(hashMap);
                                                reference.child("friends").child(user.getId()).child(firebaseUser.getUid()).setValue(hashMap);

                                                reference.child("requests").child(firebaseUser.getUid()).child(user.getId()).removeValue();

                                                Toast.makeText(ListFriendRequestActivity.this, "Accepted success: " + user.getName(), Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onRejectClick(int position) {
                                                // Xử lý sự kiện khi click nút reject
                                                User user = mUsers.get(position);

                                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                                                reference.child("requests").child(firebaseUser.getUid()).child(user.getId()).removeValue();

                                                Toast.makeText(ListFriendRequestActivity.this, "Rejected success: " + user.getName(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}