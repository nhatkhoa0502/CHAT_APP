package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ForgetPasswordView extends AppCompatActivity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password_view);
        setupUI(findViewById(R.id.rootLayout));
        final EditText txtPhoneAndEmail =  findViewById(R.id.txtPhoneAndEmail);
        final RelativeLayout wrapTxtCode = findViewById(R.id.wrapTxtCode);
        final EditText txtCode = findViewById(R.id.txtCode);
        final AppCompatButton changePasswordBtn = findViewById(R.id.changePasswordBtn);
        final RelativeLayout signInWithEmailAndPassword = findViewById(R.id.signInWithEmailAndPassword);
}
