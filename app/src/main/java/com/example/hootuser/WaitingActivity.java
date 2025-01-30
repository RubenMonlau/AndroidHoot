package com.example.hootuser;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ChildEventListener;

import java.util.ArrayList;
import java.util.List;

public class WaitingActivity extends AppCompatActivity {

    private RecyclerView playersRecyclerView;
    private TextView tvGameCode, etUserName, emojiUser;
    private Button buttonAllGood;
    private String gameCode, playerName, emoji, hexColor;
    private List<Player> playersList;
    private PlayerAdapter playerAdapter;

    private DatabaseReference mDatabase;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_waiting);

        Intent intent = getIntent();
        this.playerName = intent.getStringExtra("playerName");
        this.gameCode = intent.getStringExtra("gameCode");
        this.emoji = intent.getStringExtra("emoji");
        this.hexColor = intent.getStringExtra("hexColor");

        playersRecyclerView = findViewById(R.id.playersRecyclerView);
        playersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        playersList = new ArrayList<>();
        playerAdapter = new PlayerAdapter(playersList);
        playersRecyclerView.setAdapter(playerAdapter);

        // Reference to the game status in Firebase
        DatabaseReference gameStatusRef = database.getReference("Games/" + "game_" + gameCode + "/status");

        // Reference to the players node in Firebase
        DatabaseReference playersRef = database.getReference("Games/" + "game_" + gameCode + "/players");

        etUserName = findViewById(R.id.tvPlayerName);
        tvGameCode = findViewById(R.id.gameCodeTextView);
        emojiUser = findViewById(R.id.emojiUser);
        tvGameCode.setText(gameCode);
        etUserName.setText(playerName);
        emojiUser.setText(emoji);

        Drawable background = emojiUser.getBackground();
        if (background != null) {
            background.setTint(Color.parseColor(hexColor));
            emojiUser.setBackground(background);
        }

        // Set up a listener to detect when the status is set to "playing"
        gameStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String status = dataSnapshot.getValue(String.class);
                if ("playing".equals(status)) {
                    // When status is active, move to the next activity
                    startNextActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });

        // Set up a listener for new players added to the game
        playersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Player player = dataSnapshot.getValue(Player.class);
                playersList.clear();  // Limpiar la lista antes de agregar los nuevos jugadores

                if (player != null) {
                    playersList.add(player);  // Add the player without clearing the list
                    playerAdapter.notifyDataSetChanged();  // Notify the adapter to update the UI
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                // Handle changes in existing players, if needed
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Handle player removed, if needed
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // Handle players being moved, if needed
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    // Function to navigate to the next activity
    private void startNextActivity() {
        Intent intent = new Intent(WaitingActivity.this, GameActivity.class);
        startActivity(intent);
        finish();  // Optionally finish the current activity
    }

    public void listenForPlayers(String gameCode) {
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("Games").child("game_" + gameCode).child("players");

        gameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                playersList.clear();  // Limpiar la lista antes de agregar los nuevos jugadores

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Player player = snapshot.getValue(Player.class);
                    if (playersList != null) {
                        playersList.add(player);
                    }
                }

                playerAdapter.notifyDataSetChanged();  // Notificar que los datos han cambiado
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Error al leer los jugadores: " + databaseError.getMessage());
            }
        });
    }
}