package com.example.chatapp;

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

public class ForgetPasswordView extends AppCompatActivity {
    TextView resendOTP;
    private static final long TIME_WAIT = 60000;
    private static final long TIME_COUNTDOWN = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password_view);
        setupUI(findViewById(R.id.rootLayout));
        final EditText txtPhoneAndEmail =  findViewById(R.id.txtPhoneAndEmail);
        final RelativeLayout wrapTxtCode = findViewById(R.id.wrapTxtCode);
        resendOTP = findViewById(R.id.resendOTP);
        final EditText txtCode = findViewById(R.id.txtCode);
        final AppCompatButton changePasswordBtn = findViewById(R.id.changePasswordBtn);
        final RelativeLayout signInWithEmailAndPassword = findViewById(R.id.signInWithEmailAndPassword);
        signInWithEmailAndPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgetPasswordView.this, LoginView.class));
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
                // Kiểm tra độ dài của chuỗi trong resendOTP
                if (  s.length()==6) {
                    changePasswordBtn.setEnabled(true);
                    changePasswordBtn.setAlpha(1.0f);
                } else {
                    // Nếu không đủ 6 ký tự, vô hiệu hóa và làm mờ nút changePasswordBtn
                    changePasswordBtn.setEnabled(false);
                    changePasswordBtn.setAlpha(0.5f);
                }
            }
        });
        txtPhoneAndEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Không cần làm gì ở đây
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("Debug", "Current text length: " + s);
                if (s.length() == 10) {
                    startCountdown();
                    wrapTxtCode.setVisibility(View.VISIBLE);
                    resendOTP.setVisibility(View.VISIBLE);
                    Log.d("Debug", "txtCode is now visible");
                    // xử lý gửi OTP ở dưới đây
                } else {
                    wrapTxtCode.setVisibility(View.GONE);
                    resendOTP.setVisibility(View.GONE);
                    Log.d("Debug", "txtCode is now gone");
                }
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
                resendOTP.setEnabled(true);  // Enable the resend button
            }
        }.start();
    }
    //    handle UI/UX
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

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    currentFocusedView.getWindowToken(), 0);
        }
    }
}
