package com.example.suco.dto.tienich.tien.quydoi;

public class GiaoDichResultDTO {

    private boolean success;
    private String message;

    public GiaoDichResultDTO() {
    }

    public GiaoDichResultDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}