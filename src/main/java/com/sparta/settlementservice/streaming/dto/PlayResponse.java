package com.sparta.settlementservice.streaming.dto;

public class PlayResponse {
    private boolean success;
    private String message;

    public PlayResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
