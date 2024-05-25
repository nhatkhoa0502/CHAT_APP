package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.text.BreakIterator;

public class ForgetPasswordView extends AppCompatActivity {
    private static final String TAG = ForgetPasswordView.class.getSimpleName();
    TextView resendOTP;
    private static final long TIME_WAIT = 60000;
    private static final long TIME_COUNTDOWN = 1000;

    private FirebaseAuth firebaseAuth;
    EditText txtPhoneAndEmail;
    private TextView txtEmail, textViewSignIn;
    private  AppCompatButton btn_forgetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password_view);
        setupUI(findViewById(R.id.rootLayout));

        firebaseAuth = FirebaseAuth.getInstance();

        txtEmail = findViewById(R.id.txtEmail);
        textViewSignIn = findViewById(R.id.signInBtnDirector);
        btn_forgetPassword = findViewById(R.id.btn_forgetPassword);


        final RelativeLayout wrapTxtCode = findViewById(R.id.wrapTxtCode);
        resendOTP = findViewById(R.id.resendOTP);
        final EditText txtCode = findViewById(R.id.txtCode);

//        final RelativeLayout signInWithEmailAndPassword = findViewById(R.id.signInWithEmailAndPassword);

//        signInWithEmailAndPassword.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(ForgetPasswordView.this, LoginView.class));
//            }
//        });

        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String emailAddress = txtEmail.getText().toString().trim();
                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ForgetPasswordView.this, "Email sent. Please check email to reset password!!!", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Email sent.");
                                    finish();
                                } else {
                                    Log.d(TAG, "Email sent fail" + task.getException());
                                    Toast.makeText(ForgetPasswordView.this, "Email sent fail: "+task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        txtCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
//                // Kiểm tra độ dài của chuỗi trong resendOTP
//                if (s.length() == 6) {
//                    changePasswordBtn.setEnabled(true);
//                    changePasswordBtn.setAlpha(1.0f);
//                } else {
//                    // Nếu không đủ 6 ký tự, vô hiệu hóa và làm mờ nút changePasswordBtn
//                    changePasswordBtn.setEnabled(false);
//                    changePasswordBtn.setAlpha(0.5f);
//                }
            }
        });

//        txtPhoneAndEmail.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                // Không cần làm gì ở đây
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                Log.d("Debug", "Current text length: " + s);
//                if (s.length() == 10) {
//                    startCountdown();
//                    wrapTxtCode.setVisibility(View.VISIBLE);
//                    resendOTP.setVisibility(View.VISIBLE);
//                    Log.d("Debug", "txtCode is now visible");
//                    // Xử lý gửi OTP ở dưới đây
//                    sendOTPToUser(s.toString());
//                } else {
//                    wrapTxtCode.setVisibility(View.GONE);
//                    resendOTP.setVisibility(View.GONE);
//                    Log.d("Debug", "txtCode is now gone");
//                }
//            }
//        });

//        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String verificationCode = txtCode.getText().toString().trim();
//                if (verificationCode.length() == 6) {
//                    resetPasswordWithVerificationCode(verificationCode);
//                } else {
//                    Toast.makeText(ForgetPasswordView.this, "Invalid verification code", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        resendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneOrEmail = txtPhoneAndEmail.getText().toString().trim();
                sendOTPToUser(phoneOrEmail);
            }
        });
    }

    private void startCountdown() {
        resendOTP.setEnabled(false);  // Disable the resend button initially
        new CountDownTimer(TIME_WAIT, TIME_COUNTDOWN) {

            public void onTick(long millisUntilFinished) {
                resendOTP.setText("Please wait: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                resendOTP.setText("Resend OTP");
                resendOTP.setEnabled(true);  //Enable the resend button
            }
        }.start();
    }

    private void sendOTPToUser(String phoneOrEmail) {
        firebaseAuth.sendPasswordResetEmail(phoneOrEmail)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgetPasswordView.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ForgetPasswordView.this, "Failed to send OTP", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetPasswordWithVerificationCode(String verificationCode) {
        String phoneOrEmail = txtPhoneAndEmail.getText().toString().trim();
        firebaseAuth.verifyPasswordResetCode(verificationCode)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String newPassword = "new_password_here"; // Thay đổi thành mật khẩu mới của người dùng
                        firebaseAuth.confirmPasswordReset(verificationCode, newPassword)
                                .addOnCompleteListener(this, resetTask -> {
                                    if (resetTask.isSuccessful()) {
                                        Toast.makeText(ForgetPasswordView.this, "Password reset successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ForgetPasswordView.this, LoginView.class));
                                    } else {
                                        Toast.makeText(ForgetPasswordView.this, "Failed to reset password", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        String errorMessage;
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException invalidUser) {
                            errorMessage = "Invalid email address";
                        } catch (FirebaseAuthInvalidCredentialsException invalidCredentials) {
                            errorMessage = "Invalid verification code";
                        } catch (Exception e) {
                            errorMessage = "Failed to verify password reset code";
                            e.printStackTrace();
                        }
                        Toast.makeText(ForgetPasswordView.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(ForgetPasswordView.this);
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
}