package com.paris.casino;

/**
 * Created by exe on 000026/4/26.
 */
public class Player {

    public Player(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
}
