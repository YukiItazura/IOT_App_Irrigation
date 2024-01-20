package com.example.dam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText loginUsername, loginPassword;
    Button loginButton;
    TextView signupRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginUsername = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateUsername() | !validatePassword()) {

                } else {
                    checkUser();
                }
            }
        });

        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    public Boolean validateUsername(){
        String val = loginUsername.getText().toString();
        if (val.isEmpty()) {
            loginUsername.setError("Please fill in the Username.");
            return false;
        } else{
            loginUsername.setError(null);
            return true;
        }
    }

    public Boolean validatePassword(){
        String val = loginPassword.getText().toString();
        if (val.isEmpty()) {
            loginPassword.setError("Please fill in the Password.");
            return false;
        } else{
            loginPassword.setError(null);
            return true;
        }
    }

    public void checkUser(){
        String userUsername = loginUsername.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    loginUsername.setError(null);
                    String passwordFromDB = snapshot.child(userUsername).child("password").getValue(String.class);

                    Log.d("LoginActivity", "Snapshot: " + snapshot.toString());
                    Log.d("LoginActivity", "Password from DB: " + passwordFromDB);

                    if (passwordFromDB != null && passwordFromDB.equals(userPassword)) {
                        loginUsername.setError(null);
                        Intent intent = new Intent(LoginActivity.this, LandingPage.class);
                        intent.putExtra("USERNAME", userUsername);
                        startActivity(intent);
                    } else {
                        if (passwordFromDB == null) {
                            // Handle the case where the password is not found in the database.
                            Log.d("LoginActivity", "Password not found in the database.");
                            loginPassword.setError("Password not found.");
                        } else {
                            // Handle the case where the entered password is incorrect.
                            Log.d("LoginActivity", "Entered password is incorrect.");
                            loginPassword.setError("Wrong Password");
                        }
                        loginPassword.requestFocus();
                    }
                } else {
                    loginUsername.setError("User does not exist.");
                    loginUsername.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}