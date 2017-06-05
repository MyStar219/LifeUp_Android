package com.smartgateway.app.data;

import ru.johnlife.lifetools.data.AbstractData;

/**
 * Created by yanyu on 5/17/2016.
 */
public class FamilyData extends AbstractData {
    private int id;
    private String name;
    private String relation;
    private boolean pending;

    public FamilyData(int id,String name, String relation, boolean pending) {
        this.id = id;
        this.name = name;
        this.relation = relation;
        this.pending = pending;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isPending() {
        return pending;
    }

    public String getRelation() {
        return relation;
    }
}
