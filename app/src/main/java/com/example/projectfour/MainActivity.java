// FILE: MainActivity.java
package com.example.projectfour;

import android.os.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Handler uiHandler;
    private PlayerThread player1, player2;
    private int winningHole;
    private boolean isGameOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initGame(); // 🔹 Start game when app launches
    }

    private void initGame() {
        // 🔹 Randomly pick a winning hole
        winningHole = new Random().nextInt(50);

        // 🔹 Handle player messages
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message message) {
                if (isGameOver) return;

                int hole = message.arg1;
                int playerId = message.arg2;

                int outcome = evaluateShot(hole, playerId);
                updateUI(hole, playerId, outcome);

                // Send result back to the player thread
                Message result = Message.obtain();
                result.arg1 = outcome;
                if (playerId == 1) {
                    player1.getHandler().sendMessage(result);
                } else {
                    player2.getHandler().sendMessage(result);
                }
            }
        };

        // 🔹 Start both players with different strategies
        player1 = new PlayerThread(1, uiHandler, StrategyType.AGGRESSIVE);
        player2 = new PlayerThread(2, uiHandler, StrategyType.DEFENSIVE);
        player1.start();
        player2.start();
    }

    private int evaluateShot(int hole, int playerId) {
        // 🔹 TODO: Check hole groupings, other player, etc.
        if (hole == winningHole) {
            endGame(playerId);
            return ResultType.JACKPOT;
        }
        return ResultType.BIG_MISS; // Default for now
    }

    private void updateUI(int hole, int playerId, int outcome) {
        // 🔹 TODO: Add RecyclerView or TextView updates
        System.out.println("Player " + playerId + " -> Hole " + hole + " (" + outcome + ")");
    }

    private void endGame(int winnerId) {
        isGameOver = true;
        player1.quit();
        player2.quit();
        Toast.makeText(this, "Player " + winnerId + " wins!", Toast.LENGTH_LONG).show();
    }
}
