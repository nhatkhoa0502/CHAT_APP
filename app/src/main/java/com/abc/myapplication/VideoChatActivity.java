package com.abc.myapplication;

import android.Manifest;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import java.util.HashMap;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_KEY = "47881211";
    private static String SESSION_ID = "1_MX40Nzg4MTIxMX5-MTcxNjEwNTU1NDk1M340b0J0SmtxMWQ5VkpIL3pvcHBuV3VDczl-fn4";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00Nzg4MTIxMSZzaWc9MWY3ZjEyZGRmZWYyODk4ODdlZjA5Njk1YmZjZjI0OTQ5Zjc0MjBlZTpzZXNzaW9uX2lkPTFfTVg0ME56ZzRNVEl4TVg1LU1UY3hOakV3TlRVMU5EazFNMzQwYjBKMFNtdHhNV1E1VmtwSUwzcHZjSEJ1VjNWRGN6bC1mbjQmY3JlYXRlX3RpbWU9MTcxNjEwNTU2NSZub25jZT0wLjY0OTc0MTc1NzcxNTEzMTImcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTcxODY5NzU2MyZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;
    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber msubscriber;
    private ImageView closeVideoChatBtn;
    private DatabaseReference userRef;
    private String senderUserId = "", receiverUserId = "", userID = "";
    private ValueEventListener valueEventListener1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        senderUserId = getIntent().getExtras().get("senderUserId").toString();
        receiverUserId = getIntent().getExtras().get("receiverUserId").toString();

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        closeVideoChatBtn = findViewById(R.id.close_video_chat_btn);
        closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final HashMap<String, Object> cancel = new HashMap<>();
                cancel.put("cancel", "cancel");
                userRef.child(senderUserId).updateChildren(cancel);
                userRef.child(receiverUserId).updateChildren(cancel);
            }
        });
        valueEventListener1 = userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userID).hasChild("Ringing") && dataSnapshot.child(userID).hasChild("cancel")) {
                    userRef.child(userID).child("Ringing").removeValue();
                    userRef.child(userID).child("cancel").removeValue();

                    if (mPublisher != null) {
                        mSession.unpublish(mPublisher);
                        mPublisher.destroy();
                        mPublisher = null;
                    }
                    if (msubscriber != null) {
                        mSession.unsubscribe(msubscriber);
                        msubscriber.destroy();
                        msubscriber = null;
                    }
                    mSession.disconnect();
                    finish();
                }
                if (dataSnapshot.child(userID).hasChild("Calling") && dataSnapshot.child(userID).hasChild("cancel")) {
                    userRef.child(userID).child("Calling").removeValue();
                    userRef.child(userID).child("cancel").removeValue();

                    if (mPublisher != null) {
                        mSession.unpublish(mPublisher);
                        mPublisher.destroy();
                        mPublisher = null;
                    }
                    if (msubscriber != null) {
                        mSession.unsubscribe(msubscriber);
                        msubscriber.destroy();
                        msubscriber = null;
                    }
                    mSession.disconnect();
                    finish();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        requestPermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userRef.removeEventListener(valueEventListener1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoChatActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};

        if (EasyPermissions.hasPermissions(this, perms)) {

            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscriberViewController = findViewById(R.id.subscriber_container);

            //initialize and connect the session
            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);
        } else {
            EasyPermissions.requestPermissions(this, "This app needs Mic and Camera, Please allow.", RC_VIDEO_APP_PERM, perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);
        mPublisherViewController.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Stream Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");
        if (msubscriber == null) {
            msubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(msubscriber);
            mSubscriberViewController.addView(msubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");
        if (msubscriber != null) {
            msubscriber = null;
            mSubscriberViewController.removeAllViews();
        }
        if (mPublisher != null) {
            mPublisher = null;
            mPublisherViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG, "Stream Error");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
