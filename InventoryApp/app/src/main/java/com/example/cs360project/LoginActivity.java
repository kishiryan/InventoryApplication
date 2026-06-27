package com.example.cs360project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin, btnCreateAccount;
    DatabaseHelper db;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        username = getIntent().getStringExtra("USERNAME");

        // Define login field and buttons by ids
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // activity_login Button 1
        // Login functionality
        btnLogin.setOnClickListener(view -> { // on button click
            String username = etUsername.getText().toString().trim(); // clean username field
            String password = etPassword.getText().toString().trim(); // clean password field

            // functionality contingencies
            // if either field is empty
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show();
                return;
            }
            // if login pulls both username and password from db
            if(db.loginUser(username, password)) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
                finish();
            }
            // else (any other contingency)
            else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        // activity_login button 2
        // create account functionality
        btnCreateAccount.setOnClickListener(view -> {
            // variables set to username and password field strings
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // input contingencies
            // if either field is empty
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username or Password is missing from field", Toast.LENGTH_SHORT).show();
                return;
            }
            // if both fields are accepted
            if (db.registerUser(username, password)) {
                Toast.makeText(this, "Account creation success. Please log in.", Toast.LENGTH_SHORT).show();
            }
            // anything else
            else {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
