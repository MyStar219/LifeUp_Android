package com.smartgateway.app.data.model.User;

import com.smartgateway.app.data.model.Credentials;

/**
 * User
 * Created by Terry on 30/6/16.
 */
public class User {

    private String detail;
    private Credentials Credentials;

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }

    private UserBean User;

    public UserBean getUser() {
        return User;
    }

    public void setUser(UserBean User) {
        this.User = User;
    }

    private Notification Notification;

    public static class Notification {
        private int feedback;
        private int system;
        private int maintenance;
        private int condo;
        private int booking;

        private boolean family;
        private boolean transfer;
        private boolean maintenance_fees;
        private boolean history;

        public int getCondo() {
            return condo;
        }

        public void setCondo(int condo) {
            this.condo = condo;
        }

        public int getFeedback() {
            return feedback;
        }

        public void setFeedback(int feedback) {
            this.feedback = feedback;
        }

        public int getMaintenance() {
            return maintenance;
        }

        public void setMaintenance(int maintenance) {
            this.maintenance = maintenance;
        }

        public int getSystem() {
            return system;
        }

        public void setSystem(int system) {
            this.system = system;
        }

        public boolean getFamily() {
            return family;
        }

        public void setFamily(boolean family) {
            this.family = family;
        }

        public boolean getHistory() {
            return history;
        }

        public void setHistory(boolean history) {
            this.history = history;
        }

        public boolean getTransfer() {
            return transfer;
        }

        public void setTransfer(boolean transfer) {
            this.transfer = transfer;
        }

        public boolean getMaintenance_fees() {
            return maintenance_fees;
        }

        public void setMaintenance_fees(boolean maintenance_fees) {
            this.maintenance_fees = maintenance_fees;
        }
        public int getBooking() {
            return booking;
        }

        public void setBooking(int booking) {
            this.booking = booking;
        }
    }

    public Notification getNotification() {
        return Notification;
    }

    public void setNotification(Notification notification) {
        Notification = notification;
    }

    public static class UserBean {
        private String token;
        private String name;
        private String mobile;
        private String email;
        private String image_url;
        private String default_condo;

        public String getDefault_condo() {
            return default_condo;
        }

        public void setDefault_condo(String default_condo) {
            this.default_condo = default_condo;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }
    }

    public Credentials getCredentials() {
        return Credentials;
    }
}
