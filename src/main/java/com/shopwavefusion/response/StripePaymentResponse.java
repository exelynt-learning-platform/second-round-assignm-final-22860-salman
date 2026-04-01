package com.shopwavefusion.response;

public class StripePaymentResponse {

    private String chargeId;
    private String status;
    private long amount;
    private String currency;
    private String message;
    private boolean success;

    public StripePaymentResponse() {
    }

    public StripePaymentResponse(String chargeId, String status, long amount, String currency, String message,
            boolean success) {
        this.chargeId = chargeId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.message = message;
        this.success = success;
    }

    public String getChargeId() {
        return chargeId;
    }

    public void setChargeId(String chargeId) {
        this.chargeId = chargeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
