package com.smartgateway.app.service;

import android.content.res.Resources;

import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.data.AnnouncementData;
import com.smartgateway.app.data.ApartmentData;
import com.smartgateway.app.data.BookingData;
import com.smartgateway.app.data.FacilityData;
import com.smartgateway.app.data.FamilyData;
import com.smartgateway.app.data.FeedbackData;
import com.smartgateway.app.data.MaintenanceData;
import com.smartgateway.app.data.PaymentData;

import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetools.ClassConstantsProvider;
import ru.johnlife.lifetools.service.BaseBackgroundService;

/**
 * Created by yanyu on 5/13/2016.
 */
public class BackgroundService extends BaseBackgroundService {
    private static BackgroundService instance = null;
    public static BackgroundService getInstance() {
        return instance;
    }

    private List<FacilityData> facilities = new ArrayList<>();

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }

    @Override
    protected ClassConstantsProvider getClassConstants() {
        return Constants.CLASS_CONSTANTS;
    }

    public FacilityData getFacility(String name) { //replace by id
        for (FacilityData facility : facilities) {
            if (facility.getName().equals(name)) {
                return facility;
            }
        }
        throw new Resources.NotFoundException("Unable to find facility with name "+name);
    }

    public List<PaymentData> getPayments() {
        return DummyDataStorage.dummyPayments;
    }

}
