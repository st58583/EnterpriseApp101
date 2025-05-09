package com.example.enterpriseapp.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standardní formát odpovědi")
public class ApiResponse<T> {

    @Schema(description = "Výsledek operace (ok/error)")
    private String response;

    @Schema(description = "Kód chyby (0 = bez chyby)")
    private int errorCode;

    @Schema(description = "Popis odpovědi nebo chyby")
    private String description;

    @Schema(description = "Vrácená data")
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(String response, int errorCode, String description, T data) {
        this.response = response;
        this.errorCode = errorCode;
        this.description = description;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(String description, T data) {
        return new ApiResponse<>("ok", 0, description, data);
    }

    public static <T> ApiResponse<T> error(int errorCode, String description) {
        return new ApiResponse<>("error", errorCode, description, null);
    }

    // Gettery a settery

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
