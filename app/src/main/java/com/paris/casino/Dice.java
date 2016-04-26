package com.paris.casino;

import java.util.Random;

/**
 * Created by yddn on 000026/4/26.
 */
public class Dice {

    public Dice() {
        number = 0;
    }

    public void lot() {
        Random r = new Random();
        number = r.nextInt(6)+1;
    }

    public int getNumber() {
        return number;
    }

    private int number;
}
