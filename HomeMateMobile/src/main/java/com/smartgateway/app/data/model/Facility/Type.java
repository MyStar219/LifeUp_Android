package com.smartgateway.app.data.model.Facility;

import java.util.List;

import io.realm.RealmObject;

/**
 * Created by Terry on 29/6/16.
 */
public class Type{

    /**
     * type : BBQ Pit
     * id : 1
     */

    private List<TypeBean> Type;

    public List<TypeBean> getType() {
        return Type;
    }

    public void setType(List<TypeBean> Type) {
        this.Type = Type;
    }

    public static class TypeBean {
        private String type;
        private int id;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
