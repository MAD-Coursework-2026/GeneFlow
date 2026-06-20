package com.geneflow.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.geneflow.app.db.DatabaseHelper;
import com.geneflow.app.model.User;
import com.geneflow.app.util.Session;

/** Screen 2 - sign up for GeneFlow. */
public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        TextView txtLogin = findViewById(R.id.txtLogin);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirm = findViewById(R.id.etConfirmPassword);
        Button btnCreate = findViewById(R.id.btnCreateAccount);

        txtLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnCreate.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString();
            String confirm = etConfirm.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username)
                    || TextUtils.isEmpty(pass) || TextUtils.isEmpty(confirm)) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHelper db = DatabaseHelper.get(this);
            if (db.emailExists(email)) {
                Toast.makeText(this, "An account with this email already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            User u = new User(username, email, pass);
            long id = db.insertUser(u);
            if (id > 0) {
                new Session(this).setCurrentEmail(email);
                Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Could not create account, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
