package com.example.hootuser;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UserActivity extends AppCompatActivity {

    private EditText etUserName;
    private TextView tvGameCode;
    private Button buttonAllGood;
    private String gameCode, playerName, emoji, hexColor;

    private DatabaseReference mDatabase;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);

        Intent intent = getIntent();
        this.gameCode = intent.getStringExtra("gameCode");

        // Reference to the players inside the specific game
        DatabaseReference playersRef = database.getReference("Games/" + "game_" + gameCode + "/players");

        etUserName = findViewById(R.id.etUser);
        tvGameCode = findViewById(R.id.gameModeCode);
        buttonAllGood = findViewById(R.id.joinButton);
        tvGameCode.setText(gameCode);

        // Initialize Firebase Database reference

        buttonAllGood.setOnClickListener(v -> {
            this.playerName = etUserName.getText().toString().trim();

            if (playerName.isEmpty()) {
                Toast.makeText(this, "Please enter a game code", Toast.LENGTH_SHORT).show();
            } else {
                generateRandomEmoji();
                generateRandomHexColor();
                addPlayerTransaction(playersRef);
            }
        });
    }

    private void addPlayerTransaction(DatabaseReference playersRef) {
        playersRef.runTransaction(new Transaction.Handler() {


            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                long playerCount = mutableData.getChildrenCount(); // Get current number of players
                long newPlayerNumber = playerCount + 1; // Assign new player number

                // Create a player entry
                Map<String, Object> playerData = new HashMap<>();
                playerData.put("username", playerName);
                playerData.put("image_color", hexColor);
                playerData.put("image_emoji", emoji); // Add emoji

                // Save player inside the node
                mutableData.child(String.valueOf("player_" + newPlayerNumber)).setValue(playerData);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    Log.d("Firebase", "Player added successfully.");
                    intentToWaitingActivity();
                } else {
                    Log.e("Firebase", "Transaction failed: " + databaseError.getMessage());
                }
            }
        });
    }

    private void intentToWaitingActivity() {
        Intent intent = new Intent(this, WaitingActivity.class);
        intent.putExtra("playerName", playerName);
        intent.putExtra("gameCode", gameCode);
        intent.putExtra("emoji", emoji);
        intent.putExtra("hexColor", hexColor);
        startActivity(intent);
    }

    public void generateRandomEmoji() {
        String[] emojis = getResources().getStringArray(R.array.emoji_list);
        this.emoji = emojis[new Random().nextInt(emojis.length)];
    }

    public void generateRandomHexColor() {
        // Generate random values for red, green, and blue (0 to 255)
        int red = (int) (Math.random() * 256);  // Random value between 0 and 255
        int green = (int) (Math.random() * 256);  // Random value between 0 and 255
        int blue = (int) (Math.random() * 256);  // Random value between 0 and 255

        this.hexColor = String.format("#%02X%02X%02X", red, green, blue);
    }
}