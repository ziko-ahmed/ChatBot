package com.example.chatbot;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import com.example.chatbot.MainActivity;
import com.example.chatbot.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends ComponentActivity {

    private FirebaseAuth firebaseAuth;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        firebaseAuth = FirebaseAuth.getInstance();
        setContentView(binding.getRoot());
        binding.register.setOnClickListener(v -> startActivity(new Intent(this, SignUp.class)));
        binding.loginBtn.setOnClickListener(v -> {
            String email = binding.emaillogin.getText().toString();
            String password = binding.passwordlogin0.getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) {

                binding.loginBtn.setEnabled(false);
                binding.loginBtn.setText("");
                ProgressBar progressBar = findViewById(R.id.loginProgressBar);
                progressBar.setVisibility(View.VISIBLE);

                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user != null) {
                                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                                    usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String name = dataSnapshot.child("name").getValue(String.class);
                                            if (name != null) {
                                                Intent intent = new Intent(Login.this, MainActivity.class);
                                                intent.putExtra("name", name);
                                                startActivity(intent);
                                                finishAffinity();
                                                progressBar.setVisibility(View.INVISIBLE);

                                            } else {
                                                Toast.makeText(Login.this, "Name not found", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Toast.makeText(Login.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(this, "INVALID", Toast.LENGTH_SHORT).show();
                                binding.loginBtn.setEnabled(true);
                                binding.loginBtn.setText("Login");
                            }
                        });
            } else {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
