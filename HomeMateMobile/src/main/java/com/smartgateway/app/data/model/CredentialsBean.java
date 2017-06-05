package com.smartgateway.app.data.model;

import com.google.gson.annotations.SerializedName;

public class CredentialsBean {
    @SerializedName("Credentials")
    Credentials credentials;

    private String detail;

    public Credentials getCredentials() {
        return credentials;
    }

    public String getDetail() {
        return detail;
    }
}
