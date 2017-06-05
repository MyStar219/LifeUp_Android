package com.smartgateway.app.data.model.Family;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Terry on 7/7/16.
 */
public class FamilyList {

    /**
     * user_id : 26
     * name : saw
     * relationship : Owner
     */

    private List<FamilyBean> Family;
    /**
     * user_id : 26
     * name : 34222
     * relationship : father
     */

    private List<PendingFamilyBean> Pending_Family;

    public static FamilyList objectFromData(String str) {

        return new Gson().fromJson(str, FamilyList.class);
    }

    public List<FamilyBean> getFamily() {
        return Family;
    }

    public void setFamily(List<FamilyBean> Family) {
        this.Family = Family;
    }

    public List<PendingFamilyBean> getPending_Family() {
        return Pending_Family;
    }

    public void setPending_Family(List<PendingFamilyBean> Pending_Family) {
        this.Pending_Family = Pending_Family;
    }

    public static class FamilyBean {
        private int user_id;
        private String name;
        private String relationship;

        public static FamilyBean objectFromData(String str) {

            return new Gson().fromJson(str, FamilyBean.class);
        }

        public int getUser_id() {
            return user_id;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRelationship() {
            return relationship;
        }

        public void setRelationship(String relationship) {
            this.relationship = relationship;
        }
    }

    public static class PendingFamilyBean {
        private int user_id;
        private String name;
        private String relationship;

        public static PendingFamilyBean objectFromData(String str) {

            return new Gson().fromJson(str, PendingFamilyBean.class);
        }

        public int getUser_id() {
            return user_id;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRelationship() {
            return relationship;
        }

        public void setRelationship(String relationship) {
            this.relationship = relationship;
        }
    }
}
