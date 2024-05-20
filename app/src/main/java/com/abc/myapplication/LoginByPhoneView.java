package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class LoginByPhoneView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_by_phone_view);
        setupUI(findViewById(R.id.rootLayout));
        final EditText txtPhone = findViewById(R.id.txtPhone);
        final EditText txtCode = findViewById(R.id.txtCode);
        final RelativeLayout wrapTxtCode = findViewById(R.id.wrapTxtCode);
        final RelativeLayout signInWithEmailAndPassword = findViewById(R.id.signInWithEmailAndPassword);
        signInWithEmailAndPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginByPhoneView.this,LoginView.class));
            }
        });
        txtPhone.addTextChangedListener(new TextWatcher() {
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
                    wrapTxtCode.setVisibility(View.VISIBLE);
                    Log.d("Debug", "txtCode is now visible");
                } else {
                    wrapTxtCode.setVisibility(View.GONE);
                    Log.d("Debug", "txtCode is now gone");
                }
            }
        });
    }
//    handle UI/UX
private void setupUI(View view) {
    // Set up touch listener for non-text box views to hide keyboard.
    if (!(view instanceof EditText)) {
        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(LoginByPhoneView.this);
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