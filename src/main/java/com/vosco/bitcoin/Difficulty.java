package com.vosco.bitcoin;

import java.time.Instant;

public class Difficulty  implements DifficultyListener {

    private DifficultyListener dListener; // listener field

    public long nextDificultyChange() {
        // return millis from 01.01.1970 of teh exact time whne the difficulty will change next
        return Instant.now().toEpochMilli();
    }

    public DifficultyListener returnListener() {
        return this.dListener;
    }

    // register to receive callbacks when difficulty changes ( with teh exact difficulty in %)
    // multiple listeners can be registered
    public void register(DifficultyListener listener) {
         this.dListener = listener;
    }

    // unregister to receive callbacks
    public void unregister(DifficultyListener listener) {
        this.dListener = null;
    }

    @Override
    public void update(double percent) {

    }
}
