package com.abc.myapplication.Notification;

import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private String mSenderName, mMessage;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        updateToken();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        mSenderName = message.getData().get("senderName");
        mMessage = message.getData().get("message");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        // Tạo kênh thông báo nếu chưa có
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getString(R.string.channel_id); // Id kênh thông báo
            CharSequence name = getString(R.string.channel_name); // Tên kênh thông báo
            String description = getString(R.string.channel_description); // Mô tả kênh thông báo
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);

            // Tạo và hiển thị thông báo
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                    .setSmallIcon(R.drawable.ic_chat_app_24)
                    .setContentTitle("New Message")
                    .setContentText(mSenderName + ": " + mMessage)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            notificationManager.notify(0, builder.build());
        }
    }

    private void updateToken() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("tokens");
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

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
    }
}



