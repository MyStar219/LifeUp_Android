package com.smartgateway.app.data.model;


import com.google.gson.annotations.SerializedName;

public class SGWalletDetail {

    private Wallet wallet;

    public Wallet getWallet() {
        return wallet;
    }

    public class Wallet {
        private String balance;
        private String currency;
        @SerializedName("topup_url")
        private String topupUrl;
        @SerializedName("history_url")
        private String historyUrl;
        @SerializedName("maintenancefees_url")
        private String maintenancefeesUrl;
        @SerializedName("transfer_url")
        private String transferUrl;

        public String getBalance() {
            return balance;
        }

        public void setBalance(String balance) {
            this.balance = balance;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getTopupUrl() {
            return topupUrl;
        }

        public void setTopupUrl(String topupUrl) {
            this.topupUrl = topupUrl;
        }

        public String getHistoryUrl() {
            return historyUrl;
        }

        public void setHistoryUrl(String historyUrl) {
            this.historyUrl = historyUrl;
        }

        public String getMaintenancefeesUrl() {
            return maintenancefeesUrl;
        }

        public void setMaintenancefeesUrl(String maintenancefeesUrl) {
            this.maintenancefeesUrl = maintenancefeesUrl;
        }

        public String getTransferUrl() {
            return transferUrl;
        }

        public void setTransferUrl(String transferUrl) {
            this.transferUrl = transferUrl;
        }
    }
}








