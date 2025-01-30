package com.example.hootuser;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private EditText editTextGameCode;
    private Button buttonSearch;

    private DatabaseReference mDatabase;
    FirebaseDatabase database = FirebaseDatabase.getInstance();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        editTextGameCode = findViewById(R.id.gameModeCode);
        buttonSearch = findViewById(R.id.joinButton);

        // Initialize Firebase Database reference

        buttonSearch.setOnClickListener(v -> {
            String gameCode = editTextGameCode.getText().toString().trim();

            if (gameCode.isEmpty()) {
                Toast.makeText(this, "Please enter a game code", Toast.LENGTH_SHORT).show();
            } else {
                isGameActive(gameCode);
            }
        });
    }


    // Method to check if a game's status is active
    public void isGameActive(String gameCode) {
        DatabaseReference myRef = database.getReference("Games/" + "game_"+gameCode + "/status");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.getValue(String.class);  // Retrieve the status value
                    if (status != null && status.equals("waiting")) {
                        Toast.makeText(getApplicationContext(), "Game found", Toast.LENGTH_SHORT).show();
                        startUserActivity(gameCode);
                    } else {
                        Toast.makeText(getApplicationContext(), "Game not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Game don't exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override

            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.e("MainActivityLogin", "Error: " + databaseError.getMessage());

            }

        });
    }

    //

    private void startUserActivity(String gameCode) {
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("gameCode", gameCode);
        startActivity(intent);            }
}