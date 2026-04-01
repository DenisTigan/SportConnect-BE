package com.SportConnect.demo.dto;

public record AuthRespone(String token, String type) {

    public AuthRespone(String token){
        this(token,"Bearer");
    }
}
