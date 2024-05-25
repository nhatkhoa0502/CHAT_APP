package com.abc.myapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

public class MicRecorderActivity extends AppCompatActivity {
    private static final String TAG = MicRecorderActivity.class.getSimpleName();

    private ImageButton startRecord, doneRecord, deleteRecord;
    private TextView txt_timer;
    private MediaRecorder mediaRecorder;
    private String audioSavePath = null;

    private int seconds = 0;
    private Handler handler;
    private Runnable runnable;

    private Intent intent;
    private String senderId;
    private String receiverId;

    private StorageReference mStorageMp3Ref;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mic_recorder);

        startRecord = findViewById(R.id.startRecord);
        deleteRecord = findViewById(R.id.deleteRecord);
        doneRecord = findViewById(R.id.doneRecord);
        txt_timer = findViewById(R.id.txt_timer);

        deleteRecord.setEnabled(false);
        doneRecord.setEnabled(false);

        intent = getIntent();
        senderId = intent.getStringExtra("senderId");
        receiverId = intent.getStringExtra("receiverId");

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                seconds++;
                int minutes = seconds / 60;
                int remainingSeconds = seconds % 60;
                txt_timer.setText(String.format("%02d:%02d", minutes, remainingSeconds));
                handler.postDelayed(this, 1000);
            }
        };

        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions() == true) {
                    handler.postDelayed(runnable, 1000);

                    deleteRecord.setEnabled(true);
                    doneRecord.setEnabled(true);

                    startRecord.setImageResource(R.drawable.ic_pause_24);
                    startRecord.setEnabled(false);
                    startRecord.setOnClickListener(null);

                    // Lấy thư mục lưu trữ nội bộ của ứng dụng
                    File internalFilesDir = getFilesDir();

                    // Tạo đường dẫn lưu trữ file mp3 trong thư mục lưu trữ nội bộ
                    File audioFile = new File(internalFilesDir, "recordingAudio.mp3");
                    audioSavePath = audioFile.getAbsolutePath();

                    Log.d(TAG, audioSavePath);

                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    mediaRecorder.setOutputFile(audioSavePath);

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                        Toast.makeText(MicRecorderActivity.this, "Recording started", Toast.LENGTH_SHORT).show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    ActivityCompat.requestPermissions(MicRecorderActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                }

            }
        });

        deleteRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                mediaRecorder.stop();
                mediaRecorder.release();
                Toast.makeText(MicRecorderActivity.this, "Recording deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        doneRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                mediaRecorder.stop();
                mediaRecorder.release();

                // Khởi tạo FirebaseStorage
                mStorageMp3Ref = FirebaseStorage.getInstance().getReference("MessageMp3");

                // Khởi tạo ProgressDialog
                mProgressDialog = new ProgressDialog(v.getContext());
                mProgressDialog.setMessage("Sending...");
                mProgressDialog.setCancelable(false);

                // Đường dẫn đến tệp MP3 trong thiết bị của bạn
                Uri fileUri = Uri.fromFile(new File(audioSavePath));

                StorageReference fileRef = mStorageMp3Ref.child(System.currentTimeMillis() + ".mp3");

                // Thêm metadata để đặt loại MIME
                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType("audio/mpeg")
                        .build();

                // Hiển thị ProgressDialog
                mProgressDialog.show();

                // Tải lên tệp MP3 lên Firebase Storage
                fileRef.putFile(fileUri, metadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url = uri.toString();
                                long duration = getAudioDuration(fileUri);
                                sendMessage(url, duration);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Xử lý nếu không thể lấy đường dẫn URL
                                Toast.makeText(MicRecorderActivity.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Ẩn ProgressDialog khi tải lên thành công
                        mProgressDialog.dismiss();
                        Toast.makeText(MicRecorderActivity.this, "Recording done", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Ẩn ProgressDialog khi tải lên thất bại
                        mProgressDialog.dismiss();
                        Toast.makeText(MicRecorderActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });


            }
        });

    }

    private void sendMessage(String url, long duration) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", senderId);
        hashMap.put("receiver", receiverId);
        hashMap.put("message", url);
        hashMap.put("duration", duration);
        hashMap.put("time", new Date().getTime());
        hashMap.put("isseen", false);
        reference.child("chats").push().setValue(hashMap);
    }

    public static long getAudioDuration(Uri audioUri) {
        long duration = 0;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(audioUri.getPath());
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            duration = Long.parseLong(durationStr);
            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return duration;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private boolean checkPermissions() {
        int first = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        return first == PackageManager.PERMISSION_GRANTED;
    }
}