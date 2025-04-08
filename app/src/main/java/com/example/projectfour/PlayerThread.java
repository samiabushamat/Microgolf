package com.example.projectfour;

import android.os.*;

import androidx.annotation.NonNull;

import java.util.*;

public class PlayerThread extends Thread {
    private final int playerId;
    private final Handler uiHandler;
    private Handler playerHandler;
    private Looper looper;
    private StrategyType strategy;
    private final Set<Integer> attemptedHoles = new HashSet<>();

    public PlayerThread(int id, Handler uiHandler, StrategyType strategy) {
        this.playerId = id;
        this.uiHandler = uiHandler;
        this.strategy = strategy;
    }

    public Handler getHandler() {
        return playerHandler;
    }

    public void quit() {
        if (looper != null) looper.quitSafely();
    }

    @Override
    public void run() {
        Looper.prepare();
        looper = Looper.myLooper();

        playerHandler = new Handler(looper) {
            @Override
            public void handleMessage(@NonNull Message message) {
                int outcome = message.arg1;
                postDelayedNextShot(); // Wait 2s, then send next move
            }
        };

        // First move
        postDelayedNextShot();

        Looper.loop();
    }

    private void postDelayedNextShot() {
        playerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int hole = chooseNextHole();
                attemptedHoles.add(hole);

                Message move = Message.obtain();
                move.arg1 = hole;
                move.arg2 = playerId;
                uiHandler.sendMessage(move);
            }
        }, 2000);
    }

    private int chooseNextHole() {
        // 🔹 TODO: implement smarter strategy later
        Random rand = new Random();
        int hole;
        do {
            hole = rand.nextInt(50); // choose unattempted
        } while (attemptedHoles.contains(hole));
        return hole;
    }
}
