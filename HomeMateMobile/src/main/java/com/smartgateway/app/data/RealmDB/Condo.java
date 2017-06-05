package com.smartgateway.app.data.RealmDB;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Terry on 7/7/16.
 */
public class Condo extends RealmObject {

    public String condo_id;
    public RealmList<Building> buildings;
}
