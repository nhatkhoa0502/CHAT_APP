package com.abc.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.chatapp.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.internal.TextWatcherAdapter;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterView extends AppCompatActivity {
    private static final String TAG = RegisterView.class.getSimpleName();
    private boolean passwordShowing = false;
    private EditText txtEmail, txtFullName, txtPassword, txtPasswordSubmit;
    private AppCompatButton btnRegister;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private TextView textViewSignIn;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_view);

        txtEmail = findViewById(R.id.txtEmail);
        txtFullName = findViewById(R.id.txtFullName);
        txtPassword = findViewById(R.id.txtPassword);
        txtPasswordSubmit = findViewById(R.id.txtPasswordSubmit);
        btnRegister = findViewById(R.id.signupEmailAndPasswordBtn);
        textViewSignIn = findViewById(R.id.signInBtnDirector);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Thiết lập sự kiện click cho nút đăng ký
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });

        // Thiết lập sự kiện thay đổi trường mật khẩu
        txtPasswordSubmit.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkPasswordsAndAdjustButton(txtPassword, txtPasswordSubmit, btnRegister);
            }
        });

        // Thiết lập sự kiện chạm để ẩn bàn phím
        setupUI(findViewById(R.id.rootLayout));
    }

    private void checkPasswordsAndAdjustButton(EditText txtPassword, EditText txtPasswordSubmit, AppCompatButton button) {
        if (!txtPassword.getText().toString().equals(txtPasswordSubmit.getText().toString())) {
            button.setEnabled(false);
            button.setAlpha(0.5f); // Make button appear blurred or faded
        } else {
            button.setEnabled(true);
            button.setAlpha(1.0f); // Restore button appearance
        }
    }

    private void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(RegisterView.this);
                    return false;
                }
            });
        }

        // If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    currentFocusedView.getWindowToken(), 0);
        }
    }

    private void createUser() {
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        String name = txtFullName.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            txtEmail.setError("Email cannot be empty");
            txtEmail.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            txtPassword.setError("Password cannot be empty");
            txtPassword.requestFocus();
        } else {
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String userID = task.getResult().getUser().getUid();
                                System.out.println("User ID: " + userID);
                                User user = new User(userID, name, "https://www.gstatic.com/mobilesdk/160503_mobilesdk/logo/2x/firebase_28dp.png", "offline", email);
                                addUserToDatabase(user);
                                Toast.makeText(RegisterView.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(RegisterView.this, "Failed to register user: " + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void addUserToDatabase(User user) {
        DatabaseReference userRef = database.child("users").child(user.getId());
        userRef.setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // User added to database successfully
                            Log.d(TAG, "User added to database");
                        } else {
                            // Failed to add user to database
                            Log.e(TAG, "Failed to add user to database: ", task.getException());
                        }
                    }
                });
    }
}