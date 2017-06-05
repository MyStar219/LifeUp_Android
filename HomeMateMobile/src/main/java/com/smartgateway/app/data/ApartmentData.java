package com.smartgateway.app.data;

import ru.johnlife.lifetools.data.AbstractData;

/**
 * Created by yanyu on 5/20/2016.
 */
public class ApartmentData extends AbstractData {
    private int id;
    private String name;
    private String block;
    private String unit;
    private String door;
    private boolean pending;
    private boolean selected;

    public ApartmentData(String name, String block, String unit, String door) {
        this.name = name;
        this.block = block;
        this.unit = unit;
        this.door = door;
    }

    public ApartmentData(int id, String name, String block, String unit, boolean pending, boolean selected) {
        this.id = id;
        this.name = name;
        this.block = block;
        this.unit = unit;
        this.pending = pending;
        this.selected = selected;
    }

    public int getId() {
        return id;
    }

    public String getBlock() {
        return block;
    }

    public String getDoor() {
        return door;
    }

    public String getName() {
        return name;
    }

    public boolean isPending() {
        return pending;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getUnit() {
        return unit;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public void setDoor(String door) {
        this.door = door;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
