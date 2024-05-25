package com.example.chatapp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatapp.Adapter.FriendAdapter;
import com.example.chatapp.Adapter.ListFriendRequestAdapter;
import com.example.chatapp.Adapter.UserAdapter;
import com.example.chatapp.ListFriendRequestActivity;
import com.example.chatapp.Model.User;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FriendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FriendAdapter friendAdapter;
    private List<User> mFriends;
    private EditText search_users;
    private FirebaseUser firebaseUser;
    private ValueEventListener valueEventListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mFriends = new ArrayList<>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


//        search_users = view.findViewById(R.id.txt_search);
//        search_users.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (!s.toString().equals("")) {
//                    searchUsers(s.toString().toLowerCase());
//                }else{
//                    readUsers();
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        readFriends();
    }

    private void readFriends() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("friends").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mFriends.clear();
                for (DataSnapshot sn : snapshot.getChildren()) {
                    String friendId = sn.getKey();
                    FirebaseDatabase.getInstance().getReference("users").child(friendId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    User user = snapshot.getValue(User.class);
                                    mFriends.add(user);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
                friendAdapter = new FriendAdapter(getContext(), mFriends, false);
                recyclerView.setAdapter(friendAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    private void searchUsers(String str) {
//        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        Query query = FirebaseDatabase.getInstance().getReference("friends").child(firebaseUser.getUid()).orderByChild("email")
//                .startAt(str)
//                .endAt(str + "\\uf8ff");
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                mUsers.clear();
//                for (DataSnapshot sn : snapshot.getChildren()) {
//                    User user = sn.getValue(User.class);
//                    assert user != null;
//                    assert firebaseUser != null;
//                    if (!user.getId().equals(firebaseUser.getUid())) {
//                        mUsers.add(user);
//                    }
//                }
//                userAdapter = new FriendAdapter(getContext(), mUsers, false);
//                recyclerView.setAdapter(userAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }


}