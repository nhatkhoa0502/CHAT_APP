package com.abc.myapplication;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.chatapp.Fragments.ChatsFragment;
import com.example.chatapp.Fragments.FriendsFragment;
import com.example.chatapp.Fragments.ProfileFragment;
import com.example.chatapp.Fragments.QrCodeFragment;
import com.example.chatapp.Fragments.UsersFragment;
import com.example.chatapp.Notification.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private DatabaseReference reference;
    private DatabaseReference usersRef;
    private DatabaseReference tokenRef;
    private String currentUserId;
    private ValueEventListener valueEventListener1, valueEventListener2;
    private int colorPrimary;
    private int colorWhite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colorPrimary = ContextCompat.getColor(MainActivity.this, R.color.colorPrimary);
        colorWhite = ContextCompat.getColor(MainActivity.this, R.color.white);

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        tokenRef = FirebaseDatabase.getInstance().getReference("tokens");

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager viewPager = findViewById(R.id.viewPager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
        viewPagerAdapter.addFragment(new FriendsFragment(), "Friends");
        viewPagerAdapter.addFragment(new QrCodeFragment(), "QR Code");
        viewPagerAdapter.addFragment(new UsersFragment(), "Users");
        viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_chat_24);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_friend_24);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_qr_code_24);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_user_24);
        tabLayout.getTabAt(4).setIcon(R.drawable.ic_person_24);

        tabLayout.getTabAt(0).getIcon().setColorFilter(new PorterDuffColorFilter(colorPrimary, PorterDuff.Mode.SRC_IN));
        tabLayout.getTabAt(1).getIcon().setColorFilter(new PorterDuffColorFilter(colorWhite, PorterDuff.Mode.SRC_IN));
        tabLayout.getTabAt(2).getIcon().setColorFilter(new PorterDuffColorFilter(colorWhite, PorterDuff.Mode.SRC_IN));
        tabLayout.getTabAt(3).getIcon().setColorFilter(new PorterDuffColorFilter(colorWhite, PorterDuff.Mode.SRC_IN));
        tabLayout.getTabAt(4).getIcon().setColorFilter(new PorterDuffColorFilter(colorWhite, PorterDuff.Mode.SRC_IN));
        // thay đổi màu sắc của icon của tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(new PorterDuffColorFilter(colorPrimary, PorterDuff.Mode.SRC_IN));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(new PorterDuffColorFilter(colorWhite, PorterDuff.Mode.SRC_IN));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        checkForReceivingCall();
        updateToken();
    }

    private void checkForReceivingCall() {
        valueEventListener2 = usersRef.child(currentUserId).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("ringing") && !dataSnapshot.hasChild("picked")) {
                    String calledBy = dataSnapshot.child("ringing").getValue().toString();
                    Intent callingIntent = new Intent(MainActivity.this, CallingActivity.class);
                    callingIntent.putExtra("userid", calledBy);
                    startActivity(callingIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Lấy token thành công
                        String tokenStr = task.getResult();
                        Log.d(TAG, "FCM Token: " + tokenStr);

                        // Tiến hành lưu token lên realtime database
                        Token token = new Token(tokenStr);
                        tokenRef.child(currentUserId).setValue(token);
                    }
                });
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            this.fragments.add(fragment);
            this.titles.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return this.titles.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
         status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
         status("offline");
    }

    private void status(String status) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        FirebaseDatabase.getInstance().getReference("users").child(currentUserId).updateChildren(hashMap);
    }
}
