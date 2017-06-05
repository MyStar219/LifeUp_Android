package com.smartgateway.app.data;

import android.content.res.Resources;

import java.util.List;

import ru.johnlife.lifetools.data.AbstractData;

/**
 * Created by yanyu on 5/13/2016.
 */
public class FacilityData extends AbstractData {
    private int iconRes;
    private int id;
    private String name;
    private List<FacilityVariantData> variants;

    public FacilityData(int iconRes, int id, String name) {
        this.iconRes = iconRes;
        this.name = name;
        this.id = id;
    }

    public FacilityData(int iconRes, String name, List<FacilityVariantData> variants) {
        this.iconRes = iconRes;
        this.name = name;
        this.variants = variants;
    }

    public int getId() {
        return id;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getName() {
        return name;
    }

    public List<FacilityVariantData> getVariants() {
        return variants;
    }

    public FacilityVariantData getVariant(String name) {  //TODO: replace by ID
        for (FacilityVariantData variant : variants) {
            if (variant.getName().equals(name)) {
                return variant;
            }
        }
        throw new Resources.NotFoundException("Unable to find variant with name "+name);
    }

    public void setVariants(List<FacilityVariantData> variants) {
        this.variants = variants;
    }
}
