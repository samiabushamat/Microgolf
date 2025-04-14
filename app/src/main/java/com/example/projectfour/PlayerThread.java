package com.example.projectfour;

import android.os.*;
import androidx.annotation.NonNull;

import java.util.*;

public class PlayerThread extends Thread {
    private final int playerId;
    private final Handler uiHandler;
    private Handler playerHandler;
    private Looper looper;
    private final StrategyType strategy;
    private final Set<Integer> attemptedHoles = new HashSet<>();
    private int previousOutcome = -1;
    private boolean isFirstMove = true;
    private final Set<Integer> testedGroups = new HashSet<>();
    private int currentGroup = -1;
    private int previousHole = -1;
    private final Random random = new Random();

    // 🟣 PlayerThread constructor
    public PlayerThread(int id, Handler uiHandler, StrategyType strategy) {
        this.playerId = id;
        this.uiHandler = uiHandler;
        this.strategy = strategy;
    }
    // PlayerThread Getter
    public Handler getHandler() {
        return playerHandler;
    }
    // Quit for the looper
    public void quit() {
        if (looper != null) looper.quitSafely();
    }

    // 🟣 Run method for the PlayerThread
    @Override
    public void run() {
        // preparing and starting the looper
        Looper.prepare();
        looper = Looper.myLooper();

        // creating a new handler for the player
        playerHandler = new Handler(Objects.requireNonNull(looper)) {

            // Handle messages from the UI
            @Override
            public void handleMessage(@NonNull Message message) {
                // if the message is negative settings the previous outcome
                if (message.arg1 >= 0) {
                    previousOutcome = message.arg1;
                    return;
                }

                // setting up the shot logic if it is there first shot
                int hole;
                // first shot
                if (isFirstMove) {
                    hole = chooseRandomHole();
                    isFirstMove = false;
                } else { // if the player thread has an aggresive or defensive strategy type
                    if (strategy == StrategyType.AGGRESSIVE) {
                        hole = chooseAggressiveHole();
                    } else {
                        hole = chooseDefensiveHole();
                    }
                }

                // settings the previous hole and adding it to the attempted holes and calculating the current group
                previousHole = hole;
                attemptedHoles.add(hole);
                currentGroup = (hole - 1) / 5;

                // sending the move to the UI handler
                Message move = Message.obtain();
                move.arg1 = hole;
                move.arg2 = playerId;
                uiHandler.sendMessage(move);
            }
        };

        // looping
        Looper.loop();
    }

    // 🟣 AGGRESSIVE: Focus on holes in the current group or adjacent groups
    private int chooseAggressiveHole() {
        if (previousOutcome == ResultType.NEAR_MISS) {
            return getRandomHoleFromGroup(currentGroup);
        } else if (previousOutcome == ResultType.NEAR_GROUP) {
            int[] adj = {Math.max(0, currentGroup - 1), Math.min(9, currentGroup + 1)};
            return getHoleFromRandomGroup(adj);
        } else if (previousOutcome == ResultType.BIG_MISS) {
            int newGroup;
            do {
                newGroup = random.nextInt(10);
            } while (newGroup == currentGroup);
            return getRandomHoleFromGroup(newGroup);
        } else {
            return chooseRandomHole();
        }
    }

    // 🟣 DEFENSIVE: Systematically probe unvisited groups
    private int chooseDefensiveHole() {
        if (previousOutcome == ResultType.NEAR_MISS || previousOutcome == ResultType.NEAR_GROUP) {
            int[] probe = {Math.max(0, currentGroup - 1), Math.min(9, currentGroup + 1)};
            return getHoleFromRandomGroup(probe);
        }

        for (int g = 0; g < 10; g++) {
            if (!testedGroups.contains(g)) {
                testedGroups.add(g);
                return getRandomHoleFromGroup(g);
            }
        }

        return chooseRandomHole();
    }

    private int chooseRandomHole() {
        int hole;
        do {
            hole = random.nextInt(50) + 1;
        } while (attemptedHoles.contains(hole));
        return hole;
    }

    private int getRandomHoleFromGroup(int group) {
        int start = group * 5 + 1;
        int end = start + 4;
        List<Integer> available = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            if (!attemptedHoles.contains(i)) {
                available.add(i);
            }
        }
        return available.isEmpty() ? chooseRandomHole() : available.get(random.nextInt(available.size()));
    }

    private int getHoleFromRandomGroup(int[] groups) {
        for (int g : groups) testedGroups.add(g);
        return getRandomHoleFromGroup(groups[random.nextInt(groups.length)]);
    }
}
