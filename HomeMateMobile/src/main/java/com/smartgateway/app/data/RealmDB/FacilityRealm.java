package com.smartgateway.app.data.RealmDB;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Terry on 10/7/16.
 */
public class FacilityRealm extends RealmObject {

    public String name;
    public int fid;
    public String image_url;
    public RealmList<DatesRealm> datesRealms;
}
