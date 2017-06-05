package com.smartgateway.app.data.model.Maintenance;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Terry on 5/7/16.
 */
public class Category {

    /**
     * id : 1
     * name : Maintenance cat 1
     */

    private List<MaintenanceCategoryBean> Maintenance_category;

    public static Category objectFromData(String str) {

        return new Gson().fromJson(str, Category.class);
    }

    public List<MaintenanceCategoryBean> getMaintenance_category() {
        return Maintenance_category;
    }

    public void setMaintenance_category(List<MaintenanceCategoryBean> Maintenance_category) {
        this.Maintenance_category = Maintenance_category;
    }

    public static class MaintenanceCategoryBean {
        private int id;
        private String name;

        public static MaintenanceCategoryBean objectFromData(String str) {

            return new Gson().fromJson(str, MaintenanceCategoryBean.class);
        }

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
