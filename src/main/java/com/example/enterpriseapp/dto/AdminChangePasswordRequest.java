package com.example.enterpriseapp.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminChangePasswordRequest {

    @NotBlank
    private String newPassword;

    public AdminChangePasswordRequest() {}

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
