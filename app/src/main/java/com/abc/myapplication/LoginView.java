 package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

 public class LoginView extends AppCompatActivity {
    private boolean passwordShowing= false;
     private Button btn_loginWithGG;
     private FirebaseAuth auth;
     private FirebaseDatabase database;
     private GoogleSignInClient mGoogleSignInClient;
     private int RC_SIGN_IN = 20;
     private FirebaseUser firebaseUser;
     EditText txtEmail,txtPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_view);

        final EditText txtEmail = findViewById(R.id.txtEmail);
        final EditText txtPassword = findViewById(R.id.txtPassword);
        final TextView signUpBtn = findViewById(R.id.signUpBtnDirector);
        final ImageView passwordIcon = findViewById(R.id.showHideBtn);
        final RelativeLayout signInWithGoogleBtn = findViewById(R.id.signInWithGoogleBtn);
        final AppCompatButton signInEmailAndPasswordBtn = findViewById(R.id.signInEmailAndPasswordBtn);
        final RelativeLayout signInByPhoneBtn = findViewById(R.id.signInByPhone);
        final TextView forgetPassword = findViewById(R.id.forgetPassword);
        auth = FirebaseAuth.getInstance();
        setupUI(findViewById(R.id.rootLayout));
        signInEmailAndPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validateUsername()| !validatePassword()){

                }else{
                    checkUser();
                }
            }
        });
        signInByPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginView.this, LoginByPhoneView.class));
            }
        });
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginView.this, ForgetPasswordView.class));
            }
        });
        passwordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(passwordShowing){
                    passwordShowing=false;
                    txtPassword.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                    passwordIcon.setImageResource(R.drawable.show_pass_icon);
                }else{
                    passwordShowing=true;
                    txtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordIcon.setImageResource(R.drawable.hide_password_icon);
                }
                txtPassword.setSelection(txtPassword.length());
            }
        });
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFieldsForEmptyValues(txtEmail, txtPassword, signInEmailAndPasswordBtn);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        txtEmail.addTextChangedListener(textWatcher);
        txtPassword.addTextChangedListener(textWatcher);

        // Initial check to set initial state of the button
        checkFieldsForEmptyValues(txtEmail, txtPassword, signInEmailAndPasswordBtn);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            Intent intent = new Intent(LoginView.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInWithGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginView.this,RegisterView.class));
            }
        });
    }


     /**
      * Validate fields
      */


      public Boolean validatePassword(){
        String val = txtPassword.getText().toString();
        if(val.isEmpty()){
            txtPassword.setError("Username cannot be empty");
            return false;
        }else{
            txtPassword.setError(null);
            return true;
        }
    }


     public Boolean validateUsername(){
         String val = txtEmail.getText().toString();
         if(val.isEmpty()){
             txtEmail.setError("Username cannot be empty");
             return false;
         }else{
             txtEmail.setError(null);
             return true;
         }
     }

   
     /**
      * Login by Google
      */
     private void googleSignIn() {
         Intent intent = mGoogleSignInClient.getSignInIntent();
         startActivityForResult(intent, RC_SIGN_IN);
     }
     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == RC_SIGN_IN) {
             Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
             try {
                 GoogleSignInAccount account = task.getResult(ApiException.class);
                 fireBaseAuth(account.getIdToken());
             } catch (ApiException e) {
                 // Handle ApiException here
                 String errorMessage = "Error: " + e.getStatusCode(); // Example of getting error code
                 Log.e("GoogleSignIn", "Google sign-in failed with error: " + errorMessage, e); // Log the exception
                 Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
             } catch (Exception e) {
                 // Handle other exceptions here
                 Log.e("GoogleSignIn", "Google sign-in failed with unknown error", e); // Log the exception
                 Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
             }
         }
     }

     private void fireBaseAuth(String idToken) {
         AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
         auth.signInWithCredential(credential)
                 .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                     @Override
                     public void onComplete(@NonNull Task<AuthResult> task) {
                         if (task.isSuccessful()) {
                             FirebaseUser user = auth.getCurrentUser();
                             // kiểm tra xem user có tồn tại trong db hay chưa, nếu chưa thì lưu thông tin user lại
                             database.getReference().child("users").child(user.getUid()).get()
                                     .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                         @Override
                                         public void onSuccess(DataSnapshot dataSnapshot) {
                                             if (!dataSnapshot.exists()) {
                                                 HashMap<String, Object> map = new HashMap<>();
                                                 map.put("id", user.getUid());
                                                 map.put("name", user.getDisplayName());
                                                 map.put("profile", user.getPhotoUrl().toString());
                                                 map.put("email", user.getEmail());
                                                 map.put("status", "offline");
                                                 database.getReference().child("users").child(user.getUid()).setValue(map);
                                             }
                                             Intent intent = new Intent(LoginView.this, MainActivity.class);
                                             startActivity(intent);
                                             finish();
                                         }
                                     }).addOnFailureListener(new OnFailureListener() {
                                         @Override
                                         public void onFailure(@NonNull Exception e) {
                                             Toast.makeText(LoginView.this, "Đăng nhập thất bại!!!", Toast.LENGTH_SHORT).show();
                                         }
                                     });
                         } else {
                             Toast.makeText(LoginView.this, "Fail", Toast.LENGTH_SHORT).show();
                         }
                     }
                 });
     }
     /**
      * Handle hide keyboard
      */
     private void setupUI(View view) {
         // Set up touch listener for non-text box views to hide keyboard.
         if (!(view instanceof EditText)) {
             view.setOnTouchListener(new View.OnTouchListener() {
                 public boolean onTouch(View v, MotionEvent event) {
                     hideSoftKeyboard(LoginView.this);
                     return false;
                 }
             });
         }

         //If a layout container, iterate over children and seed recursion
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
     /**
      * Disables the sign-in button if either email or password fields are empty
      */
     private void checkFieldsForEmptyValues(EditText txtEmail, EditText txtPassword, Button signInButton) {
         String email = txtEmail.getText().toString();
         String password = txtPassword.getText().toString();

         if (email.isEmpty() || password.isEmpty()) {
             signInButton.setEnabled(false);
             signInButton.setAlpha(0.5f);  // Set the button to look "disabled"
         } else {
             signInButton.setEnabled(true);
             signInButton.setAlpha(1.0f);  // Set the button to look "enabled"
         }
     }




     
     /**
      * Login by email and password
      */
         public void checkUser(){
             String userUsername= txtEmail.getText().toString().trim();
             String userPassword= txtPassword.getText().toString().trim();

             if(TextUtils.isEmpty(userUsername)){
                 txtEmail.setError("Email cannot be empty");
                 txtEmail.requestFocus();
             }else if(TextUtils.isEmpty(userPassword)){
                 txtEmail.setError("Password cannot be empty");
                 txtPassword.requestFocus();
             }else{
                 auth.signInWithEmailAndPassword(userUsername,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                     @Override
                     public void onComplete(@NonNull Task<AuthResult> task) {
                         if(task.isSuccessful()){
                             Toast.makeText(LoginView.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                             startActivity(new Intent(LoginView.this,MainActivity.class));
                         }else{
                             Toast.makeText(LoginView.this, "Login Error", Toast.LENGTH_SHORT).show();
                         }
                     }
                 });
             }
         }
 }
