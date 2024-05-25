package com.example.chatapp.Fragments;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp.ListFriendRequestActivity;
import com.example.chatapp.LoginView;
import com.example.chatapp.MainActivity;
import com.example.chatapp.MessageActivity;
import com.example.chatapp.Model.User;
import com.example.chatapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private static final String TAG = ProfileFragment.class.getSimpleName();
    CircleImageView profile_image;
    TextView username;
    private String currentUserId;
    ImageView img_edit_profile_image;
    DatabaseReference reference;
    FirebaseUser firebaseUser;
    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;
    private ValueEventListener valueEventListener1;
    private RelativeLayout logout, list_friend_request;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profile_image = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);
        img_edit_profile_image = view.findViewById(R.id.img_edit_profile_image);
        logout = view.findViewById(R.id.logout);
        list_friend_request = view.findViewById(R.id.list_friend_request);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = firebaseUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference("AvatarUsers");

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStatus("offline"); // Set user status to offline
                performLogout();
            }
        });

        list_friend_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ListFriendRequestActivity.class));
            }
        });

        valueEventListener1 = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getName());
                if (getActivity() != null) {
                    Glide.with(getActivity()).load(user.getProfile()).into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        img_edit_profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getContext(), "Upload in progress!!!", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        reference.removeEventListener(valueEventListener1);
        Log.d(TAG, "onDestroy");
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("profile", mUri);
                        reference.updateChildren(map);

                        pd.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(getContext(), "No image selected!!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setStatus(String status) {
        // Assuming 'currentUserId' is already defined and 'reference' is initialized
        reference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Log.e("setStatus", "Failed to update user status");
                }
            }
        });
    }

    private void performLogout() {
//        Context context = getActivity();
            Context context = requireContext();



        if (context == null){
            return;
        }

        FirebaseAuth.getInstance().signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignIn.getClient(context, gso).signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Successfully logged out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, LoginView.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(context, "Failed to log out", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}