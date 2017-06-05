package com.smartgateway.app.data.model.Apartment;

import java.util.List;

/**
 * Created by Terry on 4/7/16.
 */
public class Condos {

    /**
     * name : Yishun Sapphire
     * condo_id : 2
     */

    private List<CondosBean> Condos;

    public List<CondosBean> getCondos() {
        return Condos;
    }

    public void setCondos(List<CondosBean> Condos) {
        this.Condos = Condos;
    }

    public static class CondosBean {
        private String name;
        private int condo_id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCondo_id() {
            return condo_id;
        }

        public void setCondo_id(int condo_id) {
            this.condo_id = condo_id;
        }
    }
}
