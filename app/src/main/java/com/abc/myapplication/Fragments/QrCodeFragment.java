package com.example.chatapp.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.CaptureAct;
import com.example.chatapp.MessageActivity;
import com.example.chatapp.Model.User;
import com.example.chatapp.Notification.APIService;
import com.example.chatapp.Notification.Client;
import com.example.chatapp.Notification.Data;
import com.example.chatapp.Notification.MyResponse;
import com.example.chatapp.Notification.Sender;
import com.example.chatapp.R;
import com.example.chatapp.StartActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Date;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrCodeFragment extends Fragment {
    private String TAG = QrCodeFragment.this.getClass().getSimpleName();
    private ImageView imageView;
    private Button btn_scan;
    private TextView txt_username;
    private String userId;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_code, container, false);

        imageView = view.findViewById(R.id.qr_code);
        btn_scan = view.findViewById(R.id.btn_scan);
        txt_username = view.findViewById(R.id.txt_username);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = firebaseUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference();


        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            Task<DataSnapshot> task = databaseReference.child("users").child(userId).get();
            task.addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        User user = dataSnapshot.getValue(User.class);
                        txt_username.setText(user.getName());
                    } else {
                        Log.e(TAG, "Lỗi load tên user lên QR Code");
                    }
                }
            });
            BitMatrix bitMatrix = multiFormatWriter.encode(userId, BarcodeFormat.QR_CODE, 300, 300);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
            }
        });

        return view;
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Point the camera towards the QR code.");
        options.setBeepEnabled(true); //âm thanh (beep) được phát ra khi quét thành công mã QR
        options.setOrientationLocked(true); //Khóa hướng màn hình để màn hình quét mã không xoay khi người dùng quay điện thoại.
        options.setCaptureActivity(CaptureAct.class); // CaptureAct để chỉnh giao diện hoặc chức năng của màn hình quét mã
        barLaucher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result ->
    {
        String userIdFromQRCode = result.getContents();
        if (userIdFromQRCode != null && !userIdFromQRCode.equals(userId)) {
            Task<DataSnapshot> task = databaseReference.child("users").get();
            task.addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        boolean isValidQrCode = false;
                        DataSnapshot dataSnapshot = task.getResult();
                        for (DataSnapshot sn : dataSnapshot.getChildren()) {
                            User user = sn.getValue(User.class);
                            if (user.getId().equals(userIdFromQRCode)) {
                                isValidQrCode = true;
                                sendFriendRequest(userIdFromQRCode);
                                Toast.makeText(getContext(), "Request Friend Success!!!", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                        if (!isValidQrCode) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Error");
                            builder.setMessage("User not found or invalid QR Code!!!");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                        }
                    } else {
                        Log.e(TAG, "Lỗi QR Code");
                    }
                }
            });
        }
    });

    private void sendFriendRequest(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", "pending");
        reference.child("requests").child(userId).child(firebaseUser.getUid()).setValue(hashMap);
    }


}