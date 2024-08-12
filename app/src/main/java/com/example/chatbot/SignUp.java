package com.example.chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import com.example.chatbot.MainActivity;
import com.example.chatbot.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUp extends ComponentActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        binding.already.setOnClickListener(v -> startActivity(new Intent(this, Login.class)));

        ProgressBar signUpProgressBar = findViewById(R.id.signUpProgressBar);

        binding.signupBtn.setOnClickListener(v -> {
            String name = binding.name.getText().toString();
            String phoneNumber = binding.phonenumber.getText().toString();
            String email = binding.email.getText().toString();
            String password = binding.passwordsignup0.getText().toString();
            String confPass = binding.confirmpasswordsignup0.getText().toString();

            if (!name.isEmpty() && !phoneNumber.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confPass.isEmpty()) {
                if (password.equals(confPass)) {

                    binding.signupBtn.setEnabled(false);
                    binding.signupBtn.setText("");
                    ProgressBar progressBar = findViewById(R.id.signUpProgressBar);
                    progressBar.setVisibility(View.VISIBLE);

                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                signUpProgressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    String userId = firebaseAuth.getCurrentUser().getUid();
                                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                                    HashMap<String, Object> userData = new HashMap<>();
                                    userData.put("name", name);
                                    userData.put("phoneNumber", phoneNumber);
                                    userData.put("email", email);
                                    usersRef.child(userId).setValue(userData);

                                    Intent intent = new Intent(this, MainActivity.class);
                                    intent.putExtra("name",name);
                                    startActivity(intent);
                                    finishAffinity();
                                    progressBar.setVisibility(View.INVISIBLE);

                                } else {
                                    Toast.makeText(this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(this, "Passwords are not matching", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
