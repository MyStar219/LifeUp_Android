package ru.johnlife.lifetools.event;

public class DetailEvent {

    private String detail;
    private int responseCode;

    public DetailEvent(String detail, int responseCode) {
        this.detail = detail;
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getDetail() {
        return detail;
    }
}
