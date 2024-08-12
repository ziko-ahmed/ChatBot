package com.example.chatbot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbot.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_NAME = "name";
    TextView userName;
    RecyclerView recyclerView;
    EditText editText;
    ImageButton sendButton;
    List<Message> messageList;
    MessageAdapter messageAdapter;

    private RelativeLayout rootLayout;

    public static final MediaType JSON = MediaType.get("application/json");

    OkHttpClient client = new OkHttpClient();

    private static final int BACK_PRESS_INTERVAL = 2000;
    private long backPressedTime;

    @Override
    public void onBackPressed() {
        if (backPressedTime + BACK_PRESS_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            finishAffinity();
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        // Save the state when the app is exited
//        saveState();
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        // Restore the state when the app starts
//        restoreState();
//    }
//
//    private void saveName(String name) {
//        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(KEY_NAME, name);
//        editor.apply();
//    }
//
//    private String getName() {
//        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
//        return sharedPreferences.getString(KEY_NAME, null);
//    }
//
//    private void saveState() {
//        // Save any other necessary state here
//    }
//
//    private void restoreState() {
//        // Restore any other necessary state here
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        String name = getIntent().getStringExtra("name");
//        saveName(name);

//        String savedName = getName();
//        if (savedName != null) {
//            userName.setText(savedName);
//        }

        LinearLayout app_name_logOut = findViewById(R.id.app_name_logOut);

        app_name_logOut.setOnClickListener(v -> {
            Intent intent = new Intent(this, LogOut.class);
            intent.putExtra("name",name);
            startActivity(intent);
        });

        rootLayout = findViewById(R.id.root_layout);

        rootLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                int systemWindowInsetBottom = insets.getSystemWindowInsetBottom();
                int systemWindowInsetTop = insets.getSystemWindowInsetTop();

                rootLayout.setPadding(rootLayout.getPaddingLeft(), systemWindowInsetTop,
                        rootLayout.getPaddingRight(), systemWindowInsetBottom);

                return insets;
            }
        });

        messageList = new ArrayList<>();

            userName = findViewById(R.id.user_name);
            userName.setText(name);
            recyclerView = findViewById(R.id.recycler_view);
            editText = findViewById(R.id.message_edit_text);
            sendButton = findViewById(R.id.send_btn);

            messageAdapter = new MessageAdapter(messageList);
            recyclerView.setAdapter(messageAdapter);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setStackFromEnd(true);
            recyclerView.setLayoutManager(llm);

            sendButton.setOnClickListener((v) -> {
                String question = editText.getText().toString().trim();
                addToChat(question,Message.SENT_BY_ME);
                editText.setText("");
                callAPI(question);
            });
    }
    void addToChat(String message, String sentBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message,sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat(response, Message.SENT_BY_BOT);
    }

    void callAPI(String question){
        messageList.add(new Message("Typing...", Message.SENT_BY_BOT));

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo");
            JSONArray messagesArray = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", question); // User input as prompt
            messagesArray.put(userMessage);

            jsonBody.put("messages", messagesArray);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        RequestBody body = RequestBody.create(jsonBody.toString(),JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization","Bearer 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")
                .post(body)
                .build();

        System.out.println(request.body());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to get response due to "+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");

                        JSONObject choiceObject = jsonArray.getJSONObject(0);
                            JSONObject messageObject = choiceObject.getJSONObject("message");
                            String result = messageObject.getString("content");
                            addResponse(result.trim());

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    addResponse("Failed to get response due to " + response.body());
                }
            }

        });
    }
}