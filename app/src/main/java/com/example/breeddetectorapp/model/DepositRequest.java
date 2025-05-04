package com.example.breeddetectorapp.model;

public class DepositRequest {
    private int user_id;
    private String momo_transaction_id;
    private double amount;

    public DepositRequest(int user_id, String momo_transaction_id, double amount) {
        this.user_id = user_id;
        this.momo_transaction_id = momo_transaction_id;
        this.amount = amount;
    }

    // getters + setters nếu cần
}
