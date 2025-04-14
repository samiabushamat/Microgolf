// imports
package com.example.projectfour;
import android.graphics.Color;
import android.os.*;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    // variables
    private final Set<Integer> playerOneShots = new HashSet<>();
    private final Set<Integer> playerTwoShots = new HashSet<>();

    private Handler uiHandler;
    private PlayerThread player1, player2;
    private int winningHole;
    private boolean isGameOver = false;
    private int playerOneLastShot = -1;
    private int playerTwoLastShot = -1;
    private int currentTurnPlayer = 1;

    LinearLayout holeContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        holeContainer = findViewById(R.id.holeContainer);

        // create ui with 50 holes
        for (int i = 1; i <= 50; i++) {
            View holeItem = createHoleItem(i);
            holeContainer.addView(holeItem);
        }
        // start the game
        initGame();
    }
    // create a hole item
    private View createHoleItem(int index) {
        // new layout for each hole
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setTag("hole_" + index);

        TextView circle = new TextView(this);
        circle.setText(String.valueOf(index));
        circle.setTextSize(16);
        circle.setTextColor(Color.WHITE);
        circle.setGravity(Gravity.CENTER);
        circle.setBackground(ContextCompat.getDrawable(this, R.drawable.hole_circle));
        LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(100, 100);
        circle.setLayoutParams(circleParams);

        // tag the circle view for later use
        layout.setTag(R.id.hole_circle, circle);

        layout.addView(circle);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.setMargins(0, 8, 0, 8);
        layout.setLayoutParams(layoutParams);

        return layout;
    }

    // function to start the game
    private void initGame() {
        // set the random hole and print it
        winningHole = new Random().nextInt(50) + 1;
        System.out.println("Winning Hole: " + (winningHole));
        // highlight the winning hole
        highlightWinningHole();

        // Initialize UI handler
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message message) {
                // if the game is over return
                if (isGameOver) return;

                // Handle player shot
                int hole = message.arg1;
                int playerId = message.arg2;

                // evaluate the shot to return the outcome and update the ui
                int outcome = evaluateShot(hole, playerId);
                updateUI(hole, playerId, outcome);

                // Send result back to player
                Message result = Message.obtain();
                result.arg1 = outcome;
                if (playerId == 1) {
                    player1.getHandler().sendMessage(result);
                } else {
                    player2.getHandler().sendMessage(result);
                }

                // Schedule next player's move
                currentTurnPlayer = (playerId == 1) ? 2 : 1;
                uiHandler.postDelayed(() -> {
                    Message nextMove = Message.obtain();
                    nextMove.arg1 = -1;
                    nextMove.arg2 = currentTurnPlayer;
                    if (currentTurnPlayer == 1) {
                        player1.getHandler().sendMessage(nextMove);
                    } else {
                        player2.getHandler().sendMessage(nextMove);
                    }
                }, 2000);
            }
        };

        player1 = new PlayerThread(1, uiHandler, StrategyType.AGGRESSIVE);
        player2 = new PlayerThread(2, uiHandler, StrategyType.DEFENSIVE);
        player1.start();
        player2.start();

        // start off the first move after 1 second
        uiHandler.postDelayed(() -> {
            Message firstMove = Message.obtain();
            firstMove.arg1 = -1;
            firstMove.arg2 = 1;
            player1.getHandler().sendMessage(firstMove);
        }, 1000);
    }

    // function to evaluate the shot
    private int evaluateShot(int hole, int playerId) {
        // Update history
        if (playerId == 1) playerOneShots.add(hole);
        else playerTwoShots.add(hole);

        //  check for catastrophe if this hole has already been occupied by the opponent
        if (playerId == 1 && playerTwoShots.contains(hole)) {
            endGame(2); // Player 2 wins
            return ResultType.CATASTROPHE;
        }
        if (playerId == 2 && playerOneShots.contains(hole)) {
            endGame(1); // Player 1 wins
            return ResultType.CATASTROPHE;
        }
        if (hole == winningHole) {
            endGame(playerId);
            return ResultType.JACKPOT;
        }

        // 📍 Determine group result
        int shotGroup = (hole - 1) / 5;
        int winningGroup = (winningHole - 1) / 5;

        if (shotGroup == winningGroup) return ResultType.NEAR_MISS;
        if (Math.abs(shotGroup - winningGroup) == 1) return ResultType.NEAR_GROUP;

        return ResultType.BIG_MISS;
    }


    private void updateUI(int hole, int playerId, int outcome) {
        runOnUiThread(() -> {
            // Reset previous shot
            if (playerId == 1 && playerOneLastShot != -1) {
                resetHoleIfNotWinning(playerOneLastShot);
            } else if (playerId == 2 && playerTwoLastShot != -1) {
                resetHoleIfNotWinning(playerTwoLastShot);
            }

            // Update current shot
            LinearLayout layout = holeContainer.findViewWithTag("hole_" + hole);
            if (layout != null) {
                TextView circle = (TextView) layout.getTag(R.id.hole_circle);
                if (circle != null) {
                    int drawableRes = (hole == winningHole)
                            ? R.drawable.winning_hole
                            : (playerId == 1 ? R.drawable.player_one_hole : R.drawable.player_two_hole);
                    circle.setBackground(ContextCompat.getDrawable(this, drawableRes));
                }
            }

            // Track last shot
            if (playerId == 1) playerOneLastShot = hole;
            else playerTwoLastShot = hole;

            String outcomeText = getOutcomeText(outcome);
            Toast.makeText(this, "Player " + playerId + " Shot Hole " + hole + " -> " + outcomeText, Toast.LENGTH_SHORT).show();
        });
    }

    private void resetHoleIfNotWinning(int hole) {
        LinearLayout layout = holeContainer.findViewWithTag("hole_" + hole);
        if (layout != null) {
            TextView circle = (TextView) layout.getTag(R.id.hole_circle);
            if (circle != null) {
                // Keep winning hole yellow, reset others to default
                int drawableRes = (hole == winningHole)
                        ? R.drawable.winning_hole
                        : R.drawable.hole_circle;
                circle.setBackground(ContextCompat.getDrawable(this, drawableRes));
            }
        }
    }
    private void highlightWinningHole() {
        LinearLayout layout = holeContainer.findViewWithTag("hole_" + winningHole);
        if (layout != null) {
            TextView circle = (TextView) layout.getTag(R.id.hole_circle);
            if (circle != null) {
                circle.setBackground(ContextCompat.getDrawable(this, R.drawable.winning_hole));
            }
        }
    }


    private void endGame(int winnerId) {
        isGameOver = true;
        player1.quit();
        player2.quit();
    }

    private String getOutcomeText(int outcome) {
        switch (outcome) {
            case ResultType.JACKPOT:
                return "JACKPOT";
            case ResultType.NEAR_MISS:
                return "Near Miss!";
            case ResultType.NEAR_GROUP:
                return "Near Group";
            case ResultType.BIG_MISS:
                return "Big Miss";
            case ResultType.CATASTROPHE:
                return "Catastrophe";
            default:
                return "Unknown";
        }
    }

}
