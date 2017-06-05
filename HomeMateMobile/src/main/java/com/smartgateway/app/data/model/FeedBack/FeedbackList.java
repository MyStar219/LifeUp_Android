package com.smartgateway.app.data.model.FeedBack;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Terry on 4/7/16.
 */
public class FeedbackList {

    /**
     * status : completed
     * building : Block 01
     * item : window leak
     * date : 2016-05-05 10:13:25
     * condo : Yishun Sapphire
     * id : 3
     * unit : 20-2-21
     */

    private List<FeedbackListBean> feedback_list;

    public List<FeedbackListBean> getFeedback_list() {
        return feedback_list;
    }

    public void setFeedback_list(List<FeedbackListBean> feedback_list) {
        this.feedback_list = feedback_list;
    }

    public static class FeedbackListBean {
        private String status;
        private String building;
        private String item;
        private String type;
        private String date;
        private String condo;
        private int id;
        private String unit;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getBuilding() {
            return building;
        }

        public void setBuilding(String building) {
            this.building = building;
        }

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getCondo() {
            return condo;
        }

        public void setCondo(String condo) {
            this.condo = condo;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
}
