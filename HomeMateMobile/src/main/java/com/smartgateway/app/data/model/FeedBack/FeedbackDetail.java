package com.smartgateway.app.data.model.FeedBack;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Terry on 4/7/16.
 */
public class FeedbackDetail {


    /**
     * status : completed
     * item : window leak
     * unit : 20-2-21
     * image :
     * date : 2016-05-05 10:13:25
     * condo : Yishun Sapphire
     * category : Feedback cat 1
     * id : 3
     * block : Block 01
     * description : since 3 days
     */

    private FeedbackBean feedback;

    public static FeedbackDetail objectFromData(String str) {

        return new Gson().fromJson(str, FeedbackDetail.class);
    }

    public FeedbackBean getFeedback() {
        return feedback;
    }

    public void setFeedback(FeedbackBean feedback) {
        this.feedback = feedback;
    }

    public static class FeedbackBean {
        private String status;
        private String item;
        private String unit;
        private String image;
        private String date;
        private String condo;
        private String category;
        private int id;
        private String block;
        private String description;
        @SerializedName("image_urls")
        private ArrayList<String> imageUrl;

        public static FeedbackBean objectFromData(String str) {

            return new Gson().fromJson(str, FeedbackBean.class);
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
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

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getBlock() {
            return block;
        }

        public void setBlock(String block) {
            this.block = block;
        }

        public List<String> getImageUrl() {
            return imageUrl;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
