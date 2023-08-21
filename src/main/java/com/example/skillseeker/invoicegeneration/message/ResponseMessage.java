package com.example.skillseeker.invoicegeneration.message;

/**
 * @author I077659
 */
public class ResponseMessage {

    private String message;

    public ResponseMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
