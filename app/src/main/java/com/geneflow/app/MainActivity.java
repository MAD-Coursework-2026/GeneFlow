package com.geneflow.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.geneflow.app.util.SeedData;

/** Screen 1 - landing screen with Sign in / Create Account. */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Seed demo data once so the dashboard is populated on first run.
        SeedData.seedIfNeeded(getApplicationContext());

        TextView btnSignIn = findViewById(R.id.btnSignIn);
        TextView btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnSignIn.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        btnCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));
    }
}
