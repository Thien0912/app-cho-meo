package com.example.breeddetectorapp.model;


public class GoogleLoginRequest {
    private String id_token;
    private String email;

    public GoogleLoginRequest(String id_token, String email) {
        this.id_token = id_token;
        this.email = email;
    }

    // Getters và Setters
    public String getIdToken() {
        return id_token;
    }

    public void setIdToken(String id_token) {
        this.id_token = id_token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}