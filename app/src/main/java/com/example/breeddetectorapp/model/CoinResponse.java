package com.example.breeddetectorapp.model;

public class CoinResponse {
    private String message;
    private int coins;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }
}