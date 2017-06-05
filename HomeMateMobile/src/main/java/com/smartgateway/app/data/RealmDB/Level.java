package com.smartgateway.app.data.RealmDB;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Terry on 7/7/16.
 */
public class Level extends RealmObject {

    public int level;
    public RealmList<Unit> units;

}
