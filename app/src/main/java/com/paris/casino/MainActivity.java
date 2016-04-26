package com.paris.casino;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    final static int DICE_NUMBER = 4;
    final static int PLAYER_NUMBER = 2;

    Dice[] dices = new Dice[DICE_NUMBER];
    Player[] players = new Player[PLAYER_NUMBER];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create all the players
        for (int i = 0; i < PLAYER_NUMBER; i++) {
            players[i] = new Player("Player" + i);
        }

        // Create all the Dice objects
        for (int i = 0; i < DICE_NUMBER; i++) {
            dices[i] = new Dice();
            dices[i].lot();
        }

        for (int i = 0; i < DICE_NUMBER; i++) {
            System.out.println("The number of Dice " + i + " is " + dices[i].getNumber());
        }
    }
}
