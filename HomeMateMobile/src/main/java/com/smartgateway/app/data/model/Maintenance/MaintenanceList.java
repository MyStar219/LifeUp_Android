package com.smartgateway.app.data.model.Maintenance;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Terry on 4/7/16.
 */
public class MaintenanceList {

    /**
     * status : done
     * item : Maintenance report submission 1
     * block : Block 01
     * date : 2016-05-05 00:27:26
     * condo : Yishun Sapphire
     * id : 3
     * unit : 20-2-21
     */

    private List<MaintenanceListBean> maintenance_list;


    public List<MaintenanceListBean> getMaintenance_list() {
        return maintenance_list;
    }

    public void setMaintenance_list(List<MaintenanceListBean> maintenance_list) {
        this.maintenance_list = maintenance_list;
    }

    public static class MaintenanceListBean {
        private String status;
        private String item;
        private String block;
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

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public String getBlock() {
            return block;
        }

        public void setBlock(String block) {
            this.block = block;
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
