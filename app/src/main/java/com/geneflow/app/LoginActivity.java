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
import com.geneflow.app.util.Session;

/** Screen 3 - sign in to GeneFlow. */
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnSignIn = findViewById(R.id.btnSignIn);
        TextView txtForgot = findViewById(R.id.txtForgot);
        TextView txtCreateAccount = findViewById(R.id.txtCreateAccount);

        btnSignIn.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean ok = DatabaseHelper.get(this).validateLogin(email, pass);
            if (ok) {
                new Session(this).setCurrentEmail(email);
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        });

        txtForgot.setOnClickListener(v ->
                Toast.makeText(this, "Password reset is not part of this prototype", Toast.LENGTH_SHORT).show());

        txtCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));
    }
}
