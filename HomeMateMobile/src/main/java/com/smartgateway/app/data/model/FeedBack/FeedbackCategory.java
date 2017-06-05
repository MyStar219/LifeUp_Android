package com.smartgateway.app.data.model.FeedBack;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Terry on 4/7/16.
 */
public class FeedbackCategory {

    /**
     * id : 1
     * name : Feedback cat 1
     */

    private List<FeedbackCategoryBean> Feedback_category;

    public List<FeedbackCategoryBean> getFeedback_category() {
        return Feedback_category;
    }

    public void setFeedback_category(List<FeedbackCategoryBean> Feedback_category) {
        this.Feedback_category = Feedback_category;
    }

    public static class FeedbackCategoryBean {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
