package com.smartgateway.app.data.RealmDB;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Terry on 10/7/16.
 */
public class TypeRealm extends RealmObject{

    public String type;
    public int id;
    public RealmList<FacilityRealm> facilityRealms;
}
