package com.example.chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatbot.databinding.ActivityLogOutBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LogOut extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ActivityLogOutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log_out);
        
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);

        binding = ActivityLogOutBinding.inflate(getLayoutInflater());
        firebaseAuth = FirebaseAuth.getInstance();

        String name = getIntent().getStringExtra("name");
        TextView user_Name = findViewById(R.id.user_Name);

        user_Name.setText(name);

        Button logOutBtn = findViewById(R.id.logOutBtn);

        logOutBtn.setBackgroundResource(R.drawable.button);

        logOutBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
        });
        finishAffinity();
    }
}